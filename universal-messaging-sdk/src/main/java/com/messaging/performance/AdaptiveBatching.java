package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Adaptive batch processing configuration
 * Feature 52: Adaptive Batch Processing
 *
 * Dynamically optimize batch sizes based on load
 */
@Data
@Builder
public class AdaptiveBatching {

    /**
     * Enable adaptive batching
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Minimum batch size
     */
    @Builder.Default
    private int minBatchSize = 10;

    /**
     * Maximum batch size
     */
    @Builder.Default
    private int maxBatchSize = 1000;

    /**
     * Target latency for batching
     */
    @Builder.Default
    private Duration targetLatency = Duration.ofMillis(100);

    /**
     * Batching algorithm
     */
    @Builder.Default
    private BatchingAlgorithm algorithm = BatchingAlgorithm.GRADIENT_BASED;

    /**
     * Batching algorithms
     */
    public enum BatchingAlgorithm {
        RULE_BASED,         // Simple rules
        GRADIENT_BASED,     // Optimize for target latency
        ML_OPTIMIZED,       // Neural network learns optimal batching
        PREDICTIVE          // Anticipate load spikes
    }

    public static AdaptiveBatching createDefault() {
        return AdaptiveBatching.builder().build();
    }
}
