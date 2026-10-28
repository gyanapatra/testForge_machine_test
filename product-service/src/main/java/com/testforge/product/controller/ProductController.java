package com.testforge.product.controller;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.testforge.product.entity.Product;
import com.testforge.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<Product> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @RequestParam(name = "search", defaultValue = "") String search) {

        return productService.list(page, size, sortBy, direction, search);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable("id") Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public Product update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request) {

        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        productService.delete(id);
    }

    public record ProductRequest(
            @NotBlank(message = "Product name is required")
            @Size(max = 100, message = "Product name cannot exceed 100 characters")
            String name,

            @Size(max = 1000, message = "Description cannot exceed 1000 characters")
            String description,

            @NotNull(message = "Product price is required")
            @DecimalMin(value = "0.0", message = "Price cannot be negative")
            BigDecimal price,

            @Min(value = 0, message = "Quantity cannot be negative")
            int quantity) {
    }
}