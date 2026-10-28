package com.testforge.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

import com.testforge.auth.dto.UserRole;

@Entity
@Table(
    name = "pending_registrations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_pending_registration_email",
            columnNames = "email"
        )
    }
)
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false, length = 64)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts;

    protected PendingRegistration() {
    }

    public PendingRegistration(
            String username,
            String email,
            String passwordHash,
            UserRole role,
            String otpHash,
            Instant expiresAt
    ) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.attempts = 0;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}