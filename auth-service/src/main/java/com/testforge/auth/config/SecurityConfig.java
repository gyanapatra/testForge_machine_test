package com.testforge.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testforge.auth.exception.ApiErrorResponse;
import com.testforge.auth.repository.RevokedTokenRepository;
import com.testforge.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            RevokedTokenRepository revokedTokens,
            ObjectMapper objectMapper) throws Exception {

        AuthenticationEntryPoint authenticationEntryPoint =
                (request, response, exception) -> writeError(
                        response,
                        request,
                        objectMapper,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Authentication is required to access this resource"
                );

        AccessDeniedHandler accessDeniedHandler =
                (request, response, exception) -> writeError(
                        response,
                        request,
                        objectMapper,
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "You do not have permission to perform this operation"
                );

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new JwtAuthFilter(jwtService, revokedTokens, objectMapper),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            ObjectMapper objectMapper,
            HttpStatus status,
            String error,
            String message) throws java.io.IOException {

        response.setStatus(status.value());
        response.setHeader(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE
        );

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                Map.of()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
