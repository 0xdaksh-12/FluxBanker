package com.fluxbanker.api.security;

import com.fluxbanker.api.entity.Session;
import com.fluxbanker.api.exception.UnauthorizedException;
import com.fluxbanker.api.service.JwtService;
import com.fluxbanker.api.service.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT authentication filter — runs once per request.
 * Mirrors Node's middleware/authenticationHandler.js.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.verifyAccessToken(token);
            UUID userId = UUID.fromString(claims.getSubject());
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));

            // Check session validity (mirrors Node's getSession + session.valid check)
            Session session = sessionService.getSession(sessionId);
            if (!session.isValid()) {
                throw new UnauthorizedException("Unauthorized");
            }

            // Verify session belongs to the claimed user
            if (!session.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("Unauthorized");
            }

            // Attach authentication principal
            CustomUserDetails userDetails = new CustomUserDetails(session.getUser());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            sessionId.toString(),
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            logger.debug("JWT authentication failed: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
