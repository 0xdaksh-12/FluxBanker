package com.fluxbanker.api.service;

import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.exception.NotFoundException;
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
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getAddress1() != null) user.setAddress1(request.getAddress1());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getPinCode() != null) user.setPinCode(request.getPinCode());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getProfilePic() != null) user.setProfilePic(request.getProfilePic());
        return userRepository.save(user);
    }

    /**
     * Simulates sending a password reset email.
     */
    public void requestPasswordReset(UUID userId) {
        User user = getUserById(userId);
        // In a real app, generate token and send email
        System.out.println("Simulated: Password reset link sent to " + user.getEmail());
    }

    /**
     * Verifies user email.
     */
    public User verifyEmail(UUID id) {
        User user = getUserById(id);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
}
