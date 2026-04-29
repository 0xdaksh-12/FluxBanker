package com.fluxbanker.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Application-wide beans. Separate from SecurityConfig to avoid circular dependencies. */
@Configuration
public class AppConfig {

    /** BCrypt password encoder — equivalent to Node's bcrypt.genSalt() + bcrypt.hash(). */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
