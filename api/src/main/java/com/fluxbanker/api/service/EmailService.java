package com.fluxbanker.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@fluxbanker.com}")
    private String fromEmail;

    @org.springframework.beans.factory.annotation.Value("${VITE_CLIENT_URL:${app.client-url:http://localhost:5173}}")
    private String clientUrl;



    @org.springframework.scheduling.annotation.Async
    public void sendVerificationEmail(String toEmail, String userName, String token) {
        String primaryUrl = getPrimaryClientUrl();
        String verificationLink = primaryUrl + "/verify-email?token=" + token; 
        sendEmail(toEmail, "FluxBanker - Welcome! Please verify your email",
                "Hello " + userName + ",\n\n" +
                        "Welcome to FluxBanker! Please verify your email by clicking the link below:\n" +
                        verificationLink + "\n\n" +
                        "Thank you for joining us!");
    }

    @org.springframework.scheduling.annotation.Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String primaryUrl = getPrimaryClientUrl();
        String resetLink = primaryUrl + "/reset-password?token=" + token;
        sendEmail(toEmail, "FluxBanker - Password Reset Request",
                "We received a request to reset your password.\n\n" +
                        "Click the link below to set a new password. This link can be accessed up to 3 times and is for one-time use:\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, please ignore this email.\n" +
                        "This link will expire in 1 hour.");
    }

    private String getPrimaryClientUrl() {
        if (clientUrl == null || clientUrl.isEmpty()) {
            return "http://localhost:5173";
        }
        return clientUrl.split(",")[0].trim();
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, toEmail, e.getMessage());
        }
    }
}
