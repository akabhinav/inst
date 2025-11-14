package com.messaging.reliability.impl;

import com.messaging.reliability.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Retry executor with exponential backoff
 * Feature 21: Automatic Retry with Exponential Backoff
 */
@Slf4j
public class RetryExecutor {

    private final RetryPolicy policy;
    private final Random random = new Random();

    public RetryExecutor(RetryPolicy policy) {
        this.policy = policy;
    }

    /**
     * Execute operation with retry logic
     */
    public <T> T execute(Supplier<T> operation) {
        int attempt = 0;
        Throwable lastException = null;

        while (attempt < policy.getMaxAttempts()) {
            try {
                log.debug("Attempt {}/{}", attempt + 1, policy.getMaxAttempts());
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                // Check if exception is retryable
                if (!policy.getRetryableExceptions().test(e)) {
                    log.warn("Exception is not retryable: {}", e.getMessage());
                    throw new RuntimeException("Non-retryable exception", e);
                }

                if (attempt >= policy.getMaxAttempts()) {
                    log.error("Max retry attempts ({}) reached", policy.getMaxAttempts());
                    break;
                }

                // Calculate backoff duration
                Duration backoff = calculateBackoff(attempt);
                log.info("Retry attempt {}/{} after {}ms", attempt, policy.getMaxAttempts(), backoff.toMillis());

                try {
                    Thread.sleep(backoff.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw new RuntimeException("Failed after " + policy.getMaxAttempts() + " attempts", lastException);
    }

    /**
     * Execute operation with retry logic (reactive)
     */
    public <T> Mono<T> executeAsync(Mono<T> operation) {
        return operation.retryWhen(
            Retry.backoff(policy.getMaxAttempts(), policy.getInitialBackoff())
                .maxBackoff(policy.getMaxBackoff())
                .filter(policy.getRetryableExceptions())
                .doBeforeRetry(signal -> {
                    log.info("Retrying operation, attempt: {}, error: {}",
                        signal.totalRetries() + 1, signal.failure().getMessage());
                })
        );
    }

    /**
     * Calculate backoff duration based on strategy
     */
    private Duration calculateBackoff(int attempt) {
        long backoffMs;

        switch (policy.getStrategy()) {
            case FIXED -> backoffMs = policy.getInitialBackoff().toMillis();

            case LINEAR -> backoffMs = policy.getInitialBackoff().toMillis() * attempt;

            case EXPONENTIAL -> {
                backoffMs = (long) (policy.getInitialBackoff().toMillis() *
                    Math.pow(policy.getBackoffMultiplier(), attempt - 1));
            }

            case FIBONACCI -> {
                backoffMs = policy.getInitialBackoff().toMillis() * fibonacci(attempt);
            }

            default -> backoffMs = policy.getInitialBackoff().toMillis();
        }

        // Apply max backoff limit
        backoffMs = Math.min(backoffMs, policy.getMaxBackoff().toMillis());

        // Add jitter if enabled
        if (policy.isEnableJitter()) {
            double jitter = random.nextDouble() * 0.2; // 0-20% jitter
            backoffMs = (long) (backoffMs * (1 + jitter));
        }

        return Duration.ofMillis(backoffMs);
    }

    /**
     * Calculate Fibonacci number
     */
    private long fibonacci(int n) {
        if (n <= 1) return 1;
        long a = 1, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}
