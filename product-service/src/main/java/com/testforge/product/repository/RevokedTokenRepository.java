package com.testforge.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.testforge.product.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
}
