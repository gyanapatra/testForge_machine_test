package com.testforge.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @Test
    void generatedOtpContainsSixDigits() {
        String otp = otpService.generateOtp();

        assertNotNull(otp);
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void matchingOtpIsAccepted() {
        String email = "user@example.com";
        String otp = "123456";

        String hash = otpService.hashOtp(email, otp);

        assertTrue(otpService.matches(email, otp, hash));
    }

    @Test
    void incorrectOtpIsRejected() {
        String email = "user@example.com";
        String hash = otpService.hashOtp(email, "123456");

        assertFalse(
                otpService.matches(email, "654321", hash)
        );
    }

    @Test
    void otpHashIsConsistentForSameEmailAndOtp() {
        String first = otpService.hashOtp(
                "user@example.com", "123456");

        String second = otpService.hashOtp(
                "user@example.com", "123456");

        assertEquals(first, second);
    }

    @Test
    void otpExpiryIsApproximatelyFiveMinutes() {
        Instant before = Instant.now();

        Instant expiry = otpService.getExpiryTime();

        Instant expectedLatest = before.plusSeconds(300);
        Instant expectedEarliest = before.plusSeconds(299);

        assertFalse(expiry.isBefore(expectedEarliest));
        assertFalse(expiry.isAfter(expectedLatest));
    }
}