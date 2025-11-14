package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Retry policy configuration
 * Feature 21: Automatic Retry with Exponential Backoff
 */
@Data
@Builder
public class RetryPolicy {

    /**
     * Maximum number of retry attempts
     */
    @Builder.Default
    private int maxAttempts = 3;

    /**
     * Initial backoff duration
     */
    @Builder.Default
    private Duration initialBackoff = Duration.ofMillis(100);

    /**
     * Maximum backoff duration
     */
    @Builder.Default
    private Duration maxBackoff = Duration.ofSeconds(30);

    /**
     * Backoff multiplier (for exponential backoff)
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;

    /**
     * Retry strategy
     */
    @Builder.Default
    private RetryStrategy strategy = RetryStrategy.EXPONENTIAL;

    /**
     * Jitter to add randomness to backoff
     */
    @Builder.Default
    private boolean enableJitter = true;

    /**
     * Predicate to determine if error is retryable
     */
    @Builder.Default
    private Predicate<Throwable> retryableExceptions = ex -> true;

    /**
     * Retry strategies
     */
    public enum RetryStrategy {
        FIXED,        // Fixed delay between retries
        LINEAR,       // Linear increase
        EXPONENTIAL,  // Exponential backoff (default)
        FIBONACCI     // Fibonacci sequence
    }

    /**
     * Create a default retry policy
     */
    public static RetryPolicy createDefault() {
        return RetryPolicy.builder().build();
    }

    /**
     * Create retry policy with custom max attempts
     */
    public static RetryPolicy withMaxAttempts(int maxAttempts) {
        return RetryPolicy.builder()
                .maxAttempts(maxAttempts)
                .build();
    }
}
