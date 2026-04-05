package com.reactive.programming.practice.reactiveProgramming.handler;

import com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException;
import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * ProductHandler - Part of Functional Routing approach.
 * Contains handler methods that process requests and generate responses.
 *
 * Key Points for Interviews:
 * - Functional Routing: alternative to @RestController for defining routes
 * - Handler methods accept ServerRequest and return Mono<ServerResponse>
 * - More programmatic and flexible than annotation-based routing
 * - Great for complex routing scenarios, middleware chains
 * - Paired with Router class that defines actual routes
 *
 * Comparison with Controllers:
 * - Controllers: Annotation-based, declarative, standard REST
 * - Handlers: Functional, explicit, powerful for complex scenarios
 * - This is the modern Spring WebFlux way after Spring 5.2+
 *
 * ServerRequest provides:
 * - pathVariable(name): extract path parameters
 * - queryParam(name): extract query parameters
 * - bodyToMono(class): deserialize request body
 * - bodyToFlux(class): deserialize request streaming body
 *
 * ServerResponse provides:
 * - ok(): 200 response
 * - created(URI): 201 with location header
 * - noContent(): 204 response
 * - status(int): custom status code
 * - body(Mono): set response body
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductHandler {

    private final ProductService productService;

    /**
     * Handle GET /products/functional
     * Retrieve all products via functional routing.
     *
     * ServerResponse.ok(): Sets HTTP 200 status
     * .contentType(MediaType.APPLICATION_JSON): Sets response content type
     * .body(flux, Product.class): Serializes Flux to JSON streaming response
     *
     * @param request ServerRequest containing request details
     * @return Mono<ServerResponse> with all products
     */
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        log.info("Handler: GET /products/functional - retrieving all products");

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.getAllProducts(), Product.class);
    }

    /**
     * Handle GET /products/functional/{id}
     * Retrieve single product by ID via functional routing.
     *
     * pathVariable("id"): Extracts {id} from URL path
     * flatMap: Chains async operations
     * onErrorResume: Handle ProductNotFoundException
     * map: Transform result to ServerResponse
     *
     * @param request ServerRequest containing ID in path
     * @return Mono<ServerResponse> with product or 404 error
     */
    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: GET /products/functional/{} - retrieving product", id);

        return productService.getProductById(id)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .onErrorResume(ProductNotFoundException.class, error -> {
                    log.warn("Product not found: {}", id);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * Handle POST /products/functional
     * Create new product via functional routing.
     *
     * bodyToMono(Product.class): Deserialize request JSON body to Product
     * flatMap: Chain with service call
     * created(URI): Return 201 Created with Location header pointing to new resource
     *
     * Interview Point: Shows full reactive chain with error handling.
     *
     * @param request ServerRequest containing product data in body
     * @return Mono<ServerResponse> with created product and 201 status
     */
    public Mono<ServerResponse> createProduct(ServerRequest request) {
        log.info("Handler: POST /products/functional - creating new product");

        return request.bodyToMono(Product.class)
                .flatMap(productService::createProduct)
                .flatMap(savedProduct ->
                        ServerResponse.created(URI.create("/products/functional/" + savedProduct.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedProduct))
                .onErrorResume(IllegalArgumentException.class, error -> {
                    log.warn("Invalid product data: {}", error.getMessage());
                    return ServerResponse.badRequest().build();
                });
    }

    /**
     * Handle PUT /products/functional/{id}
     * Update existing product via functional routing.
     *
     * Shows combining path variable extraction with request body deserialization.
     *
     * @param request ServerRequest with ID in path and product data in body
     * @return Mono<ServerResponse> with updated product
     */
    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: PUT /products/functional/{} - updating product", id);

        return request.bodyToMono(Product.class)
                .flatMap(productDetails -> productService.updateProduct(id, productDetails))
                .flatMap(updatedProduct -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedProduct))
                .onErrorResume(ProductNotFoundException.class, error -> {
                    log.warn("Product not found during update: {}", id);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * Handle DELETE /products/functional/{id}
     * Delete product by ID via functional routing.
     *
     * Uses then() to chain Mono after deletion completes.
     * Returns 204 No Content on success.
     *
     * @param request ServerRequest with product ID
     * @return Mono<ServerResponse> with 204 No Content
     */
    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: DELETE /products/functional/{} - deleting product", id);

        return productService.deleteProduct(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(ProductNotFoundException.class, error -> {
                    log.warn("Product not found for deletion: {}", id);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * Handle GET /products/functional/category/{category}
     * Find products by category via functional routing.
     *
     * @param request ServerRequest with category in path
     * @return Mono<ServerResponse> with product flux
     */
    public Mono<ServerResponse> getProductsByCategory(ServerRequest request) {
        String category = request.pathVariable("category");
        log.info("Handler: GET /products/functional/category/{} - fetching products", category);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.getProductsByCategory(category), Product.class)
                .onErrorResume(ProductNotFoundException.class, error -> {
                    log.warn("No products found in category: {}", category);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * Handle GET /products/functional/search?query=xyz
     * Search products by name pattern via functional routing.
     *
     * queryParam(name): Extracts query parameter from URL
     * Returns either search results or empty Mono if no query provided
     *
     * @param request ServerRequest with query parameter
     * @return Mono<ServerResponse> with matching products
     */
    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        return request.queryParam("query")
                .map(query -> {
                    log.info("Handler: GET /products/functional/search - searching with query: {}", query);

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(productService.searchProductsByName(query), Product.class);
                })
                .orElseGet(() -> {
                    log.warn("Search query parameter missing");
                    return ServerResponse.badRequest().build();
                });
    }
}
