package com.testforge.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.testforge.product.controller.ProductController.ProductRequest;
import com.testforge.product.entity.Product;
import com.testforge.product.exception.ProductNotFoundException;
import com.testforge.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(
                "Laptop",
                "Test laptop",
                new BigDecimal("1200.00"),
                10
        );
    }

    @Test
    void listProductsReturnsPage() {
        var pageable = PageRequest.of(
                0, 10, Sort.by("name").ascending());

        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(product)));

        var result = productService.list(
                0, 10, "name", "asc", "");

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());

        verify(repository).findAll(pageable);
    }

    @Test
    void searchProductsUsesRepositorySearch() {
        var pageable = PageRequest.of(
                0, 10, Sort.by("name").ascending());

        when(repository.findByNameContainingIgnoreCase(
                "Laptop", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(product)));

        var result = productService.list(
                0, 10, "name", "asc", "Laptop");

        assertEquals(1, result.getTotalElements());

        verify(repository)
                .findByNameContainingIgnoreCase("Laptop", pageable);
    }

    @Test
    void createProductSavesProduct() {
        ProductRequest request = new ProductRequest(
                "Laptop",
                "Test laptop",
                new BigDecimal("1200.00"),
                10
        );

        when(repository.save(any(Product.class)))
                .thenReturn(product);

        Product result = productService.create(request);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());

        verify(repository).save(any(Product.class));
    }

    @Test
    void getExistingProductReturnsProduct() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
    }

    @Test
    void getMissingProductThrowsException() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getById(999L)
        );

        assertEquals(
                "Product not found with id: 999",
                exception.getMessage()
        );
    }

    @Test
    void invalidPageSizeThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.list(
                        0, 0, "name", "asc", "")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void invalidSortFieldThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.list(
                        0, 10, "invalidField", "asc", "")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void invalidSortDirectionThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.list(
                        0, 10, "name", "sideways", "")
        );

        verifyNoInteractions(repository);
    }
}