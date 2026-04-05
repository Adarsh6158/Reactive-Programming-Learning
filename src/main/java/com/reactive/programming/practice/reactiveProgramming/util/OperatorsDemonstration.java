package com.reactive.programming.practice.reactiveProgramming.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * OperatorsDemonstration - Comprehensive guide to Reactor operators.
 * Demonstrates practical use of Mono and Flux operators for real-world scenarios.
 *
 * Key Points for Interviews:
 * - Mono: Represents 0 or 1 element (or error)
 * - Flux: Represents 0 to N elements (or error)
 * - Operators: Transform, filter, combine, handle errors on reactive streams
 * - Non-blocking: Operations execute asynchronously without blocking
 * - Composable: Chain operators to build complex data pipelines
 *
 * Common Operators Categories:
 * 1. Transformation: map, flatMap, switchMap
 * 2. Filtering: filter, take, skip, distinct
 * 3. Combining: zip, merge, concat
 * 4. Error Handling: onErrorResume, onErrorReturn, retry
 * 5. Utility: doOn*, timeout, subscribe
 *
 * Each method demonstrates a specific operator or pattern with comments
 * explaining when and why to use it.
 */
@Slf4j
public class OperatorsDemonstration {

    // ============ TRANSFORMATION OPERATORS ============

    /**
     * MAP operator - Transform each element.
     * Projects each item from Flux<T> to Flux<R>.
     * One-to-one mapping: 1 input -> 1 output.
     *
     * Use When:
     * - Converting objects to different types
     * - Extracting specific fields
     * - Applying simple transformations
     *
     * Performance: Synchronous operation on each element
     *
     * Interview Example:
     */
    public static Flux<Integer> mapExample() {
        log.info("=== MAP OPERATOR EXAMPLE ===");
        // Input: Flux of numbers
        return Flux.range(1, 5)
                // Transform each number: multiply by 10
                .map(number -> {
                    log.info("Mapping input: {} to output: {}", number, number * 10);
                    return number * 10;
                });
        // Output: Flux of [10, 20, 30, 40, 50]
    }

    /**
     * FLATMAP operator - Transform each element to Flux and merge results.
     * Projects each item to Mono/Flux and flattens all results.
     * One-to-many mapping with flattening.
     *
     * Use When:
     * - Each element needs async transformation
     * - Result is multiple items per input
     * - Need to call external APIs/database for each item
     *
     * Important: Order is NOT guaranteed if async operations complete at different times.
     * Use concatMap if order matters, flatMap for performance.
     *
     * Interview Explanation:
     * - flatMap vs map: map returns Mono/Flux<T>, flatMap flattens the result
     * - Example: For each user ID, fetch user details from DB -> multiple items
     *
     */
    public static Flux<String> flatMapExample() {
        log.info("=== FLATMAP OPERATOR EXAMPLE ===");
        // Input: Flux of userIds [1, 2, 3]
        return Flux.range(1, 3)
                // For each user ID, fetch user details (simulated)
                .flatMap(userId -> {
                    log.info("Processing userId: {}", userId);
                    // Simulate async operation (like DB call)
                    return simulateAsyncUserLookup(userId);
                });
        // Output: Combined results from all user lookups
    }

    /**
     * SWITCHMAP - Like flatMap but cancels previous subscription.
     * When new element arrives, cancels inner Flux of previous element.
     * Useful for "latest" pattern - only care about most recent item.
     *
     * Use When:
     * - Auto-complete search (cancel previous searches when user types new char)
     * - Live data feeds (only show latest updates)
     * - User switches between items (only fetch latest item's data)
     *
     * Interview Point: switchMap vs flatMap
     * - flatMap: all results kept, subscribe to all
     * - switchMap: cancels previous, only latest matters
     *
     */
    public static Flux<String> switchMapExample() {
        log.info("=== SWITCHMAP OPERATOR EXAMPLE ===");
        // Simulating search queries: user types "h", "he", "hel", "hell", "hello"
        return Flux.just("h", "he", "hel", "hell", "hello")
                .delayElements(Duration.ofMillis(100))
                .switchMap(searchTerm -> {
                    log.info("Searching for: {}", searchTerm);
                    // Each search is async and takes time
                    return simulateAsyncSearch(searchTerm);
                });
        // Output: Only results for "hello" (previous searches are cancelled)
    }

    // ============ FILTERING OPERATORS ============

    /**
     * FILTER operator - Keep only elements matching predicate.
     * Filters out elements that don't match condition.
     *
     * Use When:
     * - Conditional selection (only active products, only premium users)
     * - Data validation
     * - Reducing result set
     *
     */
    public static Flux<Integer> filterExample() {
        log.info("=== FILTER OPERATOR EXAMPLE ===");
        return Flux.range(1, 10)
                .filter(number -> {
                    boolean passes = number % 2 == 0;
                    log.info("Number {} is even: {}", number, passes);
                    return passes;
                });
        // Output: Flux of [2, 4, 6, 8, 10]
    }

    /**
     * TAKE operator - Emit only first N elements then complete.
     * Great for pagination or limiting results.
     *
     * Use When:
     * - Pagination (take 10 items for page 1)
     * - Rate limiting (process only first N items)
     * - Preview data
     *
     */
    public static Flux<Integer> takeExample() {
        log.info("=== TAKE OPERATOR EXAMPLE ===");
        return Flux.range(1, 100)
                .take(5);
        // Output: Flux of [1, 2, 3, 4, 5] then completes
    }

    /**
     * SKIP operator - Skip first N elements.
     * Opposite of take - useful for pagination.
     *
     * Use When:
     * - Skip page offset (skip 10, take 10 for page 2)
     * - Ignore initial data
     *
     */
    public static Flux<Integer> skipExample() {
        log.info("=== SKIP OPERATOR EXAMPLE ===");
        return Flux.range(1, 10)
                .skip(5);
        // Output: Flux of [6, 7, 8, 9, 10]
    }

    /**
     * DISTINCT operator - Emit only unique elements.
     * Removes duplicates in the stream.
     *
     * Use When:
     * - Removing duplicate database results
     * - Unique user IDs from log entries
     * - Suppressing duplicate notifications
     *
     * Caution: Stores seen elements in memory!
     * On infinite streams, can cause memory issues.
     *
     */
    public static Flux<Integer> distinctExample() {
        log.info("=== DISTINCT OPERATOR EXAMPLE ===");
        return Flux.just(1, 2, 2, 3, 3, 3, 4, 5, 5)
                .distinct();
        // Output: Flux of [1, 2, 3, 4, 5]
    }

    // ============ COMBINING OPERATORS ============

    /**
     * ZIP operator - Combine elements from multiple streams.
     * Waits for one element from each stream and combines them.
     * Completes when the shortest stream completes.
     *
     * Use When:
     * - Combining data from multiple sources
     * - Aggregating related data (user + orders)
     * - Waiting for multiple async operations
     *
     */
    public static Flux<String> zipExample() {
        log.info("=== ZIP OPERATOR EXAMPLE ===");
        Flux<String> users = Flux.just("Alice", "Bob", "Charlie");
        Flux<Integer> ages = Flux.just(25, 30, 35);

        return Flux.zip(users, ages)
                .map(tuple -> tuple.getT1() + " is " + tuple.getT2() + " years old");
        // Output: "Alice is 25 years old", "Bob is 30 years old", ...
    }

    /**
     * MERGE operator - Combine multiple streams without waiting.
     * Merges all streams into one, emits any value as it arrives.
     * Completes when all streams complete.
     *
     * Use When:
     * - Combining multiple event streams
     * - Aggregating from multiple sources
     * - Racing multiple operations (but see merge)
     *
     */
    public static Flux<Integer> mergeExample() {
        log.info("=== MERGE OPERATOR EXAMPLE ===");
        Flux<Integer> flux1 = Flux.just(1, 2, 3).delayElements(Duration.ofMillis(100));
        Flux<Integer> flux2 = Flux.just(10, 20, 30).delayElements(Duration.ofMillis(50));

        return Flux.merge(flux1, flux2);
        // Output: Values from both flux, interleaved based on timing
    }

    /**
     * CONCAT operator - Concatenate streams sequentially.
     * Waits for first stream to complete, then subscribes to next.
     * Preserves order: all flux1 items before flux2.
     *
     * Use When:
     * - Processing in sequence (must maintain order)
     * - Following chains of operations
     * - Database migrations or setup steps
     *
     */
    public static Flux<Integer> concatExample() {
        log.info("=== CONCAT OPERATOR EXAMPLE ===");
        Flux<Integer> flux1 = Flux.just(1, 2, 3);
        Flux<Integer> flux2 = Flux.just(10, 20, 30);

        return Flux.concat(flux1, flux2);
        // Output: [1, 2, 3, 10, 20, 30] - always in order
    }

    // ============ ERROR HANDLING OPERATORS ============

    /**
     * ERROR HANDLING - Multiple approaches to handle exceptions.
     *
     * Three main strategies:
     * 1. onErrorReturn: return default value on error
     * 2. onErrorResume: call another Mono/Flux on error
     * 3. onErrorMap: transform error to different exception
     *
     * Also: retry, retryWhen, timeout
     */

    /**
     * ONERRORRETURN - Return default value on error.
     * When error occurs, emit default value and complete.
     *
     * Use When:
     * - Have sensible default value
     * - Error is acceptable and can be recovered
     * - Don't need to investigate error
     *
     */
    public static Mono<String> onErrorReturnExample() {
        log.info("=== ONERRORRETURN EXAMPLE ===");
        return Mono.<String>error(new RuntimeException("API call failed"))
                .onErrorReturn("FALLBACK_VALUE");
        // Output: Mono emitting "FALLBACK_VALUE" and completes
    }

    /**
     * ONERRORRESUME - Call another Mono/Flux on error.
     * When error occurs, subscribe to fallback stream.
     * Best for complex error handling and recovery.
     *
     * Use When:
     * - Need intelligent error recovery
     * - Call different service on error
     * - Conditional error handling
     *
     */
    public static Mono<String> onErrorResumeExample() {
        log.info("=== ONERRORRESUME EXAMPLE ===");
        return Mono.<String>error(new RuntimeException("Primary API failed"))
                .onErrorResume(error -> {
                    log.warn("Using fallback API due to: {}", error.getMessage());
                    return callFallbackAPI();
                });
        // Output: Result from fallback API
    }

    /**
     * RETRY - Retry operation automatically on error.
     * Resubscribes specified number of times on failure.
     *
     * Use When:
     * - Transient errors (network hiccup, temporary server down)
     * - Simple retry logic without backoff
     *
     */
    public static Mono<String> retryExample() {
        log.info("=== RETRY EXAMPLE ===");
        return simulateUnreliableAPI()
                .retry(3); // Retry up to 3 times on error
        // Output: Either success or error after 3 retries
    }

    /**
     * TIMEOUT - Fail if operation takes too long.
     * If no result within specified duration, emit error.
     *
     * Use When:
     * - Prevent hanging requests
     * - API must respond within SLA
     * - Cancel long-running operations
     *
     */
    public static Mono<String> timeoutExample() {
        log.info("=== TIMEOUT EXAMPLE ===");
        return Mono.<String>never()
                .timeout(Duration.ofSeconds(5));
        // Output: Error after 5 seconds (timeout exceeded)
    }

    // ============ DO-ON OPERATORS ============

    /**
     * DO-ON operators - Side effects without modifying stream.
     * Used for logging, monitoring, debugging.
     *
     * Available:
     * - doOnNext: when element emitted
     * - doOnError: when error occurs
     * - doOnComplete: when stream completes
     * - doOnSubscribe: when subscribed
     * - doFinally: cleanup after any outcome
     *
     */
    public static Mono<String> doOnExample() {
        log.info("=== DO-ON OPERATORS EXAMPLE ===");
        return Mono.just("Hello")
                .doOnSubscribe(subscription -> log.info("Subscription started"))
                .doOnNext(value -> log.info("Emitting value: {}", value))
                .doOnSuccess(value -> log.info("Success: {}", value))
                .doOnError(error -> log.error("Error occurred", error))
                .doFinally(type -> log.info("Finally - signal type: {}", type));
        // Output: Mono<String> with side effects logged
    }

    // ============ HELPER METHODS ============

    /**
     * Simulates async user lookup (e.g., database call).
     */
    private static Flux<String> simulateAsyncUserLookup(int userId) {
        log.info("Simulating DB lookup for user: {}", userId);
        return Flux.just("User" + userId)
                .delayElements(Duration.ofMillis(100));
    }

    /**
     * Simulates async search operation.
     */
    private static Flux<String> simulateAsyncSearch(String query) {
        log.info("Simulating search API for: {}", query);
        return Flux.just("Result1", "Result2", "Result3")
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * Simulates calling fallback API.
     */
    private static Mono<String> callFallbackAPI() {
        log.info("Calling fallback API");
        return Mono.<String>just("Data from fallback API")
                .delayElement(Duration.ofMillis(100));
    }

    /**
     * Simulates unreliable API that might fail.
     */
    private static Mono<String> simulateUnreliableAPI() {
        log.info("Calling unreliable API");
        return Mono.<String>error(new RuntimeException("Temporary error"))
                .delayElement(Duration.ofMillis(100));
    }
}
