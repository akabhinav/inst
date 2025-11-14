package com.messaging.reliability.impl;

import com.messaging.reliability.RateLimit;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiter handler
 * Feature 29: Rate Limiting
 */
@Slf4j
public class RateLimiterHandler {

    private final RateLimit config;
    private final AtomicInteger tokens;
    private final AtomicLong lastRefillTime;
    private final AtomicInteger requestCount;
    private final AtomicLong windowStartTime;

    public RateLimiterHandler(RateLimit config) {
        this.config = config;
        this.tokens = new AtomicInteger(config.getMaxRequests());
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        this.requestCount = new AtomicInteger(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());

        log.info("Rate limiter initialized: {} requests per {} using {}",
            config.getMaxRequests(), config.getWindow(), config.getAlgorithm());
    }

    /**
     * Try to acquire permission for a request
     * Returns true if allowed, false if rate limit exceeded
     */
    public boolean tryAcquire() {
        if (!config.isEnabled()) {
            return true;
        }

        boolean allowed = switch (config.getAlgorithm()) {
            case TOKEN_BUCKET -> tryAcquireTokenBucket();
            case LEAKY_BUCKET -> tryAcquireLeakyBucket();
            case FIXED_WINDOW -> tryAcquireFixedWindow();
            case SLIDING_WINDOW -> tryAcquireSlidingWindow();
        };

        if (!allowed) {
            log.warn("Rate limit exceeded: {} requests per {}", config.getMaxRequests(), config.getWindow());
            handleRateLimitExceeded();
        }

        return allowed;
    }

    /**
     * Token bucket algorithm
     */
    private boolean tryAcquireTokenBucket() {
        // Refill tokens
        long now = System.currentTimeMillis();
        long timeSinceLastRefill = now - lastRefillTime.get();

        if (timeSinceLastRefill >= config.getWindow().toMillis()) {
            tokens.set(config.getMaxRequests());
            lastRefillTime.set(now);
        }

        // Try to acquire token
        int currentTokens = tokens.get();
        if (currentTokens > 0) {
            tokens.decrementAndGet();
            return true;
        }

        return false;
    }

    /**
     * Leaky bucket algorithm
     */
    private boolean tryAcquireLeakyBucket() {
        // Similar to token bucket but with constant leak rate
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime.get();

        // Calculate how many tokens have leaked out
        long leakedTokens = elapsed * config.getMaxRequests() / config.getWindow().toMillis();

        if (leakedTokens > 0) {
            int currentTokens = tokens.get();
            int newTokens = Math.min(currentTokens + (int) leakedTokens, config.getMaxRequests());
            tokens.set(newTokens);
            lastRefillTime.set(now);
        }

        // Try to acquire token
        int currentTokens = tokens.get();
        if (currentTokens > 0) {
            tokens.decrementAndGet();
            return true;
        }

        return false;
    }

    /**
     * Fixed window algorithm
     */
    private boolean tryAcquireFixedWindow() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();

        // Check if we're in a new window
        if (now - windowStart >= config.getWindow().toMillis()) {
            requestCount.set(0);
            windowStartTime.set(now);
        }

        // Check if under limit
        int count = requestCount.incrementAndGet();
        return count <= config.getMaxRequests();
    }

    /**
     * Sliding window algorithm
     */
    private boolean tryAcquireSlidingWindow() {
        // Simplified sliding window (in production, use time-series data structure)
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;

        if (elapsed >= config.getWindow().toMillis()) {
            // New window
            requestCount.set(1);
            windowStartTime.set(now);
            return true;
        }

        // Calculate weighted count
        double windowProgress = (double) elapsed / config.getWindow().toMillis();
        int previousWindowCount = requestCount.get();
        double effectiveCount = previousWindowCount * (1 - windowProgress) + 1;

        if (effectiveCount <= config.getMaxRequests()) {
            requestCount.incrementAndGet();
            return true;
        }

        return false;
    }

    /**
     * Handle rate limit exceeded based on behavior
     */
    private void handleRateLimitExceeded() {
        switch (config.getBehavior()) {
            case BLOCK -> {
                // Block until rate limit resets (sleep)
                try {
                    long sleepTime = calculateTimeUntilReset();
                    log.debug("Blocking for {}ms until rate limit resets", sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            case DROP -> {
                // Drop the request (already handled by returning false)
                log.debug("Request dropped due to rate limit");
            }
            case THROW_EXCEPTION -> {
                throw new RateLimitExceededException(
                    String.format("Rate limit exceeded: %d requests per %s",
                        config.getMaxRequests(), config.getWindow())
                );
            }
        }
    }

    /**
     * Calculate time until rate limit resets
     */
    private long calculateTimeUntilReset() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;
        return Math.max(0, config.getWindow().toMillis() - elapsed);
    }

    /**
     * Get current request count
     */
    public int getCurrentCount() {
        return requestCount.get();
    }

    /**
     * Get available tokens
     */
    public int getAvailableTokens() {
        return tokens.get();
    }

    /**
     * Reset rate limiter
     */
    public void reset() {
        tokens.set(config.getMaxRequests());
        requestCount.set(0);
        lastRefillTime.set(System.currentTimeMillis());
        windowStartTime.set(System.currentTimeMillis());
        log.debug("Rate limiter reset");
    }

    /**
     * Rate limit exceeded exception
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
