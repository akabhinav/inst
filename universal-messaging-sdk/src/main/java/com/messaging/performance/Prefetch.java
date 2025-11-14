package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

/**
 * Message prefetching configuration
 * Feature 55: Message Prefetching & Pipelining
 */
@Data
@Builder
public class Prefetch {

    /**
     * Enable prefetching
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Prefetch buffer size
     */
    @Builder.Default
    private int bufferSize = 1000;

    /**
     * Prefetch strategy
     */
    @Builder.Default
    private PrefetchStrategy strategy = PrefetchStrategy.ADAPTIVE;

    /**
     * Prefetch strategies
     */
    public enum PrefetchStrategy {
        FIXED,          // Always prefetch N messages
        ADAPTIVE,       // Adjust based on processing speed
        PREDICTIVE,     // ML predicts optimal prefetch size
        WINDOWED        // Sliding window of messages
    }

    public static Prefetch createDefault() {
        return Prefetch.builder().build();
    }
}
