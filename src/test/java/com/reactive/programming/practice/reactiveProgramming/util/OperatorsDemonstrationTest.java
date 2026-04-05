package com.reactive.programming.practice.reactiveProgramming.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * OperatorsDemonstrationTest - Test Reactor operators
 * 
 * Tests all demonstrated operators:
 * - Transformation: map, flatMap, switchMap
 * - Filtering: filter, take, skip, distinct
 * - Combining: zip, merge, concat
 * - Error Handling: onErrorReturn, onErrorResume, retry, timeout
 * - Side Effects: doOn*
 * 
 * Uses StepVerifier for assertions on reactive streams
 */
@DisplayName("Reactor Operators Tests")
class OperatorsDemonstrationTest {

    @Test
    @DisplayName("MAP - Should transform each element (1:1 mapping)")
    void testMapOperator() {
        // Act & Assert - Verify each number multiplied by 10
        StepVerifier.create(OperatorsDemonstration.mapExample())
                .expectNext(10)
                .expectNext(20)
                .expectNext(30)
                .expectNext(40)
                .expectNext(50)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("FLATMAP - Should transform to async and flatten results")
    void testFlatMapOperator() {
        // Act & Assert - Verify async transformation
        StepVerifier.create(OperatorsDemonstration.flatMapExample())
                .expectNextCount(3) // Should emit 3 items after async lookup
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("SWITCHMAP - Should cancel previous, keep latest")
    void testSwitchMapOperator() {
        // Act & Assert - SwitchMap cancels previous subscriptions
        StepVerifier.create(OperatorsDemonstration.switchMapExample())
                .expectNextCount(3) // Results from latest emissions
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("FILTER - Should keep only elements matching predicate")
    void testFilterOperator() {
        // Act & Assert - Verify only even numbers pass through
        StepVerifier.create(OperatorsDemonstration.filterExample())
                .expectNext(2)
                .expectNext(4)
                .expectNext(6)
                .expectNext(8)
                .expectNext(10)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("TAKE - Should emit only first N elements")
    void testTakeOperator() {
        // Act & Assert - Verify only first 5 elements emitted (range 1-100, take 5)
        StepVerifier.create(OperatorsDemonstration.takeExample())
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .expectNext(5)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("SKIP - Should skip first N elements")
    void testSkipOperator() {
        // Act & Assert - Verify first 5 elements skipped (range 1-10, skip 5 gives 6-10)
        StepVerifier.create(OperatorsDemonstration.skipExample())
                .expectNext(6)
                .expectNext(7)
                .expectNext(8)
                .expectNext(9)
                .expectNext(10)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("DISTINCT - Should remove duplicate elements")
    void testDistinctOperator() {
        // Act & Assert - Verify duplicates removed [1,2,2,3,3,3,4,5,5] becomes [1,2,3,4,5]
        StepVerifier.create(OperatorsDemonstration.distinctExample())
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .expectNext(5)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ZIP - Should combine elements from multiple streams")
    void testZipOperator() {
        // Act & Assert - Verify paired results from zip (3 users, 3 ages = 3 pairs)
        StepVerifier.create(OperatorsDemonstration.zipExample())
                .expectNextCount(3) // 3 zipped pairs (Alice/25, Bob/30, Charlie/35)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("MERGE - Should interleave elements from multiple streams")
    void testMergeOperator() {
        // Act & Assert - Verify interleaved results
        StepVerifier.create(OperatorsDemonstration.mergeExample())
                .expectNextCount(6) // Total 6 elements from merged streams
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("CONCAT - Should concatenate streams sequentially")
    void testConcatOperator() {
        // Act & Assert - Verify sequential combination ([1,2,3] + [10,20,30] = 6 items)
        StepVerifier.create(OperatorsDemonstration.concatExample())
                .expectNextCount(6) // All elements in sequence [1,2,3,10,20,30]
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ONERRORRETURN - Should return default value on error")
    void testOnErrorReturnOperator() {
        // Act & Assert - Verify default value returned (FALLBACK_VALUE)
        StepVerifier.create(OperatorsDemonstration.onErrorReturnExample())
                .expectNext("FALLBACK_VALUE")
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ONERRORRESUME - Should call alternative stream on error")
    void testOnErrorResumeOperator() {
        // Act & Assert - Verify alternative Mono called (fallback API returns "Data from fallback API")
        StepVerifier.create(OperatorsDemonstration.onErrorResumeExample())
                .expectNext("Data from fallback API")
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("RETRY - Should retry operation on error")
    void testRetryOperator() {
        // Act & Assert - Verify retry behavior
        StepVerifier.create(OperatorsDemonstration.retryExample())
                .expectError() // Should eventually error after retries
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("TIMEOUT - Should fail if operation takes too long")
    void testTimeoutOperator() {
        // Act & Assert - Verify timeout error after duration
        StepVerifier.create(OperatorsDemonstration.timeoutExample())
                .expectError() // Should timeout
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("DOON - Should execute side effects without modifying stream")
    void testDoOnOperator() {
        // Act & Assert - Verify doOn operators execute without modifying stream (returns just "Hello")
        StepVerifier.create(OperatorsDemonstration.doOnExample())
                .expectNext("Hello")
                .expectComplete()
                .verify();
    }
}
