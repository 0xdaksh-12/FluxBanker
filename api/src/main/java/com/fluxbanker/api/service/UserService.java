package com.fluxbanker.api.service;

import com.fluxbanker.api.entity.EmailVerificationToken;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.exception.NotFoundException;
import com.fluxbanker.api.repository.EmailVerificationTokenRepository;
import com.fluxbanker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * User persistence logic.
 * Mirrors Node's services/userServices.js.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    /**
     * Persists a new user.
     *
     * @param user entity with all required fields set
     * @return saved user with generated ID
     */
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Finds a user by email, including the password field.
     *
     * @param email the user's email address
     * @return the found user
     * @throws NotFoundException if no user exists with that email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Finds a user by ID.
     *
     * @param id the user's UUID
     * @return the found user
     * @throws NotFoundException if no matching user exists
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Returns true if any user record exists with the given email.
     *
     * @param email the email to check
     * @return true if taken, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Updates user details.
     */
    public User updateUser(UUID id, com.fluxbanker.api.dto.request.UserUpdateRequest request) {
        User user = getUserById(id);
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getAddress1() != null)
            user.setAddress1(request.getAddress1());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getState() != null)
            user.setState(request.getState());
        if (request.getPinCode() != null)
            user.setPinCode(request.getPinCode());
        if (request.getDateOfBirth() != null)
            user.setDateOfBirth(request.getDateOfBirth());
        if (request.getProfilePic() != null)
            user.setProfilePic(request.getProfilePic());
        return userRepository.save(user);
    }

    /**
     * Saves a user entity directly.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Sends a verification email to the user.
     */
    @org.springframework.transaction.annotation.Transactional
    public User sendVerificationEmail(UUID id) {
        User user = getUserById(id);

        emailVerificationTokenRepository.deleteByUser(user);

        String tokenString = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(tokenString)
                .user(user)
                .expiryDate(java.time.Instant.now().plus(java.time.Duration.ofHours(24)))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), tokenString);
        return user;
    }

    /**
     * Verifies the user's email using the provided token.
     */
    @org.springframework.transaction.annotation.Transactional
    public void verifyEmailToken(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isInvalid()) {
            throw new IllegalArgumentException("Token is invalid or expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    /**
     * Updates KYC status for a user.
     */
    public User updateKycStatus(UUID id, User.KycStatus status) {
        User user = getUserById(id);
        user.setKycStatus(status);
        return userRepository.save(user);
    }
}
