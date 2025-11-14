package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Rate limiting configuration
 * Feature 29: Rate Limiting
 */
@Data
@Builder
public class RateLimit {

    /**
     * Enable rate limiting
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Maximum number of requests per time window
     */
    @Builder.Default
    private int maxRequests = 1000;

    /**
     * Time window
     */
    @Builder.Default
    private Duration window = Duration.ofSeconds(1);

    /**
     * Rate limiting algorithm
     */
    @Builder.Default
    private Algorithm algorithm = Algorithm.TOKEN_BUCKET;

    /**
     * Behavior when limit exceeded
     */
    @Builder.Default
    private Behavior behavior = Behavior.BLOCK;

    /**
     * Rate limiting algorithms
     */
    public enum Algorithm {
        TOKEN_BUCKET,       // Smooth rate limiting
        LEAKY_BUCKET,       // Constant output rate
        FIXED_WINDOW,       // Simple but allows bursts
        SLIDING_WINDOW      // More accurate than fixed
    }

    /**
     * Behavior when rate limit exceeded
     */
    public enum Behavior {
        BLOCK,              // Block until rate limit resets
        DROP,               // Drop the message
        THROW_EXCEPTION     // Throw exception
    }

    public static RateLimit createDefault() {
        return RateLimit.builder().build();
    }

    public static RateLimit perSecond(int requests) {
        return RateLimit.builder()
                .enabled(true)
                .maxRequests(requests)
                .window(Duration.ofSeconds(1))
                .build();
    }
}
