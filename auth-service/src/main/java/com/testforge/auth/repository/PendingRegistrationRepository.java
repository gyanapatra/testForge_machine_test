package com.testforge.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.testforge.auth.entity.PendingRegistration;

import java.util.Optional;

public interface PendingRegistrationRepository
        extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Transactional
    void deleteByEmail(String email);
}
