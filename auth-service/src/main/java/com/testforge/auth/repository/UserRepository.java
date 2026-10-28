package com.testforge.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.testforge.auth.entity.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
