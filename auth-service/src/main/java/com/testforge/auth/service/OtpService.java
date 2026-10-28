package com.testforge.auth.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a six-digit OTP.
     */
    public String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

    /**
     * Creates a SHA-256 hash of the email and OTP.
     * Store the hash rather than the plain-text OTP.
     */
    public String hashOtp(String email, String otp) {
        String value = email.trim().toLowerCase() + ":" + otp;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to hash OTP", exception
            );
        }
    }

    /**
     * Compares the submitted OTP with the stored hash.
     */
    public boolean matches(
            String email,
            String submittedOtp,
            String storedHash
    ) {
        if (submittedOtp == null || storedHash == null) {
            return false;
        }

        String submittedHash = hashOtp(email, submittedOtp);

        return MessageDigest.isEqual(
                submittedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * OTP validity period: five minutes.
     */
    public Instant getExpiryTime() {
        return Instant.now().plusSeconds(300);
    }
}