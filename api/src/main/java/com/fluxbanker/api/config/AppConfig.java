package com.fluxbanker.api.config;

import com.fluxbanker.api.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Application-wide beans. Separate from SecurityConfig to avoid circular
 * dependencies.
 */
@Configuration
public class AppConfig {

    /**
     * BCrypt password encoder — equivalent to Node's bcrypt.genSalt() +
     * bcrypt.hash().
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Custom UserDetailsService to fetch users from database for Spring Security.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
