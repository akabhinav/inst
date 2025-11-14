package com.messaging.reliability.impl;

import com.messaging.reliability.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker handler
 * Feature 28: Circuit Breaker
 */
@Slf4j
public class CircuitBreakerHandler {

    private final CircuitBreaker config;
    private final AtomicReference<CircuitBreaker.State> state =
        new AtomicReference<>(CircuitBreaker.State.CLOSED);
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastStateChangeTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger halfOpenSuccesses = new AtomicInteger(0);

    public CircuitBreakerHandler(CircuitBreaker config) {
        this.config = config;
    }

    /**
     * Check if call is allowed
     */
    public boolean allowRequest() {
        if (!config.isEnabled()) {
            return true;
        }

        CircuitBreaker.State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                // Allow all requests
                return true;

            case OPEN:
                // Check if we should transition to half-open
                long timeSinceOpen = System.currentTimeMillis() - lastStateChangeTime.get();
                if (timeSinceOpen >= config.getWaitDurationInOpenState().toMillis()) {
                    log.info("Circuit breaker transitioning from OPEN to HALF_OPEN");
                    transitionTo(CircuitBreaker.State.HALF_OPEN);
                    return true;
                }
                // Reject request
                log.warn("Circuit breaker is OPEN, rejecting request");
                return false;

            case HALF_OPEN:
                // Allow limited number of requests
                if (halfOpenSuccesses.get() < config.getPermittedNumberOfCallsInHalfOpenState()) {
                    return true;
                }
                return false;

            default:
                return true;
        }
    }

    /**
     * Record successful call
     */
    public void recordSuccess() {
        if (!config.isEnabled()) {
            return;
        }

        CircuitBreaker.State currentState = state.get();
        callCount.incrementAndGet();

        if (currentState == CircuitBreaker.State.HALF_OPEN) {
            int successes = halfOpenSuccesses.incrementAndGet();
            log.debug("Half-open successes: {}/{}", successes,
                config.getPermittedNumberOfCallsInHalfOpenState());

            if (successes >= config.getPermittedNumberOfCallsInHalfOpenState()) {
                log.info("Circuit breaker transitioning from HALF_OPEN to CLOSED");
                transitionTo(CircuitBreaker.State.CLOSED);
                resetCounts();
            }
        }

        // Check if we should close the circuit
        evaluateCircuitState();
    }

    /**
     * Record failed call
     */
    public void recordFailure() {
        if (!config.isEnabled()) {
            return;
        }

        CircuitBreaker.State currentState = state.get();
        callCount.incrementAndGet();
        failureCount.incrementAndGet();

        log.debug("Recorded failure, total calls: {}, failures: {}",
            callCount.get(), failureCount.get());

        if (currentState == CircuitBreaker.State.HALF_OPEN) {
            log.warn("Failure in HALF_OPEN state, transitioning back to OPEN");
            transitionTo(CircuitBreaker.State.OPEN);
            resetCounts();
            return;
        }

        // Evaluate circuit state
        evaluateCircuitState();
    }

    /**
     * Evaluate circuit state based on failure rate
     */
    private void evaluateCircuitState() {
        int calls = callCount.get();
        int failures = failureCount.get();

        // Need minimum number of calls
        if (calls < config.getMinimumNumberOfCalls()) {
            return;
        }

        // Calculate failure rate
        double failureRate = (double) failures / calls;

        log.debug("Circuit breaker metrics - calls: {}, failures: {}, failure rate: {:.2f}",
            calls, failures, failureRate * 100);

        // Check if we should open the circuit
        if (failureRate >= config.getFailureRateThreshold() &&
            state.get() == CircuitBreaker.State.CLOSED) {

            log.error("Circuit breaker opening! Failure rate: {:.2f}% (threshold: {:.2f}%)",
                failureRate * 100, config.getFailureRateThreshold() * 100);

            transitionTo(CircuitBreaker.State.OPEN);
            resetCounts();
        }

        // Reset sliding window if needed
        if (calls >= config.getSlidingWindowSize()) {
            resetCounts();
        }
    }

    /**
     * Transition to new state
     */
    private void transitionTo(CircuitBreaker.State newState) {
        CircuitBreaker.State oldState = state.getAndSet(newState);
        lastStateChangeTime.set(System.currentTimeMillis());

        if (newState == CircuitBreaker.State.HALF_OPEN) {
            halfOpenSuccesses.set(0);
        }

        log.info("Circuit breaker state changed: {} -> {}", oldState, newState);

        // Trigger callback if configured
        if (config.getOnStateChange() != null) {
            double failureRate = callCount.get() > 0 ?
                (double) failureCount.get() / callCount.get() : 0.0;

            CircuitBreaker.CircuitBreakerEvent event = CircuitBreaker.CircuitBreakerEvent.builder()
                .fromState(oldState)
                .toState(newState)
                .reason("Failure rate: " + String.format("%.2f%%", failureRate * 100))
                .failureRate(failureRate)
                .build();

            config.getOnStateChange().accept(event);
        }
    }

    /**
     * Reset call and failure counts
     */
    private void resetCounts() {
        callCount.set(0);
        failureCount.set(0);
    }

    /**
     * Get current state
     */
    public CircuitBreaker.State getState() {
        return state.get();
    }

    /**
     * Get current failure rate
     */
    public double getFailureRate() {
        int calls = callCount.get();
        if (calls == 0) {
            return 0.0;
        }
        return (double) failureCount.get() / calls;
    }

    /**
     * Force circuit to open (for testing)
     */
    public void forceOpen() {
        transitionTo(CircuitBreaker.State.OPEN);
    }

    /**
     * Force circuit to close (for testing)
     */
    public void forceClose() {
        transitionTo(CircuitBreaker.State.CLOSED);
        resetCounts();
    }
}
