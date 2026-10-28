package com.testforge.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import com.testforge.auth.entity.AppUser;
import com.testforge.auth.entity.PendingRegistration;
import com.testforge.auth.dto.UserRole;
import com.testforge.auth.repository.PendingRegistrationRepository;
import com.testforge.auth.repository.RevokedTokenRepository;
import com.testforge.auth.repository.UserRepository;
import com.testforge.auth.request.LoginRequest;
import com.testforge.auth.request.SignupRequest;
import com.testforge.auth.request.VerifyOtpRequest;
import com.testforge.auth.service.JwtService;
import com.testforge.auth.service.OtpService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthControllerTest {

    private UserRepository users;
    private RevokedTokenRepository revoked;
    private PendingRegistrationRepository pendingRegistrations;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private OtpService otpService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        revoked = mock(RevokedTokenRepository.class);
        pendingRegistrations = mock(PendingRegistrationRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwt = mock(JwtService.class);
        otpService = mock(OtpService.class);

        controller = new AuthController(
                users,
                revoked,
                pendingRegistrations,
                encoder,
                jwt,
                otpService,
                "SY,AF,IR"
        );
    }

    @Test
    void loginWithUnknownUsernameFails() {
        when(users.findByUsername("missing"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(
                "missing",
                "WrongPassword123!"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginWithIncorrectPasswordFails() {
        String encodedPassword = encoder.encode("CorrectPassword123!");

        AppUser user = new AppUser(
                "testuser",
                "test@example.com",
                encodedPassword,
                UserRole.STANDARD_USER
        );

        when(users.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest(
                "testuser",
                "WrongPassword123!"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void signupFromBlockedCountryIsRejected() {
        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "StrongPass123!",
                UserRole.STANDARD_USER,
                "IR"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.signup(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verifyNoInteractions(otpService);
        verify(pendingRegistrations, never()).save(any());
    }

    @Test
    void signupWithInvalidCountryCodeIsRejected() {
        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "StrongPass123!",
                UserRole.STANDARD_USER,
                "INVALID"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.signup(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void verifyOtpWithMissingRegistrationFails() {
        when(pendingRegistrations.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        VerifyOtpRequest request = new VerifyOtpRequest(
                "test@example.com",
                "123456"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.verifyOtp(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void expiredOtpIsRejected() {
        PendingRegistration pending = mock(PendingRegistration.class);

        when(pendingRegistrations.findByEmail("test@example.com"))
                .thenReturn(Optional.of(pending));

        when(pending.getExpiresAt())
                .thenReturn(Instant.now().minusSeconds(60));

        VerifyOtpRequest request = new VerifyOtpRequest(
                "test@example.com",
                "123456"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.verifyOtp(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(pendingRegistrations).deleteByEmail("test@example.com");
    }
}