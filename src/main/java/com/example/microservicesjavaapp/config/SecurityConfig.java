package com.example.microservicesjavaapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Spring Security.
 * This class enables web security and defines how HTTP requests are secured.
 * It sets up basic authentication and an in-memory user store for simplicity.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web security features
public class SecurityConfig {

    /**
     * Configures the security filter chain, defining authorization rules for HTTP requests.
     *
     * @param http The HttpSecurity object to configure.
     * @return A SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated() // All requests require authentication
                )
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in a REST API (consider enabling in production)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                .httpBasic(httpBasic -> httpBasic.init(http)); // Enable HTTP Basic authentication

        return http.build();
    }

    /**
     * Configures an in-memory user details service.
     * In a real application, this would typically fetch user details from a database.
     *
     * @return A UserDetailsService instance.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Define a single user for basic authentication
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password")) // Encode the password
                .roles("USER") // Assign a role
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Provides a password encoder for encoding user passwords.
     * BCryptPasswordEncoder is recommended for strong password hashing.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
