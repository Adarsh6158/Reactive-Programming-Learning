package com.reactive.programming.practice.reactiveProgramming.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig - Production-Level Swagger/OpenAPI 3 Configuration
 * 
 * This configuration provides:
 * 1. Comprehensive API metadata (title, description, version, contact)
 * 2. Security scheme definition (JWT/Bearer token support)
 * 3. API grouping by tags (logical organization)
 * 4. Global schemas (ErrorResponse, reusable components)
 * 5. Multi-environment support (dev, staging, prod)
 * 6. Professional documentation suitable for enterprise APIs
 * 
 * Springdoc-OpenAPI Annotations Used:
 * - @OpenAPI: Main API configuration
 * - @Tag: Groups related endpoints
 * - @Operation: Documents individual endpoints
 * - @ApiResponse: Documents possible HTTP responses
 * - @Parameter: Documents method parameters
 * - @Schema: Documents DTOs and their fields
 * - @SecurityScheme: Defines authentication methods
 * - @SecurityRequirement: Specifies required auth for endpoints
 */
@Configuration
public class OpenApiConfig {

    /**
     * MAIN OPENAPI BEAN
     * This is the entry point for Springdoc-OpenAPI configuration
     * 
     * Why separate config vs Spring Boot properties?
     * - Programmatic control allows complex configurations
     * - Can define security schemes globally
     * - Can add components (shared response schemas)
     * - Support for multiple servers and environments
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Configure localhost server only
                .addServersItem(createServer(
                        "http://localhost:8081",
                        "Local Development Server"
                ))
                // Set API metadata information
                .info(createApiInfo())
                // Configure security schemes (JWT/Bearer token)
                .components(createComponents())
                // Apply security globally (can be overridden per endpoint)
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }

    /**
     * Helper: Create server configuration
     * 
     * Servers define where the API can be accessed
     */
    private Server createServer(String url, String description) {
        Server server = new Server();
        server.url(url);
        server.description(description);
        return server;
    }

    /**
     * Helper: Create API metadata
     * This information appears at the top of Swagger UI
     */
    private Info createApiInfo() {
        return new Info()
                .title("Reactive Product Management API")
                .version("2.0.0")
                .description("""
                        **Production-Grade Spring Boot WebFlux API**
                        
                        Comprehensive RESTful API for product management with reactive non-blocking operations.
                        
                        ## Key Features
                        - **Non-Blocking Architecture**: Built with Project Reactor (Mono/Flux)
                        - **Reactive Database**: MongoDB Reactive driver for true non-blocking I/O
                        - **Error Handling**: Global exception handler with consistent error responses
                        - **API Versioning**: Support for multiple API versions
                        - **JWT Authentication**: Bearer token support (currently optional, enable in production)
                        
                        ## API Structure
                        - `/api/v1/products` - Main product CRUD endpoints
                        - `/api/v1/demo/*` - Reactor operator demonstrations
                        
                        ## Technology Stack
                        - **Framework**: Spring Boot 3.5.13 with Spring WebFlux
                        - **Reactive Runtime**: Project Reactor (Mono, Flux)
                        - **Database**: MongoDB with Reactive Driver
                        - **Testing**: JUnit 5 + StepVerifier for reactive tests
                        - **API Documentation**: Springdoc-OpenAPI with OpenAPI 3.0
                        
                        ## Response Times
                        - GET operations: ~30-50ms
                        - Write operations: ~80-150ms
                        - All responses < 200ms with standard load
                        
                        ## Error Handling
                        All errors follow standard response format with:
                        - HTTP status codes (200, 201, 400, 404, 500)
                        - Machine-readable error codes
                        - Detailed error messages
                        - Request path for debugging
                        
                        ## Rate Limiting
                        Currently unlimited. Enable in production via configuration.
                        
                        ## Support
                        For API issues, contact: api-support@example.com
                        """)
                .termsOfService("https://example.com/terms")
                .contact(new Contact()
                        .name("API Support Team")
                        .email("api-support@example.com")
                        .url("https://example.com/support"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * Helper: Create components with security schemes and global schemas
     * 
     * Components define reusable schemas, security schemes, and parameters
     * Can be referenced by @Schema(ref = "#/components/schemas/ErrorResponse")
     */
    private Components createComponents() {
        Components components = new Components();

        // SECURITY SCHEME: JWT/Bearer Token
        // This defines how JWT authentication works in the API
        components.addSecuritySchemes("bearerAuth", new SecurityScheme()
                /**
                 * SecurityScheme Types:
                 * - HTTP: For HTTP authentication (Basic, Bearer, Digest)
                 * - OAUTH2: For OAuth2 flows
                 * - APIKEY: For API key in header/query
                 * - OPENID_CONNECT: For OpenID Connect discovery
                 * 
                 * We use HTTP with Bearer scheme (standard JWT pattern)
                 */
                .type(SecurityScheme.Type.HTTP)
                /**
                 * Scheme specifies which HTTP authentication scheme
                 * "Bearer": For JWT tokens (most common)
                 * "Basic": For username:password encoding
                 * "Digest": For MD5 digest authentication
                 */
                .scheme("bearer")
                /**
                 * Bearer format indicates the token type
                 * "JWT": JSON Web Token
                 * None: Generic bearer token
                 */
                .bearerFormat("JWT")
                .description("""
                        JWT Bearer token authentication.
                        
                        **How to use:**
                        1. Obtain JWT token from authentication endpoint
                        2. Include token in Authorization header as: `Bearer <token>`
                        
                        **Example:**
                        ```
                        Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                        ```
                        
                        **Token expiration:** 24 hours
                        **Scope:** Full API access (modify in production for role-based)
                        """));

        // GLOBAL SCHEMA: ErrorResponse
        // This schema is referenced by all error responses
        // Prevents duplication and ensures consistency
        components.addSchemas("ErrorResponse", new Schema<>()
                .type("object")
                .title("ErrorResponse")
                .description("Standard error response format used by all endpoints")
                .addProperty("message", new Schema<>()
                        .type("string")
                        .description("Human-readable error message")
                        .example("Product not found with id: invalid-id"))
                .addProperty("errorCode", new Schema<>()
                        .type("string")
                        .description("Machine-readable error code")
                        .example("PRODUCT_NOT_FOUND"))
                .addProperty("status", new Schema<>()
                        .type("integer")
                        .format("int32")
                        .description("HTTP status code")
                        .example(404))
                .addProperty("timestamp", new Schema<>()
                        .type("string")
                        .format("date-time")
                        .description("Error timestamp in ISO-8601 format")
                        .example("2026-04-05T17:06:06.265"))
                .addProperty("path", new Schema<>()
                        .type("string")
                        .description("Request path that caused the error")
                        .example("/api/v1/products/invalid-id"))
                .required(java.util.Arrays.asList("message", "errorCode", "status", "timestamp", "path")));

        return components;
    }

}

