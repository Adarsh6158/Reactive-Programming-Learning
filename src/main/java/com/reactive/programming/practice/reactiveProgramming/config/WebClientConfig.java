package com.reactive.programming.practice.reactiveProgramming.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WebClientConfig - Configuration bean for WebClient.
 * Creates and configures the WebClient singleton bean for HTTP calls.
 *
 * Key Points for Interviews:
 * - WebClient is the reactive HTTP client for Spring WebFlux
 * - Thread-safe and reusable (create once, use across app)
 * - Typically configured as @Bean in a @Configuration class
 * - Base URL, headers, timeout are set centrally
 *
 * Production Best Practices:
 * - Set reasonable timeouts to prevent hanging connections
 * - Configure connection pools for performance
 * - Log requests/responses for debugging
 * - Set common headers (User-Agent, Accept, etc.)
 *
 * This configuration uses JSONPlaceholder (fake API for testing).
 * In production, replace with your actual API base URL.
 */
@Configuration
public class WebClientConfig {

    /**
     * Create WebClient bean with default configuration.
     * This bean is injected wherever WebClient is needed.
     *
     * Configuration:
     * - baseUrl: Set base URL to avoid repeating in every request
     * - defaultHeader: Set default HTTP headers for all requests
     * - defaultUriVariables: Set URI template variables
     * - responseTimeout: Max time to wait for response
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                // Base URL for all requests - using JSONPlaceholder for demo
                // In production, use your actual API endpoint
                .baseUrl("https://jsonplaceholder.typicode.com")

                // Default headers for all requests
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "ReactiveWeFluxApp/1.0")

                // Build and return the configured client
                .build();
    }

    /**
     * Alternative WebClient bean with custom ExchangeStrategies.
     * Can be used if you need advanced configuration like:
     * - Custom JSON serialization
     * - Custom buffer sizes
     * - Custom codecs
     *
     * This is shown as reference for interview discussions.
     * Uncomment and use if needed in production.
     */
    /*
    @Bean("advancedWebClient")
    public WebClient advancedWebClient() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.maxInMemorySize(50 * 1024 * 1024); // 50MB buffer
                })
                .build();

        return WebClient.builder()
                .baseUrl("https://api.example.com")
                .exchangeStrategies(exchangeStrategies)
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "password"))
                .build();
    }
    */
}
