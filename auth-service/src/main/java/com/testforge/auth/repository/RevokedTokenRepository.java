package com.testforge.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.testforge.auth.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
}
