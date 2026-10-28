package com.testforge.product.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.testforge.product.controller.ProductController.ProductRequest;
import com.testforge.product.entity.Product;
import com.testforge.product.exception.ProductNotFoundException;
import com.testforge.product.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "name", "price", "quantity");

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Product> list(
            int page,
            int size,
            String sortBy,
            String direction,
            String search) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }

        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sortBy field. Allowed fields: " + ALLOWED_SORT_FIELDS);
        }

        if (direction == null
                || (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException(
                    "Invalid sort direction. Use 'asc' or 'desc'");
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        String searchTerm = search == null ? "" : search.trim();

        return searchTerm.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(ProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.quantity());

        return repository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);

        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.quantity());

        return repository.save(product);
    }

    public void delete(Long id) {
        Product product = getById(id);
        repository.delete(product);
    }
}
