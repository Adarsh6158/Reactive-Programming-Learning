package com.reactive.programming.practice.reactiveProgramming.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ExternalApiClient - Demonstrates WebClient usage for making non-blocking HTTP calls.
 * WebClient is the reactive alternative to RestTemplate.
 *
 * Key Points for Interviews:
 * - WebClient: Spring's non-blocking HTTP client for WebFlux
 * - Simplifies REST calls while maintaining reactive chains
 * - Built on Project Reactor (same Mono/Flux model)
 * - Supports timeout, retry, error handling natively
 * - Thread-safe and reusable (shared across requests)
 *
 * Comparison with RestTemplate:
 * - RestTemplate: Blocking, thread-per-request model
 * - WebClient: Non-blocking, async, with reactive backpressure
 * - For new projects, WebClient is preferred in WebFlux applications
 *
 * Usage Pattern:
 * 1. Configure WebClient (base URL, common headers, etc.)
 * 2. Specify HTTP method (get, post, put, delete)
 * 3. Set URI and parameters
 * 4. Define request body and headers if needed
 * 5. Specify response type (bodyToMono, bodyToFlux, etc.)
 * 6. Chain operators for retry, timeout, error handling
 * 7. Subscribe or return Mono/Flux to caller
 *
 * In this example, we're calling a mock external API (JSONPlaceholder).
 * In real scenarios, this would be your actual external service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {

    /**
     * WebClient bean instance (configured in @Bean if needed).
     * For this example, we create it here, but typically configured as @Bean.
     */
    private final WebClient webClient;

    /**
     * Get user data from external API.
     * Demonstrates basic GET request with WebClient.
     *
     * Interview Points:
     * - uri() builds the full URL
     * - retrieve() gets the response
     * - bodyToMono(Map.class): deserialize response to Map
     * - onErrorResume: handle errors gracefully
     * - log(): side effect to log execution (useful for debugging)
     *
     * Error Handling Chain:
     * - WebClientResponseException: HTTP errors (4xx, 5xx)
     * - Any other exception: network, timeout, etc.
     *
     * @param userId the user ID to fetch
     * @return Mono<Map<String, Object>> user data wrapped in reactive stream
     */
    public Mono<Map<String, Object>> getUserData(Long userId) {
        log.info("Fetching user data for ID: {}", userId);

        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(user -> log.debug("Received user data: {}", user))
                .doOnError(error -> log.error("Error fetching user data", error))
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.warn("HTTP error fetching user: {} - {}", error.getRawStatusCode(), error.getResponseBodyAsString());
                    return Mono.empty();
                })
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Get all posts for a user.
     * Demonstrates GET request returning Flux (multiple items).
     *
     * Interview Points:
     * - bodyToFlux(Map.class): deserialize to Flux of items
     * - Handles streaming response or paginated results
     * - Retries with exponential backoff on failure
     *
     * Retry Strategy:
     * - Retry mechanism with exponential backoff
     * - Important for resilience in distributed systems
     * - maxRetries(3): try up to 4 times (initial + 3 retries)
     * - Duration.ofMillis(100): wait before each retry
     *
     * @param userId the user ID
     * @return Flux<Map<String, Object>> stream of user's posts
     */
    public Flux<Map<String, Object>> getUserPosts(Long userId) {
        log.info("Fetching posts for user ID: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/posts", userId)
                .retrieve()
                .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(post -> log.debug("Received post: {}", post.get("id")))
                .doOnError(error -> log.error("Error fetching posts", error))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(signal -> log.warn("Retrying post fetch, attempt: {}", signal.totalRetries())))
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Create a post via external API.
     * Demonstrates POST request with request body using WebClient.
     *
     * Interview Points:
     * - bodyValue(): set request body (automatically serialized to JSON)
     * - header(): add custom HTTP headers
     * - contentType(): specify request content type
     * - flatMap: chain operations after receiving response
     *
     * Reactive Chain:
     * 1. Create request with POST method and body
     * 2. Send and get response
     * 3. Deserialize response
     * 4. Log success/error
     * 5. Handle errors appropriately
     *
     * @param userId the author user ID
     * @param title post title
     * @param body post content
     * @return Mono<Map<String, Object>> created post data
     */
    public Mono<Map<String, Object>> createPost(Long userId, String title, String body) {
        log.info("Creating post for user ID: {}", userId);

        Map<String, Object> postData = new HashMap<>();
        postData.put("userId", userId);
        postData.put("title", title);
        postData.put("body", body);

        return webClient.post()
                .uri("/posts")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(postData)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(response -> log.info("Post created successfully: {}", response.get("id")))
                .doOnError(error -> log.error("Error creating post", error))
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Update post via external API.
     * Demonstrates PUT request with request body.
     *
     * Interview Points:
     * - Similar to POST but with full resource replacement semantics
     * - Uses /posts/{id} endpoint
     *
     * @param postId the post ID to update
     * @param title updated title
     * @param body updated content
     * @return Mono<Map<String, Object>> updated post
     */
    public Mono<Map<String, Object>> updatePost(Long postId, String title, String body) {
        log.info("Updating post ID: {}", postId);

        Map<String, Object> postData = new HashMap<>();
        postData.put("title", title);
        postData.put("body", body);

        return webClient.put()
                .uri("/posts/{id}", postId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(postData)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Delete post from external API.
     * Demonstrates DELETE request.
     *
     * Interview Points:
     * - DELETE typically returns empty response (204 No Content)
     * - bodyToMono(Void.class): ignore response body
     * - Great for cleanup operations
     *
     * @param postId the post ID to delete
     * @return Mono<Void> completion signal
     */
    public Mono<Void> deletePost(Long postId) {
        log.info("Deleting post ID: {}", postId);

        return webClient.delete()
                .uri("/posts/{id}", postId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Post deleted successfully"))
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Example of complex API orchestration.
     * Fetch user, then fetch all their posts, combining results.
     *
     * Interview Points:
     * - flatMap: chain operations where next depends on previous result
     * - zipWith: combine results from two async operations
     * - Demonstrates reactive composition pattern
     *
     * Reactive Composition:
     * 1. Fetch user data (Mono)
     * 2. Fetch user's posts (Flux)
     * 3. Combine both results
     * 4. Return combined data
     *
     * This is powerful for aggregating data from multiple sources!
     *
     * @param userId the user ID
     * @return Mono containing combined user and posts data
     */
    public Mono<Map<String, Object>> getUserWithPosts(Long userId) {
        log.info("Fetching user and their posts for ID: {}", userId);

        return this.getUserData(userId)
                .zipWith(this.getUserPosts(userId).collectList())
                .map(tuple -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user", tuple.getT1());
                    result.put("posts", tuple.getT2());
                    return result;
                })
                .doOnSuccess(data -> log.info("Successfully fetched user and posts"))
                .doOnError(error -> log.error("Error fetching user and posts", error));
    }
}
