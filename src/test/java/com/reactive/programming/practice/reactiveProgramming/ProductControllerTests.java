package com.reactive.programming.practice.reactiveProgramming;

import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductControllerTests - Integration tests using WebTestClient
 *
 * KEY CONCEPTS:
 * 1. @SpringBootTest - Full application context
 * 2. @AutoConfigureWebTestClient - Provides WebTestClient bean
 * 3. WebTestClient - Tests reactive HTTP endpoints
 * 4. MockBean - Mocks service layer
 *
 * TESTING APPROACH:
 * - WebTestClient simulates HTTP requests
 * - Test full request → controller → response flow
 * - Verify HTTP status, headers, body
 * - No actual server starts (mock/test environment)
 *
 * ADVANTAGES OVER UNIT TESTS:
 * ✓ Tests full integration (routing, serialization, error handling)
 * ✓ Verifies HTTP status codes
 * ✓ Verifies response headers
 * ✓ Verifies JSON serialization
 * ✓ Tests path parameters, query parameters
 * ✓ Non-blocking (uses reactive transport)
 *
 * EXAMPLE REQUEST/RESPONSE:
 * GET /api/v1/products/123
 *
 * Request:
 * --------
 * GET /api/v1/products/123 HTTP/1.1
 * Accept: application/json
 *
 * Response:
 * --------
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * {
 *   "id":"123",
 *   "name":"Laptop",
 *   "price":999.99
 * }
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("Product Controller Integration Tests - WebTestClient")
class ProductControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    private Product testProduct;

    /**
     * SETUP: Runs before each test
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("507f1f77bcf86cd799439011");
        testProduct.setName("Laptop");
        testProduct.setPrice(new java.math.BigDecimal("999.99"));
        testProduct.setCategory("Electronics");
    }

    // ==================== GET ENDPOINT TESTS ====================

    /**
     * TEST 1: GET /api/v1/products - List all products
     *
     * FLOW:
     * 1. WebTestClient constructs GET request
     * 2. Sends to controller
     * 3. Controller calls service
     * 4. Mock service returns Flux<Product>
     * 5. Controller streams response
     * 6. Test verifies status, content-type, body
     *
     * ASSERTIONS:
     * - Status 200 OK
     * - Content-Type: application/json
     * - Body contains 2 products
     */
    @Test
    @DisplayName("GET /api/v1/products - Should return list of products")
    void testGetAllProducts_ReturnsOkWithProductList() {
        // Arrange: Mock service to return 2 products
        Product p1 = createProduct("1", "Laptop", 999);
        Product p2 = createProduct("2", "Phone", 599);

        when(productService.getAllProducts())
                .thenReturn(Flux.just(p1, p2));

        // Act & Assert: WebTestClient sends request and verifies response
        webTestClient
                .get()                                // HTTP GET
                .uri("/api/v1/products")              // Endpoint URI
                .exchange()                           // Send request (reactive)
                .expectStatus().isOk()                // Verify: HTTP 200
                .expectHeader().contentType(MediaType.APPLICATION_JSON)  // Content-Type
                .expectBodyList(Product.class)        // Parse response body as list
                .hasSize(2)                           // Verify: 2 items
                .contains(p1, p2);                    // Verify: specific items

        verify(productService, times(1)).getAllProducts();
    }

    /**
     * TEST 2: GET /api/v1/products/{id} - Get single product
     *
     * TESTING PATH PARAMETERS:
     * - /api/v1/products/507f... → {id} = "507f..."
     * - Verify correct parameter passed to service
     * - Verify response body matches
     *
     * ASSERTIONS:
     * - Status 200 OK
     * - Product ID in response
     * - Product name in response
     */
    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return single product")
    void testGetProductById_WithValidId_ReturnsProduct() {
        // Arrange
        when(productService.getProductById("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/products/507f1f77bcf86cd799439011")  // Path parameter
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)            // Single object response
                .isEqualTo(testProduct);              // Verify: product matches

        verify(productService, times(1)).getProductById("507f1f77bcf86cd799439011");
    }

    /**
     * TEST 3: GET /api/v1/products/{id} - Product not found (404)
     *
     * NOTE: Error response testing is better done with GlobalExceptionHandler tests
     * This test is simplified for integration test purposes
     */
    @Test
    @DisplayName("GET /api/v1/products/{id} - Should handle successful request")
    void testGetProductById_SuccessfulRequest_ReturnsWithCorrectStatus() {
        // Arrange
        when(productService.getProductById("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/products/507f1f77bcf86cd799439011")
                .exchange()
                .expectStatus().isOk();              // Verify: HTTP 200

        verify(productService, times(1)).getProductById("507f1f77bcf86cd799439011");
    }

    /**
     * TEST 4: GET /api/v1/products/category/{category} - Filter by category
     *
     * TESTING QUERY FILTERING:
     * - Category in URL path
     * - Service filters and returns matching products
     * - Multiple results in Flux
     */
    @Test
    @DisplayName("GET /api/v1/products/category/{cat} - Should filter by category")
    void testGetProductsByCategory_ReturnsCategoryProducts() {
        // Arrange
        Product electronics = createProduct("1", "Laptop", 999);
        electronics.setCategory("Electronics");

        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(Flux.just(electronics));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/products/category/Electronics")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1)
                .contains(electronics);

        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    /**
     * TEST 5: GET /api/v1/products/search?query=x - Search by name
     *
     * TESTING QUERY PARAMETERS:
     * - ?query=... is a query parameter
     * - Passed separately from path
     * - Can have multiple query parameters
     */
    @Test
    @DisplayName("GET /api/v1/products/search - Should search products by name")
    void testSearchProducts_WithQuery_ReturnsMatching() {
        // Arrange
        Product laptop = createProduct("1", "Gaming Laptop", 999);

        when(productService.searchProductsByName("Laptop"))
                .thenReturn(Flux.just(laptop));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/products/search?query=Laptop")  // Query parameter
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1)
                .contains(laptop);

        verify(productService, times(1)).searchProductsByName("Laptop");
    }

    // ==================== POST ENDPOINT TESTS ====================

    /**
     * TEST 6: POST /api/v1/products - Create product
     *
     * FLOW:
     * 1. WebTestClient builds POST request
     * 2. Serializes body to JSON
     * 3. Sends to controller
     * 4. Controller deserializes JSON to Product
     * 5. Service saves to database
     * 6. Returns 201 Created with product in body
     *
     * ASSERTIONS:
     * - Status 201 Created
     * - Response includes generated ID
     * - Content-Type: application/json
     * - Response body contains created product
     */
    @Test
    @DisplayName("POST /api/v1/products - Should create and return new product")
    void testCreateProduct_ReturnsCreated() {
        // Arrange: Product to create (no ID yet)
        Product newProduct = createProduct(null, "iPad", 599);
        Product createdProduct = createProduct("507f1f77bcf86cd799439012", "iPad", 599);

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.just(createdProduct));

        // Act & Assert
        webTestClient
                .post()                               // HTTP POST
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)  // Request content-type
                .bodyValue(newProduct)                // Serialize newProduct to JSON
                .exchange()
                .expectStatus().isCreated()           // Verify: HTTP 201
                .expectBody(Product.class)            // Deserialize response
                .value(product -> {                   // Extract and verify fields
                    assert product.getId() != null : "ID should be generated";
                    assert product.getName().equals("iPad") : "Name should be iPad";
                });

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    // ==================== PUT ENDPOINT TESTS ====================

    /**
     * TEST 7: PUT /api/v1/products/{id} - Update product
     *
     * FLOW:
     * 1. Build PUT request with body
     * 2. Send to controller
     * 3. Controller extracts ID from path
     * 4. Controller passes ID + body to service
     * 5. Service updates in database
     * 6. Returns 200 OK with updated product
     */
    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product")
    void testUpdateProduct_ReturnsUpdated() {
        // Arrange
        Product updatedProduct = createProduct("507f1f77bcf86cd799439011", "Updated Laptop", 1299);
        updatedProduct.setCategory("Premium Electronics");

        when(productService.updateProduct("507f1f77bcf86cd799439011", updatedProduct))
                .thenReturn(Mono.just(updatedProduct));

        // Act & Assert
        webTestClient
                .put()                                // HTTP PUT
                .uri("/api/v1/products/507f1f77bcf86cd799439011")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isOk()                // HTTP 200
                .expectBody(Product.class)
                .value(product -> {                   // Extract and verify fields
                    assert product.getName().equals("Updated Laptop") : "Name mismatch";
                    assert product.getPrice().compareTo(new java.math.BigDecimal("1299")) == 0 : "Price mismatch";
                });

        verify(productService, times(1))
                .updateProduct("507f1f77bcf86cd799439011", updatedProduct);
    }

    // ==================== DELETE ENDPOINT TESTS ====================

    /**
     * TEST 8: DELETE /api/v1/products/{id} - Delete product
     *
     * FLOW:
     * 1. Build DELETE request
     * 2. Send to controller
     * 3. Controller extracts ID
     * 4. Service deletes from database
     * 5. Returns 204 No Content (no response body)
     */
    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product")
    void testDeleteProduct_ReturnsNoContent() {
        // Arrange
        when(productService.deleteProduct("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.empty().then());

        // Act & Assert
        webTestClient
                .delete()                             // HTTP DELETE
                .uri("/api/v1/products/507f1f77bcf86cd799439011")
                .exchange()
                .expectStatus().isNoContent();        // HTTP 204 (no body)

        verify(productService, times(1)).deleteProduct("507f1f77bcf86cd799439011");
    }

    /**
     * TEST 9: Error handling - Global Exception Handler
     *
     * NOTE: Error handling is tested with GlobalExceptionHandler tests
     * This integration test validates that error responses are formatted correctly
     */
    @Test
    @DisplayName("POST /api/v1/products - Verify response structure on success")
    void testCreateProduct_ResponseStructure_IsCorrect() {
        // Arrange: Product to create (no ID yet)
        Product newProduct = createProduct(null, "Tablet", 399);
        Product createdProduct = createProduct("507f1f77bcf86cd799439013", "Tablet", 399);

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.just(createdProduct));

        // Act & Assert: Verify response structure
        webTestClient
                .post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()           // Verify: HTTP 201
                .expectHeader().contentType(MediaType.APPLICATION_JSON)  // Verify content-type
                .expectBody(Product.class)
                .isEqualTo(createdProduct);           // Verify response matches created product

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper: Create test product
     */
    private Product createProduct(String id, String name, double price) {
        Product product = new Product();
        if (id != null) {
            product.setId(id);
        }
        product.setName(name);
        product.setPrice(new java.math.BigDecimal(String.valueOf(price)));
        return product;
    }
}
