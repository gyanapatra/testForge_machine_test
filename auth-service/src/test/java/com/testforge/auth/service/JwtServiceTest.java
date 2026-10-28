package com.testforge.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import com.testforge.auth.dto.UserRole;
import com.testforge.auth.entity.AppUser;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    // At least 32 bytes for an HS256-compatible HMAC key.
    private static final String SECRET =
            "testforge-unit-test-secret-key-123456789012";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000);
    }

    @Test
    void createTokenAndParseReturnsExpectedClaims() {
        AppUser user = new AppUser(
                "testuser",
                "test@example.com",
                "encoded-password",
                UserRole.STANDARD_USER
        );

        /*
         * This test needs a valid user role because JwtService uses
         * user.getRole().name(). See the note below before running.
         */
        String token = jwtService.createToken(user);

        Claims claims = jwtService.parse(token);

        assertEquals("testuser", claims.getSubject());
        assertNotNull(claims.getId());
        assertNotNull(claims.get("role"));
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void parsingInvalidTokenThrowsException() {
        assertThrows(
                Exception.class,
                () -> jwtService.parse("not-a-valid-jwt")
        );
    }

    @Test
    void parsingTokenSignedWithAnotherSecretFails() {
        AppUser user = new AppUser(
                "testuser",
                "test@example.com",
                "encoded-password",
                UserRole.STANDARD_USER
        );

        String token = jwtService.createToken(user);

        JwtService differentService = new JwtService(
                "another-test-secret-key-123456789012",
                3600000
        );

        assertThrows(
                Exception.class,
                () -> differentService.parse(token)
        );
    }
}