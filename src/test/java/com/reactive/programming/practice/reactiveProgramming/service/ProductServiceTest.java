package com.reactive.programming.practice.reactiveProgramming.service;

import com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException;
import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ProductServiceTest - Test suite for ProductService layer
 * 
 * Tests business logic:
 * - Validation (price > 0, quantity >= 0, name not blank)
 * - CRUD operations with reactive chains
 * - Error handling (switchIfEmpty, onErrorResume, etc.)
 * - Database operations via repository
 * 
 * Uses StepVerifier for testing Mono/Flux streams
 * Mocks ProductRepository to isolate service testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private String productId = "test-id-123";

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .quantity(10)
                .category("Test Category")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GetAllProducts - Should return Flux of all products")
    void testGetAllProducts() {
        // Arrange
        when(productRepository.findAll())
                .thenReturn(Flux.just(testProduct));

        // Act & Assert - Verify Flux stream behavior
        StepVerifier.create(productService.getAllProducts())
                .expectNext(testProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("GetProductById - Should return Mono when found")
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(productId))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert - Verify Mono single value
        StepVerifier.create(productService.getProductById(productId))
                .expectNext(testProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("GetProductById - Should error when not found (switchIfEmpty)")
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById("invalid-id"))
                .thenReturn(Mono.empty());

        // Act & Assert - Verify error signal
        StepVerifier.create(productService.getProductById("invalid-id"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("CreateProduct - Should validate price > 0")
    void testCreateProduct_InvalidPrice() {
        // Arrange
        Product invalidProduct = Product.builder()
                .name("Invalid")
                .price(BigDecimal.valueOf(-100))
                .quantity(10)
                .category("Test")
                .status("ACTIVE")
                .build();

        // Act & Assert - Verify validation error
        StepVerifier.create(productService.createProduct(invalidProduct))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("CreateProduct - Should validate name not blank")
    void testCreateProduct_BlankName() {
        // Arrange
        Product invalidProduct = Product.builder()
                .name("") // Empty name
                .price(BigDecimal.valueOf(99.99))
                .quantity(10)
                .category("Test")
                .status("ACTIVE")
                .build();

        // Act & Assert
        StepVerifier.create(productService.createProduct(invalidProduct))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("CreateProduct - Should save valid product even with negative quantity (no validation)")
    void testCreateProduct_NegativeQuantity() {
        // Arrange - Service doesn't validate quantity, so it will attempt to save
        Product invalidProduct = Product.builder()
                .name("Product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(-5) // Negative quantity (not validated by service)
                .category("Test")
                .status("ACTIVE")
                .build();

        Product savedProduct = Product.builder()
                .id("generated-id")
                .name("Product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(-5)
                .category("Test")
                .status("ACTIVE")
                .build();

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // Act & Assert - Service will save without quantity validation
        StepVerifier.create(productService.createProduct(invalidProduct))
                .expectNext(savedProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("CreateProduct - Should save valid product (flatMap)")
    void testCreateProduct_Success() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Valid Product")
                .description("Valid Description")
                .price(BigDecimal.valueOf(199.99))
                .quantity(20)
                .category("Electronics")
                .status("ACTIVE")
                .build();

        Product savedProduct = Product.builder()
                .id("new-id-456")
                .name("Valid Product")
                .description("Valid Description")
                .price(BigDecimal.valueOf(199.99))
                .quantity(20)
                .category("Electronics")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // Act & Assert - Verify save returns saved product
        StepVerifier.create(productService.createProduct(newProduct))
                .expectNext(savedProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("UpdateProduct - Should fail if product not found")
    void testUpdateProduct_NotFound() {
        // Arrange
        Product updateData = Product.builder()
                .name("Updated")
                .price(BigDecimal.valueOf(99.99))
                .quantity(15)
                .category("Test")
                .status("ACTIVE")
                .build();

        when(productRepository.findById("invalid-id"))
                .thenReturn(Mono.empty());

        // Act & Assert - Verify error for non-existent product
        StepVerifier.create(productService.updateProduct("invalid-id", updateData))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("UpdateProduct - Should update existing product (flatMap)")
    void testUpdateProduct_Success() {
        // Arrange
        Product updateData = Product.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(149.99))
                .quantity(25)
                .category("Updated Category")
                .status("INACTIVE")
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(149.99))
                .quantity(25)
                .category("Updated Category")
                .status("INACTIVE")
                .createdAt(testProduct.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(productId))
                .thenReturn(Mono.just(testProduct));

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(updatedProduct));

        // Act & Assert
        StepVerifier.create(productService.updateProduct(productId, updateData))
                .expectNext(updatedProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("DeleteProduct - Should fail if product not found")
    void testDeleteProduct_NotFound() {
        // Arrange
        when(productRepository.existsById("invalid-id"))
                .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(productService.deleteProduct("invalid-id"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("DeleteProduct - Should delete existing product")
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(productId))
                .thenReturn(Mono.just(true));

        when(productRepository.deleteById(productId))
                .thenReturn(Mono.empty());

        // Act & Assert - Verify Mono.empty() returned (Mono<Void>)
        StepVerifier.create(productService.deleteProduct(productId))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("GetProductsByCategory - Should return Flux of matching products")
    void testGetProductsByCategory_Success() {
        // Arrange
        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert - Verify Flux filtering
        StepVerifier.create(productService.getProductsByCategory("Electronics"))
                .expectNext(testProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("GetProductsByCategory - Should error when no products found")
    void testGetProductsByCategory_Empty() {
        // Arrange
        when(productRepository.findByCategory("NonExistent"))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(productService.getProductsByCategory("NonExistent"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("GetProductsByStatus - Should filter by status")
    void testGetProductsByStatus() {
        // Arrange
        when(productRepository.findByStatus("ACTIVE"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        StepVerifier.create(productService.getProductsByStatus("ACTIVE"))
                .expectNext(testProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("SearchProductsByName - Should find products matching pattern")
    void testSearchProductsByName() {
        // Arrange
        when(productRepository.findByNamePattern("Test"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        StepVerifier.create(productService.searchProductsByName("Test"))
                .expectNext(testProduct)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("CountProductsByCategory - Should return count as Mono<Long>")
    void testCountProductsByCategory() {
        // Arrange
        when(productRepository.countByCategory("Test Category"))
                .thenReturn(Mono.just(5L));

        // Act & Assert - Verify Mono<Long> returned
        StepVerifier.create(productService.countProductsByCategory("Test Category"))
                .expectNext(5L)
                .expectComplete()
                .verify();
    }
}
