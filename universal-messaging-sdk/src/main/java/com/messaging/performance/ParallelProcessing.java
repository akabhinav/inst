package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

/**
 * Parallel processing configuration with guaranteed order
 * Feature 54: Parallel Processing with Guaranteed Order
 */
@Data
@Builder
public class ParallelProcessing {

    /**
     * Number of parallel threads
     */
    @Builder.Default
    private int concurrency = 50;

    /**
     * Ordering key extractor (messages with same key processed in order)
     */
    private Function<Object, String> orderingKey;

    /**
     * Guarantee ordering per key
     */
    @Builder.Default
    private boolean guaranteeOrder = true;

    /**
     * Use work stealing for load balancing
     */
    @Builder.Default
    private boolean workStealing = true;

    public static ParallelProcessing createDefault() {
        return ParallelProcessing.builder().build();
    }
}
