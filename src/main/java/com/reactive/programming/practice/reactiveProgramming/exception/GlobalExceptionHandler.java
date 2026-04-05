package com.reactive.programming.practice.reactiveProgramming.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Global Exception Handler for the entire application.
 * Centralized place to handle exceptions and return consistent error responses.
 *
 * Key Points for Interviews:
 * - @RestControllerAdvice: Annotation that makes this class a global exception handler
 *   It applies to all @RestController classes in the application
 * - Methods must return Mono for reactive controllers (to maintain reactive pipeline)
 * - @ExceptionHandler(SomeException.class) routes specific exceptions to methods
 * - Logging important for debugging and monitoring in production
 * - Consistent error response format improves client experience
 * 
 * Production Patterns:
 * - Log errors with appropriate levels (ERROR for unexpected, WARN for business logic)
 * - Return appropriate HTTP status codes
 * - Include timestamp, message, and error code in response
 * - Don't expose sensitive stack traces to clients
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Standard error response DTO.
     * Returned in JSON format to clients when exceptions occur.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ErrorResponse {
        private String message;
        private String errorCode;
        private int status;
        private LocalDateTime timestamp;
        private String path;
    }

    /**
     * Handle ProductNotFoundException.
     * Returns 404 Not Found when a product doesn't exist.
     *
     * Interview Point: This demonstrates reactive exception handling.
     * Note: We return Mono<ResponseEntity> to keep response reactive.
     *
     * @param ex the ProductNotFoundException
     * @param exchange the ServerWebExchange containing request/response
     * @return Mono containing error response wrapped in ResponseEntity
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleProductNotFoundException(
            ProductNotFoundException ex,
            ServerWebExchange exchange) {

        log.warn("Product not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("PRODUCT_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse));
    }

    /**
     * Handle IllegalArgumentException.
     * Returns 400 Bad Request for invalid input parameters.
     *
     * Use Case: Invalid product data, invalid query parameters, etc.
     *
     * @param ex the IllegalArgumentException
     * @param exchange the ServerWebExchange
     * @return Mono containing error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.warn("Invalid argument provided: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Handle NoResourceFoundException (404 Not Found for static resources).
     * Occurs when static resources like Swagger UI files are not found.
     * Returns 404 Not Found without wrapping as a 500 error.
     *
     * @param ex the NoResourceFoundException
     * @param exchange the ServerWebExchange
     * @return Mono containing error response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNoResourceFound(
            NoResourceFoundException ex,
            ServerWebExchange exchange) {

        log.debug("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Resource not found")
                .errorCode("RESOURCE_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse));
    }

    /**
     * Handle generic Exception.
     * Fallback handler for any unexpected exceptions.
     * Returns 500 Internal Server Error.
     *
     * Important: This is a catch-all - log these carefully as they're unexpected!
     *
     * @param ex the Exception
     * @param exchange the ServerWebExchange
     * @return Mono containing error response
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("An unexpected error occurred. Please contact support.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }
}
