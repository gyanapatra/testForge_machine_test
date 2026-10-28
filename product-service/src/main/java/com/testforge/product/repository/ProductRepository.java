package com.testforge.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.testforge.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Page<Product> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
