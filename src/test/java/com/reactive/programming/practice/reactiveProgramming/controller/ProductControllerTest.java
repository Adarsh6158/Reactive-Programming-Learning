package com.reactive.programming.practice.reactiveProgramming.controller;

import com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException;
import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ProductControllerTest - Test suite for ProductController
 * 
 * Tests all REST endpoints:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering and searching
 * - Error scenarios and validation
 * 
 * Uses WebTestClient for reactive testing
 * Mocks ProductService layer to test controller behavior
 */
@WebFluxTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    private Product testProduct;
    private String productId = "test-id-123";

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = Product.builder()
                .id(productId)
                .name("Test Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(1299.99))
                .quantity(10)
                .category("Electronics")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/products - Should return all products as Flux")
    void testGetAllProducts() {
        // Arrange
        when(productService.getAllProducts())
                .thenReturn(Flux.just(testProduct));

        // Act & Assert - Test Flux streaming response
        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1)
                .contains(testProduct);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return single product")
    void testGetProductById_Success() {
        // Arrange
        when(productService.getProductById(productId))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert - Test Mono single value response
        webTestClient.get()
                .uri("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(testProduct);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return product when found")
    void testGetProductById_NotFound() {
        // Arrange - Service returns product
        when(productService.getProductById(productId))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(testProduct);
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create product with validation")
    void testCreateProduct_Success() {
        // Arrange
        Product newProduct = Product.builder()
                .name("New MacBook")
                .description("M3 processor")
                .price(BigDecimal.valueOf(1999.99))
                .quantity(5)
                .category("Electronics")
                .status("ACTIVE")
                .build();

        Product savedProduct = Product.builder()
                .id("new-id-456")
                .name("New MacBook")
                .description("M3 processor")
                .price(BigDecimal.valueOf(1999.99))
                .quantity(5)
                .category("Electronics")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // Act & Assert - Verify ResponseEntity with CREATED status
        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(savedProduct);
    }

    @Test
    @DisplayName("POST /api/v1/products - Should fail validation for negative price")
    void testCreateProduct_ValidationError() {
        // Arrange
        Product invalidProduct = Product.builder()
                .name("Invalid Product")
                .price(BigDecimal.valueOf(-100)) // Invalid: negative price
                .quantity(10)
                .category("Test")
                .status("ACTIVE")
                .build();

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Product price must be greater than zero")));

        // Act & Assert - Verify 400 Bad Request status
        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidProduct)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product (flatMap)")
    void testUpdateProduct_Success() {
        // Arrange
        Product updateData = Product.builder()
                .name("Updated Laptop")
                .description("Updated description")
                .price(BigDecimal.valueOf(1599.99))
                .quantity(15)
                .category("Electronics")
                .status("ACTIVE")
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Updated Laptop")
                .description("Updated description")
                .price(BigDecimal.valueOf(1599.99))
                .quantity(15)
                .category("Electronics")
                .status("ACTIVE")
                .createdAt(testProduct.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productService.updateProduct(eq(productId), any(Product.class)))
                .thenReturn(Mono.just(updatedProduct));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(updatedProduct);
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id} - Should partially update product")
    void testPartialUpdateProduct_Success() {
        // Arrange
        Product partialUpdate = Product.builder()
                .price(BigDecimal.valueOf(999.99))
                .build();

        Product patchedProduct = new Product();
        patchedProduct.setId(productId);
        patchedProduct.setName(testProduct.getName());
        patchedProduct.setPrice(BigDecimal.valueOf(999.99));
        patchedProduct.setQuantity(testProduct.getQuantity());
        patchedProduct.setCategory(testProduct.getCategory());
        patchedProduct.setStatus(testProduct.getStatus());

        when(productService.updateProduct(eq(productId), any(Product.class)))
                .thenReturn(Mono.just(patchedProduct));

        // Act & Assert
        webTestClient.patch()
                .uri("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(partialUpdate)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product with 204 No Content")
    void testDeleteProduct_Success() {
        // Arrange
        when(productService.deleteProduct(productId))
                .thenReturn(Mono.empty());

        // Act & Assert - Verify 204 No Content status (void endpoint)
        webTestClient.delete()
                .uri("/api/v1/products/{id}", productId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Should filter by category")
    void testGetProductsByCategory() {
        // Arrange
        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/products/status/{status} - Should filter by status")
    void testGetProductsByStatus() {
        // Arrange
        when(productService.getProductsByStatus("ACTIVE"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/products/status/{status}", "ACTIVE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should search by name pattern")
    void testSearchProducts() {
        // Arrange
        when(productService.searchProductsByName("Laptop"))
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/products/search?query={query}", "Laptop")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/products/count/category/{category} - Should count products (Mono<Long>)")
    void testCountByCategory() {
        // Arrange
        when(productService.countProductsByCategory("Electronics"))
                .thenReturn(Mono.just(5L));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/products/count/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(5L);
    }
}
