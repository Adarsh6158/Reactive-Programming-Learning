package com.reactive.programming.practice.reactiveProgramming;

import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.repository.ProductRepository;
import com.reactive.programming.practice.reactiveProgramming.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductServiceTests - Demonstrates how to test reactive Spring Boot services
 *
 * KEY CONCEPTS:
 * 1. @SpringBootTest - Loads full Spring context for integration testing
 * 2. @MockBean - Creates mock for ProductRepository
 * 3. @Autowired - Injects ProductService (uses mocked repository)
 * 4. StepVerifier - Tests Mono/Flux streams (essential for reactive testing)
 *
 * TESTING PATTERNS:
 * - Unit testing: Test service in isolation with mocked repository
 * - Use StepVerifier.create(mono/flux) to verify reactive streams
 * - Test both success and error scenarios
 * - Verify correct number of emissions and completion
 *
 * REACTIVE TESTING EXPLAINED:
 * Traditional test: result = service.getProduct(id)
 * Reactive test:
 *   StepVerifier.create(service.getProduct(id))
 *     .expectNext(expectedProduct)
 *     .expectComplete()
 *     .verify()
 *
 * WHY StepVerifier?
 * - Creates subscriber automatically
 * - Records all emissions
 * - Verifies sequence of events
 * - Supports timeout testing
 * - Verifies error handling
 */
@SpringBootTest
@DisplayName("Product Service Tests - Reactive Stream Testing")
class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    private Product testProduct;

    /**
     * SETUP: Runs before each test method
     * Initializes test data (Arrange phase)
     */
    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = new Product();
        testProduct.setId("507f1f77bcf86cd799439011");
        testProduct.setName("Laptop");
        testProduct.setPrice(new java.math.BigDecimal("999.99"));
        testProduct.setCategory("Electronics");
        testProduct.setDescription("Gaming Laptop");
    }

    // ==================== MONO TESTS (Single Value) ====================

    /**
     * TEST 1: Get Product By ID - Success Case
     *
     * SCENARIO: User requests product with valid ID
     * EXPECTED: Returns Mono containing the product
     *
     * STEP-BY-STEP:
     * 1. Mock repository to return product
     * 2. Call service method
     * 3. Use StepVerifier to:
     *    - Subscribe to the Mono
     *    - Verify next element matches product
     *    - Verify stream completes (no error)
     *    - Execute and block until verified
     */
    @Test
    @DisplayName("Should return product when ID exists")
    void testGetProductById_WithValidId_ReturnsProduct() {
        // Arrange: Mock repository behavior
        when(productRepository.findById("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.just(testProduct));

        // Act: Call service method
        Mono<Product> result = productService.getProductById("507f1f77bcf86cd799439011");

        // Assert: Verify reactive stream
        StepVerifier.create(result)
                .expectNext(testProduct)              // Verify first emission
                .expectComplete()                     // Verify stream completes
                .verify();                            // Execute and block

        // Verify mock was called exactly once
        verify(productRepository, times(1)).findById("507f1f77bcf86cd799439011");
    }

    /**
     * TEST 2: Get Product By ID - Not Found (Error Case)
     *
     * SCENARIO: User requests product with invalid ID
     * EXPECTED: Returns empty Mono (no element)
     *
     * TESTING ERROR SCENARIOS:
     * - Use .expectComplete() for empty Mono
     * - Use .expectError() for exceptions
     * - Verify correct error type if thrown
     */
    @Test
    @DisplayName("Should return empty Mono when product not found")
    void testGetProductById_WithInvalidId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById("invalid-id"))
                .thenReturn(Mono.empty());

        // Act
        Mono<Product> result = productService.getProductById("invalid-id");

        // Assert: Verify stream throws ProductNotFoundException
        StepVerifier.create(result)
                .expectError(com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException.class)
                .verify();

        verify(productRepository, times(1)).findById("invalid-id");
    }

    /**
     * TEST 3: Create Product - Success
     *
     * SCENARIO: User creates new product
     * EXPECTED: Returns Mono with created product (with generated ID)
     *
     * TESTING TRANSFORMATIONS:
     * - Mock save() to generate ID
     * - Verify returned product has ID
     * - Use .expectNextMatches() to verify with predicate
     */
    @Test
    @DisplayName("Should create product and return with ID")
    void testCreateProduct_WithValidData_ReturnsProductWithId() {
        // Arrange: Product without ID is passed, mock saves with ID
        Product newProduct = new Product();
        newProduct.setName("Phone");
        newProduct.setPrice(new java.math.BigDecimal("599.99"));

        Product savedProduct = new Product();
        savedProduct.setId("507f1f77bcf86cd799439012");  // Generated ID
        savedProduct.setName("Phone");
        savedProduct.setPrice(new java.math.BigDecimal("599.99"));

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // Act
        Mono<Product> result = productService.createProduct(newProduct);

        // Assert: Verify stream emits product with ID
        StepVerifier.create(result)
                .expectNextMatches(p ->
                        p.getId() != null &&
                        p.getId().equals("507f1f77bcf86cd799439012") &&
                        p.getName().equals("Phone")
                )
                .expectComplete()
                .verify();

        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * TEST 4: Update Product - Not Found
     *
     * SCENARIO: Update non-existent product
     * EXPECTED: Returns error/empty
     *
     * TESTING WITH ERRORS:
     * - Mock to return empty
     * - Use .expectError() to verify exception
     * - Or .expectComplete() for graceful empty
     */
    @Test
    @DisplayName("Should return empty when updating non-existent product")
    void testUpdateProduct_WithInvalidId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById("invalid-id"))
                .thenReturn(Mono.empty());

        // Act
        Mono<Product> result = productService.updateProduct("invalid-id", testProduct);

        // Assert: Verify error is thrown for non-existent product
        StepVerifier.create(result)
                .expectError(com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException.class)
                .verify();

        verify(productRepository, times(1)).findById("invalid-id");
        verify(productRepository, never()).save(any());  // save() never called
    }

    /**
     * TEST 5: Delete Product - Success
     *
     * SCENARIO: Delete existing product
     * EXPECTED: Returns Mono<Void> that completes successfully
     *
     * TESTING MONO<VOID>:
     * - No values emitted (void = empty)
     * - Only check completion
     * - Verify repository delete was called
     */
    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_WithValidId_ReturnsVoid() {
        // Arrange
        when(productRepository.existsById("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.just(true));
        when(productRepository.deleteById("507f1f77bcf86cd799439011"))
                .thenReturn(Mono.empty().then());

        // Act
        Mono<Void> result = productService.deleteProduct("507f1f77bcf86cd799439011");

        // Assert
        StepVerifier.create(result)
                .expectComplete()                     // Mono<Void> completes
                .verify();

        verify(productRepository, times(1)).existsById("507f1f77bcf86cd799439011");
        verify(productRepository, times(1)).deleteById("507f1f77bcf86cd799439011");
    }

    // ==================== FLUX TESTS (Multiple Values) ====================

    /**
     * TEST 6: Get All Products - Multiple Results
     *
     * SCENARIO: User requests all products
     * EXPECTED: Returns Flux with 3 products
     *
     * TESTING FLUX:
     * - Use .expectNext() multiple times
     * - Or use .expectNextCount(n) to verify count
     * - Verify .expectComplete() at end
     */
    @Test
    @DisplayName("Should return all products as Flux stream")
    void testGetAllProducts_WithData_ReturnsFlux() {
        // Arrange: Mock repository to return 3 products
        Product p1 = createProduct("1", "Laptop", 999);
        Product p2 = createProduct("2", "Phone", 599);
        Product p3 = createProduct("3", "Tablet", 399);

        when(productRepository.findAll())
                .thenReturn(Flux.just(p1, p2, p3));

        // Act
        Flux<Product> result = productService.getAllProducts();

        // Assert: Verify stream emits all 3 products in order
        StepVerifier.create(result)
                .expectNext(p1)                       // 1st emission
                .expectNext(p2)                       // 2nd emission
                .expectNext(p3)                       // 3rd emission
                .expectComplete()                     // Stream completes
                .verify();

        verify(productRepository, times(1)).findAll();
    }

    /**
     * TEST 7: Get All Products - Empty Database
     *
     * SCENARIO: Database is empty
     * EXPECTED: Returns empty Flux (no emissions)
     *
     * EDGE CASE TESTING:
     * - Important to test empty collections
     * - Verify stream completes without errors
     * - Verify no elements emitted
     */
    @Test
    @DisplayName("Should return empty Flux when no products exist")
    void testGetAllProducts_WithNoData_ReturnsEmptyFlux() {
        // Arrange
        when(productRepository.findAll())
                .thenReturn(Flux.empty());

        // Act
        Flux<Product> result = productService.getAllProducts();

        // Assert
        StepVerifier.create(result)
                .expectComplete()                     // No emissions, just complete
                .verify();

        verify(productRepository, times(1)).findAll();
    }

    /**
     * TEST 8: Get Products By Category - Filtering
     *
     * SCENARIO: Filter products by category
     * EXPECTED: Returns Flux with matching products only
     */
    @Test
    @DisplayName("Should return products filtered by category")
    void testGetProductsByCategory_WithValidCategory_ReturnsFiltered() {
        // Arrange
        Product electronics1 = createProduct("1", "Laptop", 999);
        electronics1.setCategory("Electronics");
        Product electronics2 = createProduct("2", "Phone", 599);
        electronics2.setCategory("Electronics");

        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Flux.just(electronics1, electronics2));

        // Act
        Flux<Product> result = productService.getProductsByCategory("Electronics");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)                   // Expect 2 items total
                .expectComplete()
                .verify();

        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    /**
     * TEST 9: Search Products - Partial Match
     *
     * SCENARIO: Search products by name pattern
     * EXPECTED: Returns matching products
     *
     * TESTING WITH PREDICATES:
     * - Use .expectNextMatches() to verify with condition
     * - Useful for partial verification
     */
    @Test
    @DisplayName("Should search and return matching products")
    void testSearchProducts_WithPattern_ReturnsMatching() {
        // Arrange
        Product laptop = createProduct("1", "Gaming Laptop", 999);
        Product stand = createProduct("2", "Laptop Stand", 49);

        when(productRepository.findByNamePattern("Laptop"))
                .thenReturn(Flux.just(laptop, stand));

        // Act
        Flux<Product> result = productService.searchProductsByName("Laptop");

        // Assert: Verify both products contain "Laptop" in name
        StepVerifier.create(result)
                .expectNextMatches(p -> p.getName().contains("Laptop"))
                .expectNextMatches(p -> p.getName().contains("Laptop"))
                .expectComplete()
                .verify();
    }

    // ==================== ERROR HANDLING TESTS ====================

    /**
     * TEST 10: Error Handling - Repository Throws Exception
     *
     * SCENARIO: Database query fails
     * EXPECTED: Service propagates error
     *
     * ERROR TESTING:
     * - Use .expectError() to verify exception occurs
     * - Use .expectError(ExceptionClass.class) for specific type
     * - Service should not swallow errors
     */
    @Test
    @DisplayName("Should propagate repository errors")
    void testGetProduct_RepositoryFails_PropagatesError() {
        // Arrange: Mock repository to throw exception
        when(productRepository.findById("test-id"))
                .thenReturn(Mono.error(new RuntimeException("DB Connection Failed")));

        // Act
        Mono<Product> result = productService.getProductById("test-id");

        // Assert: Verify error is propagated
        StepVerifier.create(result)
                .expectError(RuntimeException.class)  // Expect runtime exception
                .verify();

        verify(productRepository, times(1)).findById("test-id");
    }

    /**
     * TEST 11: Timeout Testing
     *
     * SCENARIO: Operation takes too long
     * EXPECTED: Times out after specified duration
     *
     * TIMEOUT TESTING:
     * - Use .expectTimeout() to verify timeout occurs
     * - Or use verify(Duration) to wait only specified time
     */
    @Test
    @DisplayName("Should timeout if operation takes too long")
    void testSlowOperation_ExceedsTimeout_TimesOut() {
        // Arrange: Create slow operation (10 second delay)
        Mono<Product> slowMono = Mono
                .delay(Duration.ofSeconds(10))
                .then(Mono.just(testProduct));

        // Act & Assert: Verify timeout after 1 second
        StepVerifier.create(slowMono)
                .expectSubscription()                 // Verify subscription happens
                .expectTimeout(Duration.ofSeconds(1)) // Verify timeout after 1 sec
                .verify(Duration.ofSeconds(2));       // Wait up to 2 sec for verification
    }

    // ==================== COUNT TESTS ====================

    /**
     * TEST 12: Count Products - Aggregation
     *
     * SCENARIO: Count total products in category
     * EXPECTED: Returns single number
     */
    @Test
    @DisplayName("Should count products in category")
    void testCountProductsByCategory_ReturnsCount() {
        // Arrange
        when(productRepository.countByCategory("Electronics"))
                .thenReturn(Mono.just(5L));

        // Act
        Mono<Long> result = productService.countProductsByCategory("Electronics");

        // Assert
        StepVerifier.create(result)
                .expectNext(5L)                       // Expect count of 5
                .expectComplete()
                .verify();

        verify(productRepository, times(1)).countByCategory("Electronics");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper: Create test product with minimal fields
     */
    private Product createProduct(String id, String name, double price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new java.math.BigDecimal(String.valueOf(price)));
        return product;
    }
}
