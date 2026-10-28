package com.testforge.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens", indexes = @Index(name = "idx_revoked_jti", columnList = "jti", unique = true))
public class RevokedToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String jti;
    @Column(nullable = false)
    private Instant expiresAt;
    protected RevokedToken() {}
    public RevokedToken(String jti, Instant expiresAt) { this.jti = jti; this.expiresAt = expiresAt; }
}
