package com.reactive.programming.practice.reactiveProgramming.controller;

import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ProductController - Annotation-based REST controller for Product CRUD operations.
 * 
 * ========== ARCHITECTURAL PATTERNS ==========
 * 
 * ANNOTATION-BASED ROUTING:
 * - Uses @RestController, @RequestMapping, @GetMapping, etc.
 * - Most declarative approach for REST APIs
 * - Automatic discovery by Spring's component scanning
 * - Best for standard CRUD operations
 * 
 * VS FUNCTIONAL ROUTING:
 * - Uses RouterFunction and HandlerFunction beans
 * - More programmatic, supports complex routing logic
 * - Better for edge cases or non-standard patterns
 * - See ProductRouterConfig for functional routing example
 * 
 * ========== REACTIVE PATTERNS ==========
 * 
 * Return Types:
 * - Mono<T>: Single value or empty (0..1 items)
 *   Use for: Single resource fetch, create, update operations
 *   Example: getProductById returns Mono<Product>
 * 
 * - Flux<T>: Multiple values or stream (0..N items)
 *   Use for: Collection retrieval, streaming responses
 *   Example: getAllProducts returns Flux<Product>
 * 
 * - Mono<ResponseEntity<T>>: Mono with HTTP metadata
 *   Use for: Custom status codes, headers
 *   Example: Return 201 Created for POST operations
 * 
 * ========== OPENAPI ANNOTATIONS USED ==========
 * 
 * @Tag: Groups related endpoints in Swagger UI
 * @Operation: Describes an endpoint's purpose and behavior
 * @Parameter: Documents method parameters
 * @ApiResponse: Documents possible HTTP responses
 * @ApiResponses: Container for multiple @ApiResponse
 * @SecurityRequirement: Specifies required authentication
 * @Schema: Documents DTOs and their fields
 * 
 * ========== HTTP SEMANTICS ==========
 * 
 * HTTP Methods (Verbs):
 * - GET: Safe, idempotent - retrieve data without side effects
 * - POST: Non-idempotent - create new resource (201 Created)
 * - PUT: Idempotent - full resource replacement (200 OK)
 * - PATCH: Non-idempotent - partial resource update (200 OK)
 * - DELETE: Idempotent - remove resource (204 No Content)
 * 
 * Status Codes:
 * - 2xx: Success
 *   - 200: OK - standard successful response
 *   - 201: Created - new resource created (with Location header)
 *   - 204: No Content - successful but empty response body
 * - 4xx: Client Error
 *   - 400: Bad Request - invalid input data
 *   - 404: Not Found - resource doesn't exist
 *   - 409: Conflict - resource conflict (duplicate, etc)
 * - 5xx: Server Error
 *   - 500: Internal Server Error - unexpected server error
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Product Management",
    description = """
        Complete CRUD operations for product management using Spring WebFlux.
        
        Features:
        - Non-blocking reactive operations (Project Reactor)
        - Streaming responses for large datasets (Flux)
        - MongoDB integration with reactive driver
        - Comprehensive error handling
        - Full OpenAPI 3.0 documentation
        """
)
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * 
     * Retrieve all products with streaming response.
     * 
     * INTERVIEW POINTS:
     * - Flux: Used when response has multiple items
     * - Spring converts Flux to HTTP chunked transfer encoding
     * - Client receives items as they're emitted (streaming)
     * - Memory efficient for large datasets
     * - No need to load all data in memory at once
     * 
     * @return Flux<Product> streamed products
     */
    @GetMapping
    @Operation(
        summary = "Get all products",
        description = """
            Retrieves all products in the system with streaming response.
            
            Useful for:
            - Loading product catalog
            - Exporting all products
            - Real-time product feeds
            
            Performance: O(n) where n = number of products
            Memory: Streaming - constant regardless of product count
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved products",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Product.class)
        )
    )
    public Flux<Product> getAllProducts() {
        log.info("GET /api/v1/products - fetching all products");
        return productService.getAllProducts();
    }

    /**
     * GET /api/v1/products/{id}
     * 
     * Retrieve single product by ID.
     * 
     * INTERVIEW POINTS:
     * - Mono: Used when response has single item or empty
     * - ResponseEntity: Allows custom HTTP status + body
     * - map(): Transform Mono<Product> to Mono<ResponseEntity<Product>>
     * - onErrorResume(): Handle errors gracefully
     * 
     * @param id unique product identifier
     * @return Mono<ResponseEntity<Product>> found product or 404 Not Found
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = """
            Retrieves a single product by its unique identifier.
            
            Returns:
            - 200 OK with product if found
            - 404 Not Found if product doesn't exist
            
            Performance: O(1) for indexed ID lookup
            """
    )
    @Parameter(
        name = "id",
        description = "Unique product identifier (MongoDB ObjectId)",
        example = "507f1f77bcf86cd799439011",
        required = true
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Product.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found with given ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse")
            )
        )
    })
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable String id) {
        log.info("GET /api/v1/products/{} - fetching product", id);

        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .onErrorResume(error -> {
                    log.warn("Product not found: {}", id);
                    return Mono.empty();
                });
    }

    /**
     * POST /api/v1/products
     * 
     * Create new product.
     * 
     * INTERVIEW POINTS:
     * - POST: Non-idempotent method for creation
     * - @RequestBody: Deserializes JSON to Product object
     * - HTTP 201 Created: Standard response for resource creation
     * - Response includes created resource with generated ID
     * 
     * @param product the product data to create
     * @return Mono<ResponseEntity<Product>> created product with 201 status
     */
    @PostMapping
    @Operation(
        summary = "Create new product",
        description = """
            Creates a new product in the system.
            
            Request body should contain:
            - name: Product name (required)
            - description: Product description (required)
            - price: Product price (required)
            - category: Product category (optional)
            - status: Product status (default: ACTIVE)
            
            Returns:
            - 201 Created with the created product including generated ID
            - 400 Bad Request if validation fails
            
            Note: Server generates the ID. Include it in Location header.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Product.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse")
            )
        )
    })
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        log.info("POST /api/v1/products - creating new product");

        return productService.createProduct(product)
                .map(savedProduct -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(savedProduct));
    }

    /**
     * PUT /api/v1/products/{id}
     * 
     * Update existing product (full replacement).
     * 
     * INTERVIEW POINTS:
     * - PUT: Idempotent - same request produces same result
     * - Full replacement: Replaces entire resource
     * - PATCH: Would be used for partial updates
     * - Returns updated product with 200 OK
     * 
     * @param id product ID to update
     * @param productDetails complete new product data
     * @return Mono<ResponseEntity<Product>> updated product
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update product",
        description = """
            Performs full replacement of an existing product.
            
            PUT semantics:
            - Request body should contain complete product representation
            - All fields required (use PATCH for partial updates)
            - Operation is idempotent (repeating produces same result)
            
            Returns:
            - 200 OK with updated product
            - 404 Not Found if product doesn't exist
            - 400 Bad Request if validation fails
            """
    )
    @Parameter(
        name = "id",
        description = "Product ID to update",
        required = true
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Product.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data"
        )
    })
    public Mono<ResponseEntity<Product>> updateProduct(
            @PathVariable String id,
            @RequestBody Product productDetails) {
        log.info("PUT /api/v1/products/{} - updating product", id);

        return productService.updateProduct(id, productDetails)
                .map(updatedProduct -> ResponseEntity.ok(updatedProduct));
    }

    /**
     * PATCH /api/v1/products/{id}
     * 
     * Partially update product.
     * 
     * INTERVIEW POINTS:
     * - PATCH: Non-idempotent - modifies only provided fields
     * - Partial update: Only updates fields provided in request
     * - Difference from PUT:
     *   PUT = full replacement
     *   PATCH = apply delta (changes)
     * 
     * @param id product ID to update
     * @param productDetails fields to update
     * @return Mono<ResponseEntity<Product>> updated product
     */
    @PatchMapping("/{id}")
    @Operation(
        summary = "Partially update product",
        description = """
            Applies partial updates to an existing product.
            
            PATCH semantics:
            - Only provided fields are updated
            - Omitted fields retain their current values
            - Request body CAN be sparse (containing only changed fields)
            
            Use PATCH when:
            - Field updates are sparse/selective
            - Want to avoid full resource transmission
            - Need selective field modification
            
            Returns:
            - 200 OK with updated product
            - 404 Not Found if product doesn't exist
            """
    )
    @Parameter(
        name = "id",
        description = "Product ID to update",
        required = true
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product partially updated"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public Mono<ResponseEntity<Product>> partialUpdateProduct(
            @PathVariable String id,
            @RequestBody Product productDetails) {
        log.info("PATCH /api/v1/products/{} - partially updating product", id);

        return productService.updateProduct(id, productDetails)
                .map(updatedProduct -> ResponseEntity.ok(updatedProduct));
    }

    /**
     * DELETE /api/v1/products/{id}
     * 
     * Delete product by ID.
     * 
     * INTERVIEW POINTS:
     * - DELETE: Idempotent - same result for repeated requests
     * - Mono<Void>: Represents completion with no value
     * - 204 No Content: Standard response for successful deletion
     * - No response body: Client doesn't need response data
     * 
     * @param id product ID to delete
     * @return Mono<ResponseEntity<Void>> 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete product",
        description = """
            Deletes a product from the system.
            
            DELETE semantics:
            - Idempotent: Deleting same resource twice is safe
            - Returns 204 No Content (no response body)
            - Resources cannot be recovered after deletion
            
            Returns:
            - 204 No Content if deletion successful
            - 404 Not Found if product doesn't exist
            
            Note: Idempotent means:
            - DELETE /products/123 (first time) → 204
            - DELETE /products/123 (second time) → 404 (already deleted)
            - Both are safe to retry
            """
    )
    @Parameter(
        name = "id",
        description = "Product ID to delete",
        required = true
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        log.info("DELETE /api/v1/products/{} - deleting product", id);

        return productService.deleteProduct(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * GET /api/v1/products/category/{category}
     * 
     * Find products by category (filtered query).
     * 
     * INTERVIEW POINTS:
     * - Path-based filtering: Category in URL path
     * - Flux: Multiple results expected
     * - Database query: Should use index on category field
     * - Streaming response for many results
     * 
     * @param category category to filter by
     * @return Flux<Product> products in category
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = """
            Retrieves all products in a specific category.
            
            Query Pattern:
            - Category is part of URL path (/category/{category})
            - Returns streaming response (Flux)
            - Use this instead of query parameter when category is mandatory
            
            Performance: Uses indexed query on category field
            """
    )
    @Parameter(
        name = "category",
        description = "Product category filter",
        example = "Electronics",
        required = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "Products in category"
    )
    public Flux<Product> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/v1/products/category/{} - fetching products", category);

        return productService.getProductsByCategory(category);
    }

    /**
     * GET /api/v1/products/status/{status}
     * 
     * Find products by status.
     * 
     * @param status product status (ACTIVE, INACTIVE, etc)
     * @return Flux<Product> products with status
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get products by status",
        description = "Retrieves all products with a specific status"
    )
    @Parameter(
        name = "status",
        description = "Product status",
        example = "ACTIVE"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Products with status"
    )
    public Flux<Product> getProductsByStatus(@PathVariable String status) {
        log.info("GET /api/v1/products/status/{} - fetching products", status);

        return productService.getProductsByStatus(status);
    }

    /**
     * GET /api/v1/products/search?query=xyz
     * 
     * Search products by name pattern.
     * 
     * INTERVIEW POINTS:
     * - Query parameter: Optional search parameter after '?'
     * - @RequestParam: Extracts from query string
     * - Difference from @PathVariable:
     *   @PathVariable: Part of URL structure (/products/{id})
     *   @RequestParam: Optional query string (?query=abc&sort=name)
     * - Text search: Use regex or full-text search
     * 
     * @param query search pattern
     * @return Flux<Product> matching products
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search products by name",
        description = """
            Performs text search on product names.
            
            Query Parameter:
            - query: Search pattern (required)
            - Example: GET /api/v1/products/search?query=laptop
            
            Search Behavior:
            - Case-insensitive partial match
            - Uses regex pattern matching
            - Returns all matching products
            
            Performance: O(n) - linear search through products
            For large datasets, consider full-text search index
            """
    )
    @Parameter(
        name = "query",
        description = "Search pattern to match product names",
        example = "laptop",
        required = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "Search results"
    )
    public Flux<Product> searchProducts(@RequestParam String query) {
        log.info("GET /api/v1/products/search - searching with query: {}", query);

        return productService.searchProductsByName(query);
    }

    /**
     * GET /api/v1/products/count/category/{category}
     * 
     * Count products in category.
     * 
     * INTERVIEW POINTS:
     * - Aggregation query: Count operation
     * - Mono<Long>: Single numeric result
     * - Useful for pagination, statistics
     * - Returns 0 if category has no products
     * 
     * @param category category to count
     * @return Mono<Long> product count
     */
    @GetMapping("/count/category/{category}")
    @Operation(
        summary = "Count products in category",
        description = """
            Returns the count of products in a specific category.
            
            Use Cases:
            - Pagination: Know total count for page calculations
            - Statistics: Generate product statistics per category
            - Validation: Check if category exists (count > 0)
            
            Returns: Single numeric value (Mono<Long>)
            Empty category: Returns 0
            """
    )
    @Parameter(
        name = "category",
        description = "Category to count products in"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Product count",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(type = "integer", format = "int64", example = "42")
        )
    )
    public Mono<Long> countByCategory(@PathVariable String category) {
        log.info("GET /api/v1/products/count/category/{} - counting products", category);

        return productService.countProductsByCategory(category);
    }
}
