package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.request.LoginRequest;
import com.fluxbanker.api.dto.request.RegisterRequest;
import com.fluxbanker.api.entity.Session;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.exception.ConflictException;
import com.fluxbanker.api.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import com.fluxbanker.api.dto.request.ForgotPasswordRequest;
import com.fluxbanker.api.dto.request.ResetPasswordRequest;
import com.fluxbanker.api.entity.PasswordResetToken;
import com.fluxbanker.api.repository.PasswordResetTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.fluxbanker.api.entity.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Core authentication business logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final AccountService accountService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.node-env}")
    private String nodeEnv;

    @Value("${app.admin-emails:}")
    private java.util.List<String> adminEmails;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    /**
     * Registers a new user, creates a session, and sets the refresh token cookie.
     *
     * @param request  the register request DTO
     * @param req      HTTP request (for userAgent + IP)
     * @param response HTTP response (for setting the cookie)
     * @return the signed access token
     */
    public String register(RegisterRequest request, HttpServletRequest req, HttpServletResponse response) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new ConflictException("User already exists");
        }

        User.Role userRole = User.Role.USER;
        if (adminEmails != null && adminEmails.contains(request.getEmail())) {
            userRole = User.Role.ADMIN;
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address1(request.getAddress1())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .dateOfBirth(request.getDateOfBirth())
                .aadhaar(request.getAadhaar())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .kycStatus(userRole == User.Role.ADMIN ? User.KycStatus.APPROVED : User.KycStatus.PENDING)
                .build();
        user = userService.createUser(user);

        // Send verification email via UserService to handle token generation
        userService.sendVerificationEmail(user.getId());

        // Auto-provision a default checking account for the new user
        accountService.provisionAccount(user.getId(), "Flux Checking", Account.Subtype.CHECKING);

        return createAuthSession(user, req, response);
    }

    /**
     * Authenticates a user by email/password, creates a session, and sets the
     * refresh token cookie.
     *
     * @param request  the login request DTO
     * @param req      HTTP request (for userAgent + IP)
     * @param response HTTP response (for setting the cookie)
     * @return String accessToken
     */
    public String login(LoginRequest request, HttpServletRequest req, HttpServletResponse response) {
        User user;
        try {
            user = userService.getUserByEmail(request.getEmail());
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return createAuthSession(user, req, response);
    }

    /**
     * Invalidates the session and clears the refresh token cookie.
     *
     * @param sessionId the session to invalidate
     * @param response  HTTP response (for clearing the cookie)
     */
    public void logout(UUID sessionId, HttpServletResponse response) {
        sessionService.invalidateSession(sessionId);
        clearRefreshCookie(response);
    }

    /**
     * Validates the refresh token cookie, optionally rotates the refresh token,
     * and returns a new access token.
     *
     * @param req      HTTP request (reads the refresh token cookie)
     * @param response HTTP response (optional new refresh cookie)
     * @return new signed access token, or null if no cookie present (returns 204)
     */
    public String refresh(HttpServletRequest req, HttpServletResponse response) {
        String cookieToken = getRefreshCookie(req);
        if (cookieToken == null) {
            return null; // Controller sends 204
        }

        try {
            Claims claims = jwtService.verifyRefreshToken(cookieToken);
            UUID userId = UUID.fromString(claims.getSubject());
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));

            sessionService.checkSession(cookieToken, sessionId);

            String accessToken = jwtService.generateAccessToken(userId, sessionId);

            // Rotate refresh token if past its halfway point
            if (jwtService.isTokenHalfExpired(claims)) {
                String newRefreshToken = jwtService.generateRefreshToken(userId, sessionId);
                sessionService.saveToken(sessionId, newRefreshToken);
                setRefreshCookie(response, newRefreshToken);
            }

            return accessToken;
        } catch (Exception e) {
            // If refresh token fails for ANY reason, remove it as requested
            clearRefreshCookie(response);
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    /**
     * Generates a password reset token and sends an email.
     * @param request the forgot password request
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
        
        // Delete existing tokens if any
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(java.time.Instant.now().plus(Duration.ofHours(1)))
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    /**
     * Validates a reset token and increments click count.
     * @param token the token string
     * @return the associated user
     */
    @Transactional
    public User validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));

        if (resetToken.isInvalid()) {
            throw new UnauthorizedException("Token is expired, already used, or exceeded click limit");
        }

        resetToken.setClickCount(resetToken.getClickCount() + 1);
        passwordResetTokenRepository.save(resetToken);

        return resetToken.getUser();
    }

    /**
     * Resets the user's password using a token.
     * @param request the reset password request
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));

        if (resetToken.isInvalid()) {
            throw new UnauthorizedException("Token is invalid or expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.saveUser(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        if (request.isSignOutAll()) {
            invalidateAllSessions(user.getId());
        }
    }

    /**
     * Invalidates all sessions for a specific user.
     * @param userId the user's UUID
     */
    @Transactional
    public void invalidateAllSessions(UUID userId) {
        sessionService.invalidateAllUserSessions(userId);
    }

    /**
     * Writes the refresh token as an HTTP-only cookie.
     *
     * @param response HTTP response
     * @param token    the plaintext refresh token value
     */
    public void setRefreshCookie(HttpServletResponse response, String token) {
        boolean isProduction = "production".equalsIgnoreCase(nodeEnv);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(isProduction)
                .path("/auth")
                .maxAge(Duration.ofMillis(refreshExpirationMs))
                .sameSite(isProduction ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears the refresh token cookie by setting maxAge to 0.
     *
     * @param response HTTP response
     */
    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Creates an auth session and issues access + refresh tokens.
     *
     * @param user     the authenticated user
     * @param req      HTTP request
     * @param response HTTP response
     * @return signed access token
     */
    private String createAuthSession(User user, HttpServletRequest req, HttpServletResponse response) {
        String userAgent = req.getHeader("User-Agent");
        String ip = req.getRemoteAddr();

        Session session = sessionService.createSession(user, userAgent, ip);

        String accessToken = jwtService.generateAccessToken(user.getId(), session.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), session.getId());

        sessionService.saveToken(session.getId(), refreshToken);
        setRefreshCookie(response, refreshToken);

        return accessToken;
    }

    private String getRefreshCookie(HttpServletRequest req) {
        if (req.getCookies() == null)
            return null;
        for (jakarta.servlet.http.Cookie c : req.getCookies()) {
            if (REFRESH_TOKEN_COOKIE.equals(c.getName()))
                return c.getValue();
        }
        return null;
    }
}
