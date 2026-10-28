package com.testforge.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testforge.auth.exception.ApiErrorResponse;
import com.testforge.auth.repository.RevokedTokenRepository;
import com.testforge.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokens;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(
            JwtService jwtService,
            RevokedTokenRepository revokedTokens,
            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.revokedTokens = revokedTokens;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));

                if (revokedTokens.existsByJti(claims.getId())) {
                    writeUnauthorized(
                            response,
                            request,
                            "Token has been logged out"
                    );
                    return;
                }

                String role = claims.get("role", String.class);

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_" + role))
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (Exception ex) {
                SecurityContextHolder.clearContext();

                writeUnauthorized(
                        response,
                        request,
                        "Invalid or expired token"
                );
                return;
            }
        }

        chain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register");
    }


    private void writeUnauthorized(
            HttpServletResponse response,
            HttpServletRequest request,
            String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                message,
                request.getRequestURI(),
                Map.of()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
