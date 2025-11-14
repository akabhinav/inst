package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Circuit Breaker configuration
 * Feature 28: Circuit Breaker
 *
 * Prevents cascading failures by "opening" circuit when errors exceed threshold
 */
@Data
@Builder
public class CircuitBreaker {

    /**
     * Enable circuit breaker
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Failure rate threshold (0.0 to 1.0)
     * Circuit opens if failure rate exceeds this
     */
    @Builder.Default
    private double failureRateThreshold = 0.5;

    /**
     * Minimum number of calls before calculating failure rate
     */
    @Builder.Default
    private int minimumNumberOfCalls = 10;

    /**
     * Wait duration in open state before attempting half-open
     */
    @Builder.Default
    private Duration waitDurationInOpenState = Duration.ofSeconds(60);

    /**
     * Number of permitted calls in half-open state
     */
    @Builder.Default
    private int permittedNumberOfCallsInHalfOpenState = 5;

    /**
     * Sliding window size for calculating failure rate
     */
    @Builder.Default
    private int slidingWindowSize = 100;

    /**
     * Sliding window type
     */
    @Builder.Default
    private SlidingWindowType slidingWindowType = SlidingWindowType.COUNT_BASED;

    /**
     * Callback when circuit opens
     */
    private Consumer<CircuitBreakerEvent> onStateChange;

    /**
     * Sliding window types
     */
    public enum SlidingWindowType {
        COUNT_BASED,    // Based on number of calls
        TIME_BASED      // Based on time duration
    }

    /**
     * Circuit breaker states
     */
    public enum State {
        CLOSED,         // Normal operation
        OPEN,           // Rejecting calls
        HALF_OPEN       // Testing if backend recovered
    }

    /**
     * Circuit breaker event
     */
    @Data
    @Builder
    public static class CircuitBreakerEvent {
        private State fromState;
        private State toState;
        private String reason;
        private double failureRate;
    }

    /**
     * Create default circuit breaker
     */
    public static CircuitBreaker createDefault() {
        return CircuitBreaker.builder().build();
    }
}
