package com.reactive.programming.practice.reactiveProgramming.config;

import com.reactive.programming.practice.reactiveProgramming.handler.ProductHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFluxConfig - Configuration for Spring WebFlux functional routing.
 * Defines routes and connects them to handler methods.
 *
 * Key Points for Interviews:
 * - Functional routing is an alternative to @RestController with @RequestMapping
 * - More explicit and flexible - you control the routing logic completely
 * - RouterFunction defines HTTP route rules
 * - Routes are composed using:
 *   * GET, POST, PUT, DELETE: HTTP methods
 *   * path("/pattern"): URL path matching
 *   * accept(MediaType): Content-Type filtering
 * - Handlers process matching requests and return ServerResponse
 *
 * When to Use:
 * - Annotation-based: standard REST endpoints (simpler, more discoverable)
 * - Functional: complex routing, middleware chains, API gateway patterns
 *
 * This configuration creates parallel routes to ProductController:
 * - /api/v1/products/** via annotation-based controller
 * - /api/v1/products-functional/** via functional routing
 *
 * Both demonstrate the same operations, showing both approaches.
 */
@Configuration
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {

    private final ProductHandler productHandler;

    /**
     * Configure CORS to allow requests from other origins.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Define routes for product operations using functional routing.
     * This RouterFunction replaces traditional @RequestMapping annotations.
     *
     * Route Composition Pattern:
     * 1. Specify HTTP method and path predicate
     * 2. Specify accept() for content negotiation (optional)
     * 3. Route to handler method
     * 4. Chain multiple routes with .and().route()
     *
     * Interview Explanation:
     * - RequestPredicates.GET/POST/PUT/DELETE: HTTP method matchers
     * - RequestPredicates.path("/pattern"): URL path matchers
     * - RequestPredicates.accept(MediaType.APPLICATION_JSON): Content-Type matcher
     * - RouterFunctions.route() chains multiple routes together
     *
     * Benefits:
     * - Explicit routing definition
     * - Full control over routing logic
     * - Easy to compose middleware
     * - Supports complex patterns
     *
     * @return RouterFunction mapping routes to handlers
     */
    @Bean
    public RouterFunction<ServerResponse> productRoutes() {
        return RouterFunctions.route()
                // GET /api/v1/products-functional - Get all products
                .route(
                        RequestPredicates.GET("/api/v1/products-functional")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        productHandler::getAllProducts
                )
                // GET /api/v1/products-functional/{id} - Get product by ID
                .route(
                        RequestPredicates.GET("/api/v1/products-functional/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        productHandler::getProductById
                )
                // POST /api/v1/products-functional - Create new product
                .route(
                        RequestPredicates.POST("/api/v1/products-functional")
                                .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
                        productHandler::createProduct
                )
                // PUT /api/v1/products-functional/{id} - Update product
                .route(
                        RequestPredicates.PUT("/api/v1/products-functional/{id}")
                                .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
                        productHandler::updateProduct
                )
                // DELETE /api/v1/products-functional/{id} - Delete product
                .route(
                        RequestPredicates.DELETE("/api/v1/products-functional/{id}"),
                        productHandler::deleteProduct
                )
                // GET /api/v1/products-functional/category/{category} - Get by category
                .route(
                        RequestPredicates.GET("/api/v1/products-functional/category/{category}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        productHandler::getProductsByCategory
                )
                // GET /api/v1/products-functional/search - Search products
                .route(
                        RequestPredicates.GET("/api/v1/products-functional/search")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        productHandler::searchProducts
                )
                .build();
    }
}
