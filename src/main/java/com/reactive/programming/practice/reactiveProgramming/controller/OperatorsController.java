package com.reactive.programming.practice.reactiveProgramming.controller;

import com.reactive.programming.practice.reactiveProgramming.util.OperatorsDemonstration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * OperatorsController - Educational demonstrations of Reactor operators.
 * 
 * ========== PURPOSE ==========
 * This controller demonstrates how Reactor operators work in real-world endpoints.
 * Each endpoint showcases a specific operator with input → transformation → output.
 * 
 * ========== LEARNING APPROACH ==========
 * Visit each endpoint to see operator behavior:
 * 1. Single Transformation: /demo/map, /demo/filter
 * 2. Stream Composition: /demo/flatmap, /demo/switchmap
 * 3. Stream Selection: /demo/take, /demo/skip, /demo/distinct
 * 4. Stream Combination: /demo/zip, /demo/merge, /demo/concat
 * 5. Error Handling: /demo/error-handling/*
 * 6. Side Effects: /demo/do-on-operators
 * 
 * ========== OPERATOR CATEGORIES ==========
 * 
 * TRANSFORMATION OPERATORS:
 * - map(): Element-wise transformation (one-to-one)
 * - flatMap(): Async transformation with flattening (one-to-many)
 * - switchMap(): Cancels previous, starts new (auto-complete pattern)
 * 
 * FILTERING OPERATORS:
 * - filter(): Conditional pass-through
 * - take(n): Emit first N, then complete
 * - skip(n): Skip first N, then emit rest
 * - distinct(): Remove duplicates
 * 
 * COMBINING OPERATORS:
 * - zip(): Pair elements from multiple streams
 * - merge(): Interleave elements from multiple streams
 * - concat(): Sequential concatenation
 * 
 * ERROR HANDLING:
 * - onErrorReturn(): Replace error with fallback value
 * - onErrorResume(): Replace error with fallback Mono/Flux
 * - retry(): Retry failed operation N times
 * - timeout(): Fail if takes too long
 * 
 * SIDE EFFECT OPERATORS:
 * - doOnNext(): Side effect on each element
 * - doOnError(): Side effect on error
 * - doOnComplete(): Side effect on completion
 * - doOnFinally(): Side effect after terminal event
 * 
 * ========== REACTIVE PRINCIPLES ==========
 * - No side effects in transformations (map, filter, etc.)
 * - Use do-on-operators for logging/debugging only
 * - Operators are lazy until subscribed
 * - Each operator returns new Publisher (immutable pattern)
 */
@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Reactive Operators (Learning)",
    description = """
        Educational demonstrations of Project Reactor operators.
        
        Learn how these operators work via interactive endpoints:
        - Transformation: map, flatMap, switchMap
        - Filtering: filter, take, skip, distinct
        - Combining: zip, merge, concat
        - Error Handling: onErrorReturn, onErrorResume, retry, timeout
        - Side Effects: doOn* operators
        
        Each endpoint demonstrates the operator with example data.
        Use Swagger UI or curl to test each demonstration.
        """
)
public class OperatorsController {

    /**
     * GET /api/v1/demo/map
     * 
     * MAP OPERATOR: Element-wise transformation (1:1 mapping)
     * 
     * Demonstrates:
     * - Input: [1, 2, 3, 4, 5]
     * - Transformation: multiply by 10
     * - Output: [10, 20, 30, 40, 50]
     * 
     * INTERVIEW POINT:
     * Map transforms each element synchronously.
     * If transformation takes time, entire stream blocks.
     * Use flatMap for async transformations.
     */
    @GetMapping("/map")
    @Operation(
        summary = "Demonstrate MAP operator",
        description = """
            Shows the map() operator: synchronous element transformation.
            
            Pattern:
            ```
            Flux.range(1, 5)
                .map(x -> x * 10)  // Transform: 1→10, 2→20, etc
            ```
            
            Result: [10, 20, 30, 40, 50]
            
            Use map() when:
            - Synchronous transformation needed
            - Transformation is fast (no I/O)
            - One-to-one mapping (each input produces one output)
            
            Don't use map() for:
            - Async operations (use flatMap)
            - Non-deterministic operations
            - Operations that might fail
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Map demonstration results",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(type = "array")
        )
    )
    public Flux<Integer> demonstrateMap() {
        log.info("GET /api/v1/demo/map - demonstrating map operator");
        return OperatorsDemonstration.mapExample();
    }

    /**
     * GET /api/v1/demo/flatmap
     * 
     * FLATMAP OPERATOR: Async transformation with auto-flattening
     * 
     * Demonstrates:
     * - Input: "apple", "banana"
     * - Transformation: Fetch data for each item asynchronously
     * - Flattening: Combine results into single stream
     * 
     * INTERVIEW POINT:
     * FlatMap is for async operations producing streams.
     * Automatically subscribes to inner streams and flattens.
     * Important for compositions like: fetch → process → fetch
     */
    @GetMapping("/flatmap")
    @Operation(
        summary = "Demonstrate FLATMAP operator",
        description = """
            Shows the flatMap() operator: async transformation with auto-flattening.
            
            Pattern:
            ```
            Flux.just("apple", "banana")
                .flatMap(item -> asyncFetchData(item))  // Each produces Mono
                // Results auto-flattened into single Flux
            ```
            
            Key Characteristics:
            - Inner Publisher (Mono/Flux) automatically subscribed
            - Results collected into single outer stream
            - Concurrency: All inner streams run concurrently
            - Unordered: Results order not guaranteed
            
            Use flatMap() for:
            - Async operations (API calls, DB queries)
            - One-to-many mappings
            - Chaining async operations
            
            Similar operators:
            - concatMap(): Ordered version of flatMap
            - flatMapSequential(): Maintains order, runs sequential
            - switchMap(): Cancels previous, starts new
            """
    )
    @ApiResponse(responseCode = "200", description = "FlatMap demonstration results")
    public Flux<String> demonstrateFlatMap() {
        log.info("GET /api/v1/demo/flatmap - demonstrating flatmap operator");
        return OperatorsDemonstration.flatMapExample();
    }

    /**
     * GET /api/v1/demo/switchmap
     * 
     * SWITCHMAP OPERATOR: Cancels previous, starts new (auto-complete pattern)
     * 
     * Demonstrates:
     * - Input: Search terms "java", "javascript", "java" (rapid changes)
     * - Behavior: Cancels previous search when new term arrives
     * - Output: Only results for latest search term
     * 
     * INTERVIEW POINT:
     * SwitchMap is ideal for auto-complete/search scenarios.
     * Prevents resource waste on cancelled searches.
     * Inner subscription cancelled on new outer item.
     */
    @GetMapping("/switchmap")
    @Operation(
        summary = "Demonstrate SWITCHMAP operator",
        description = """
            Shows the switchMap() operator: cancels previous inner stream on new outer item.
            
            Pattern:
            ```
            searchTerms
                .switchMap(term -> searchAPI(term))  // Cancel previous, start new
            ```
            
            Key Characteristics:
            - Previous inner subscription cancelled on new outer item
            - Only latest result matters
            - Stops wasted API calls
            
            Real-world use cases:
            - Auto-complete search boxes
            - Type-ahead suggestions
            - Real-time stock price updates (only latest matters)
            - Location-based services
            
            Comparison:
            - flatMap: Concurrent, all results used
            - switchMap: Latest only, cancels previous
            - concatMap: Sequential, ordered results
            """
    )
    @ApiResponse(responseCode = "200", description = "SwitchMap demonstration results")
    public Flux<String> demonstrateSwitchMap() {
        log.info("GET /api/v1/demo/switchmap - demonstrating switchmap operator");
        return OperatorsDemonstration.switchMapExample();
    }

    /**
     * GET /api/v1/demo/filter
     * 
     * FILTER OPERATOR: Conditional pass-through
     * 
     * Demonstrates:
     * - Input: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
     * - Predicate: isEven (n % 2 == 0)
     * - Output: [2, 4, 6, 8, 10]
     */
    @GetMapping("/filter")
    @Operation(
        summary = "Demonstrate FILTER operator",
        description = """
            Shows the filter() operator: conditional pass-through.
            
            Pattern:
            ```
            Flux.range(1, 10)
                .filter(n -> n % 2 == 0)  // Only evens pass through
            ```
            
            Result: [2, 4, 6, 8, 10]
            
            Key Characteristics:
            - Predicate returns true/false
            - True: element passes through
            - False: element discarded
            - Empty stream possible if all filtered out
            
            Use filter() for:
            - Conditional selection
            - Removing outliers/invalid data
            - Permission-based filtering
            """
    )
    @ApiResponse(responseCode = "200", description = "Filter demonstration results")
    public Flux<Integer> demonstrateFilter() {
        log.info("GET /api/v1/demo/filter - demonstrating filter operator");
        return OperatorsDemonstration.filterExample();
    }

    /**
     * GET /api/v1/demo/take
     * 
     * TAKE OPERATOR: Emit first N elements then complete
     * 
     * Demonstrates:
     * - Input: [1, 2, 3, 4, 5, ..., 100]
     * - Operation: Take first 5
     * - Output: [1, 2, 3, 4, 5]
     * - Remaining: 6-100 never emitted
     */
    @GetMapping("/take")
    @Operation(
        summary = "Demonstrate TAKE operator",
        description = """
            Shows the take(n) operator: emit first N elements then complete.
            
            Pattern:
            ```
            Flux.range(1, 100)
                .take(5)  // First 5: [1,2,3,4,5]
            ```
            
            Key Characteristics:
            - Emits exactly N elements
            - Completes after N elements
            - Remaining elements never subscribed
            - Useful for pagination, sampling
            
            Use take() for:
            - Pagination (first page)
            - Sampling large streams
            - Limiting API results
            - Preview data
            """
    )
    @ApiResponse(responseCode = "200", description = "Take demonstration results")
    public Flux<Integer> demonstrateTake() {
        log.info("GET /api/v1/demo/take - demonstrating take operator");
        return OperatorsDemonstration.takeExample();
    }

    /**
     * GET /api/v1/demo/skip
     * 
     * SKIP OPERATOR: Skip first N elements then emit the rest
     * 
     * Demonstrates:
     * - Input: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
     * - Operation: Skip first 5
     * - Output: [6, 7, 8, 9, 10]
     */
    @GetMapping("/skip")
    @Operation(
        summary = "Demonstrate SKIP operator",
        description = """
            Shows the skip(n) operator: skip first N elements then emit rest.
            
            Pattern:
            ```
            Flux.range(1, 10)
                .skip(5)  // Skip first 5: [6,7,8,9,10]
            ```
            
            Key Characteristics:
            - Discards first N elements
            - Emits all remaining elements
            
            Use skip() for:
            - Pagination (skip to page N)
            - Removing headers/metadata
            - Starting from middle of sequence
            """
    )
    @ApiResponse(responseCode = "200", description = "Skip demonstration results")
    public Flux<Integer> demonstrateSkip() {
        log.info("GET /api/v1/demo/skip - demonstrating skip operator");
        return OperatorsDemonstration.skipExample();
    }

    /**
     * GET /api/v1/demo/distinct
     * 
     * DISTINCT OPERATOR: Removes duplicate elements
     * 
     * Demonstrates:
     * - Input: [1, 2, 2, 3, 1, 4, 3, 5]
     * - Filter duplicates
     * - Output: [1, 2, 3, 4, 5]
     */
    @GetMapping("/distinct")
    @Operation(
        summary = "Demonstrate DISTINCT operator",
        description = """
            Shows the distinct() operator: removes duplicates.
            
            Pattern:
            ```
            Flux.just(1,2,2,3,1,4,3,5)
                .distinct()  // [1,2,3,4,5]
            ```
            
            Key Characteristics:
            - Remembers previously emitted elements
            - Filters out duplicates
            - Maintains order of first occurrence
            - Memory: O(n) for tracking seen elements
            
            Use distinct() for:
            - Removing duplicates from query results
            - Unique users in stream
            - Deduplicating sensor data
            
            Note: Keep memory usage in mind for infinity streams
            """
    )
    @ApiResponse(responseCode = "200", description = "Distinct demonstration results")
    public Flux<Integer> demonstrateDistinct() {
        log.info("GET /api/v1/demo/distinct - demonstrating distinct operator");
        return OperatorsDemonstration.distinctExample();
    }

    /**
     * GET /api/v1/demo/zip
     * 
     * ZIP OPERATOR: Combines multiple streams element-wise
     * 
     * Demonstrates:
     * - Stream A: ["Alice", "Bob", "Charlie"]
     * - Stream B: [25, 30, 35]
     * - Output: ["Alice is 25", "Bob is 30", "Charlie is 35"]
     */
    @GetMapping("/zip")
    @Operation(
        summary = "Demonstrate ZIP operator",
        description = """
            Shows the zip() operator: combines multiple streams element-wise.
            
            Pattern:
            ```
            names = Flux.just("Alice", "Bob", "Charlie")
            ages = Flux.just(25, 30, 35)
            
            Flux.zip(names, ages, (name, age) -> name + " is " + age)
            ```
            
            Result: ["Alice is 25", "Bob is 30", "Charlie is 35"]
            
            Key Characteristics:
            - Pairs elements from multiple streams
            - Waits for slowest stream
            - Completes when shortest stream completes
            - Preserves order
            
            Use zip() for:
            - Correlating related data
            - Combining request/response pairs
            - Multi-source aggregation
            
            Performance: Slower stream determines speed
            """
    )
    @ApiResponse(responseCode = "200", description = "Zip demonstration results")
    public Flux<String> demonstrateZip() {
        log.info("GET /api/v1/demo/zip - demonstrating zip operator");
        return OperatorsDemonstration.zipExample();
    }

    /**
     * GET /api/v1/demo/merge
     * 
     * MERGE OPERATOR: Interleaves multiple streams
     * 
     * Demonstrates:
     * - Stream A: [1, 3, 5, 7, 9]
     * - Stream B: [2, 4, 6, 8, 10]
     * - Output (interleaved): [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
     */
    @GetMapping("/merge")
    @Operation(
        summary = "Demonstrate MERGE operator",
        description = """
            Shows the merge() operator: interleaves multiple streams.
            
            Pattern:
            ```
            oddNumbers = Flux.just(1, 3, 5, 7, 9)
            evenNumbers = Flux.just(2, 4, 6, 8, 10)
            
            Flux.merge(oddNumbers, evenNumbers)
            ```
            
            Result: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] (interleaved order)
            
            Key Characteristics:
            - Concurrent emission from all sources
            - No waiting between sources
            - Order not guaranteed (timing-dependent)
            - Completes when all sources complete
            
            Use merge() for:
            - Combining event streams
            - Multiple data sources
            - Real-time data feeds
            
            VS zip(): 
            - merge: fast, unordered, concurrent
            - zip: ordered, synchronized pairing
            """
    )
    @ApiResponse(responseCode = "200", description = "Merge demonstration results")
    public Flux<Integer> demonstrateMerge() {
        log.info("GET /api/v1/demo/merge - demonstrating merge operator");
        return OperatorsDemonstration.mergeExample();
    }

    /**
     * GET /api/v1/demo/concat
     * 
     * CONCAT OPERATOR: Sequential concatenation (ordered)
     * 
     * Demonstrates:
     * - Stream A: [1, 2, 3]
     * - Stream B: [10, 20, 30]
     * - Output: [1, 2, 3, 10, 20, 30] (strictly ordered)
     */
    @GetMapping("/concat")
    @Operation(
        summary = "Demonstrate CONCAT operator",
        description = """
            Shows the concat() operator: sequential concatenation.
            
            Pattern:
            ```
            first = Flux.just(1, 2, 3)
            second = Flux.just(10, 20, 30)
            
            Flux.concat(first, second)
            ```
            
            Result: [1, 2, 3, 10, 20, 30] (strict order maintained)
            
            Key Characteristics:
            - Sequential subscription (one at a time)
            - First stream fully processed before second starts
            - Order guaranteed
            - Slower than merge
            
            Use concat() for:
            - Ordered operations (pagination)
            - Transaction sequences
            - Ordered event processing
            
            Comparison:
            - merge: concurrent, fast, unordered
            - concat: sequential, ordered, guaranteed
            - zip: paired synchronization
            """
    )
    @ApiResponse(responseCode = "200", description = "Concat demonstration results")
    public Flux<Integer> demonstrateConcat() {
        log.info("GET /api/v1/demo/concat - demonstrating concat operator");
        return OperatorsDemonstration.concatExample();
    }

    /**
     * GET /api/v1/demo/error-handling/on-error-return
     * 
     * ERROR HANDLING: onErrorReturn - Replace error with fallback value
     * 
     * Demonstrates:
     * - Normal operation throws exception
     * - Instead of propagating error
     * - Returns fallback value
     */
    @GetMapping("/error-handling/on-error-return")
    @Operation(
        summary = "Demonstrate onErrorReturn",
        description = """
            Shows error handling with onErrorReturn(): return fallback value on error.
            
            Pattern:
            ```
            operation()  // throws exception
                .onErrorReturn("fallback value")  // return fallback
            ```
            
            Key Characteristics:
            - Error suppressed
            - Single fallback value emitted
            - Stream completes normally
            - No error downstream
            
            Use onErrorReturn() for:
            - Simple error recovery
            - Default values
            - Non-critical operations
            
            When to use:
            - Single operation fails
            - Have sensible default
            - Can ignore error
            
            Limitations:
            - Only one fallback value
            - For complex recovery use onErrorResume
            """
    )
    @ApiResponse(responseCode = "200", description = "OnErrorReturn demonstration")
    public Mono<String> demonstrateOnErrorReturn() {
        log.info("GET /api/v1/demo/error-handling/on-error-return");
        return OperatorsDemonstration.onErrorReturnExample();
    }

    /**
     * GET /api/v1/demo/error-handling/on-error-resume
     * 
     * ERROR HANDLING: onErrorResume - Replace with fallback stream
     * 
     * Demonstrates:
     * - Error occurs
     * - Instead of returning single value
     * - Calls alternative operation/stream
     * - Results from fallback emitted
     */
    @GetMapping("/error-handling/on-error-resume")
    @Operation(
        summary = "Demonstrate onErrorResume",
        description = """
            Shows error handling with onErrorResume(): call fallback operation on error.
            
            Pattern:
            ```
            primaryAPI()  // fails
                .onErrorResume(error -> fallbackAPI())  // call backup
            ```
            
            Key Characteristics:
            - Error suppressed
            - Fallback operation subscribed
            - Results from fallback emitted normally
            - Powerful recovery mechanism
            
            Use onErrorResume() for:
            - Calling alternative service
            - Circuit breaker pattern
            - Graceful degradation
            - Complex recovery logic
            
            Real-world uses:
            - Primary cache fails → hit database
            - API call fails → return cached data
            - Database down → use fallback service
            
            VS onErrorReturn:
            - onErrorReturn: single value
            - onErrorResume: another operation/stream
            """
    )
    @ApiResponse(responseCode = "200", description = "OnErrorResume demonstration")
    public Mono<String> demonstrateOnErrorResume() {
        log.info("GET /api/v1/demo/error-handling/on-error-resume");
        return OperatorsDemonstration.onErrorResumeExample();
    }

    /**
     * GET /api/v1/demo/error-handling/retry
     * 
     * ERROR HANDLING: retry - Retry failed operation N times
     * 
     * Demonstrates:
     * - First attempt fails
     * - Automatically retries up to 3 times
     * - Either succeeds or exhausts retries
     */
    @GetMapping("/error-handling/retry")
    @Operation(
        summary = "Demonstrate retry operator",
        description = """
            Shows error handling with retry(n): automatically retry failed operation.
            
            Pattern:
            ```
            flakeyAPI()
                .retry(3)  // Retry up to 3 times
            ```
            
            Behavior:
            - Attempt 1 fails → Attempt 2
            - Attempt 2 fails → Attempt 3
            - Attempt 3 fails → Attempt 4
            - Attempt 4 fails → Error propagated
            
            Key Characteristics:
            - Exponential backoff optional
            - Max retry count specified
            - Eventually gives up (fail-fast)
            - Resource cleanup important
            
            Use retry() for:
            - Temporary network failures
            - Transient errors
            - Flaky external APIs
            
            Advanced: retryWhen() for complex logic
            ```
            .retryWhen(errors ->
                errors.delayElement(Duration.ofSeconds(1))
                      .take(3)
            )
            ```
            """
    )
    @ApiResponse(responseCode = "200", description = "Retry demonstration")
    public Mono<String> demonstrateRetry() {
        log.info("GET /api/v1/demo/error-handling/retry");
        return OperatorsDemonstration.retryExample();
    }

    /**
     * GET /api/v1/demo/error-handling/timeout
     * 
     * ERROR HANDLING: timeout - Fail if takes too long
     * 
     * Demonstrates:
     * - Long-running operation
     * - 5-second timeout configured
     * - Fails with TimeoutException
     */
    @GetMapping("/error-handling/timeout")
    @Operation(
        summary = "Demonstrate timeout operator",
        description = """
            Shows error handling with timeout(duration): fail if takes too long.
            
            Pattern:
            ```
            slowAPI()
                .timeout(Duration.ofSeconds(5))
            ```
            
            Behavior:
            - Wait up to 5 seconds
            - If not done by then → TimeoutException
            - Stream terminated
            - Resources cleaned up
            
            Key Characteristics:
            - Prevents hanging indefinitely
            - Sets maximum wait time
            - Alternative: timeoutWith() for fallback
            
            Use timeout() for:
            - External API calls
            - Database queries
            - Any operation with unknown duration
            
            In production:
            - Always set reasonable timeouts
            - 30s typical HTTP timeout
            - 5s for internal service timeouts
            
            Recovery: Use timeoutWith() or onErrorResume()
            ```
            .timeout(Duration.ofSeconds(5), onTimeoutValue)
            .onErrorResume(TimeoutException.class, e -> fallback())
            ```
            """
    )
    @ApiResponse(responseCode = "200", description = "Timeout demonstration")
    public Mono<String> demonstrateTimeout() {
        log.info("GET /api/v1/demo/error-handling/timeout");
        return OperatorsDemonstration.timeoutExample();
    }

    /**
     * GET /api/v1/demo/do-on-operators
     * 
     * SIDE EFFECTS: do-on operators - Logging without modification
     * 
     * Demonstrates:
     * - doOnNext(): Log each element
     * - doOnError(): Log error event
     * - doOnComplete(): Log completion
     * - doOnFinally(): Cleanup after terminal event
     */
    @GetMapping("/do-on-operators")
    @Operation(
        summary = "Demonstrate do-on operators",
        description = """
            Shows do-on* operators: side effects without modifying stream.
            
            Available do-on operators:
            - doOnNext(consumer): Called for each element
            - doOnError(consumer): Called on error
            - doOnComplete(runnable): Called on completion
            - doOnFinally(runnable): Called after terminal event
            - doOnSubscribe(consumer): Called on subscription
            - doOnCancel(runnable): Called on cancellation
            - doOnRequest(consumer): Called on backpressure request
            
            Pattern:
            ```
            Flux.just("Hello")
                .doOnNext(item -> log.info("Emitting: " + item))
                .doOnComplete(() -> log.info("Completed"))
                .doOnError(error -> log.error("Error: " + error))
            ```
            
            Key Characteristics:
            - Non-intrusive (don't modify stream)
            - Useful for logging/debugging
            - Multiple do-on can be chained
            - Side effects executed in order
            
            Use do-on for:
            - Logging
            - Metrics/monitoring
            - Resource cleanup
            - Debugging
            
            IMPORTANT: NOT for business logic!
            - map(): business logic (transforms data)
            - doOnNext(): only side effects (logging)
            
            Order of execution:
            1. doOnSubscribe (subscription)
            2. doOnRequest (backpressure)
            3. doOnNext (each element)
            4. doOnComplete or doOnError (terminal)
            5. doOnFinally (cleanup)
            """
    )
    @ApiResponse(responseCode = "200", description = "Do-on operators demonstration")
    public Mono<String> demonstrateDoOnOperators() {
        log.info("GET /api/v1/demo/do-on-operators - demonstrating do-on operators");
        return OperatorsDemonstration.doOnExample();
    }
}
