package com.infy.newgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) {
        return http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints (No Token Required)
                        .requestMatchers(
                                "/otp/**",
                                "/agent/login",
                                "/agent/register",
                                "/customer/login",
                                "/customer/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/agent/check/**",
                                "/profile/reset-password")
                        .permitAll()
                        // Protected Endpoints
                        .requestMatchers("/agent/dashboard/**").hasAuthority("ROLE_AGENT")
                        .requestMatchers("/customer/policies/**", "/policies/**").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers("/profile/fetch", "/profile/update")
                        .hasAnyAuthority("ROLE_AGENT", "ROLE_CUSTOMER")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }
}