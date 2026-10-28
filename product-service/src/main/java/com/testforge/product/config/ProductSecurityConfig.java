package com.testforge.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.testforge.product.repository.RevokedTokenRepository;
import com.testforge.product.service.JwtService;

@Configuration
public class ProductSecurityConfig {
    @Bean
    SecurityFilterChain productSecurity(HttpSecurity http, JwtService jwtService,
                                        RevokedTokenRepository revokedTokens) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
            		.requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs",
                            "/v3/api-docs/**"
                    ).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtService, revokedTokens), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
