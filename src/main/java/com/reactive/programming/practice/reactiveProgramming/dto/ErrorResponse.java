package com.reactive.programming.practice.reactiveProgramming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ErrorResponse - Standard error response DTO
 * Used by GlobalExceptionHandler to return consistent error format
 * OpenAPI documents this schema for all error responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Schema(
            description = "Human-readable error message",
            example = "Product not found with id: 507f1f77bcf86cd799439011",
            required = true
    )
    private String message;

    @Schema(
            description = "Machine-readable error code for programmatic handling",
            example = "PRODUCT_NOT_FOUND",
            required = true,
            allowableValues = {
                    "PRODUCT_NOT_FOUND",
                    "INVALID_ARGUMENT",
                    "VALIDATION_ERROR",
                    "INTERNAL_SERVER_ERROR",
                    "UNAUTHORIZED",
                    "FORBIDDEN"
            }
    )
    private String errorCode;

    @Schema(
            description = "HTTP status code",
            example = "404",
            type = "integer",
            required = true
    )
    private Integer status;

    @Schema(
            description = "Timestamp when error occurred (ISO-8601 format)",
            example = "2026-04-05T17:06:06.265",
            type = "string",
            format = "date-time",
            required = true
    )
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(
            description = "Request path that caused the error",
            example = "/api/v1/products/invalid-id",
            required = true
    )
    private String path;

    @Schema(
            description = "Additional validation error details (optional, for validation errors)",
            example = "[\"price must be greater than 0\", \"name cannot be blank\"]",
            required = false
    )
    private List<String> details;
}

/**
 * BadRequestErrorResponse - Specific for 400 Bad Request
 * Includes validation field errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BadRequestErrorResponse {

    @Schema(
            description = "Error message",
            example = "Validation failed",
            required = true
    )
    private String message;

    @Schema(
            description = "Validation error code",
            example = "VALIDATION_ERROR",
            required = true
    )
    private String errorCode;

    @Schema(
            description = "HTTP 400 status",
            example = "400",
            type = "integer",
            required = true
    )
    private Integer status;

    @Schema(
            description = "Field-level validation errors",
            example = "{\"price\": [\"must be greater than 0\"], \"name\": [\"cannot be blank\"]}",
            required = true
    )
    private java.util.Map<String, List<String>> fieldErrors;

    @Schema(
            description = "Error timestamp",
            example = "2026-04-05T17:06:06.265",
            type = "string",
            format = "date-time",
            required = true
    )
    private LocalDateTime timestamp;
}

/**
 * UnauthorizedErrorResponse - Specific for 401 Unauthorized (JWT auth)
 * Indicates missing or invalid authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UnauthorizedErrorResponse {

    @Schema(
            description = "Authentication error message",
            example = "Unauthorized: Missing or invalid JWT token",
            required = true
    )
    private String message;

    @Schema(
            description = "Error code for auth failures",
            example = "UNAUTHORIZED",
            required = true
    )
    private String errorCode;

    @Schema(
            description = "HTTP 401 status",
            example = "401",
            type = "integer",
            required = true
    )
    private Integer status;

    @Schema(
            description = "Information about what auth is needed",
            example = "Bearer token required in Authorization header",
            required = true
    )
    private String authInfo;

    @Schema(
            description = "Error timestamp",
            example = "2026-04-05T17:06:06.265",
            type = "string",
            format = "date-time",
            required = true
    )
    private LocalDateTime timestamp;
}
