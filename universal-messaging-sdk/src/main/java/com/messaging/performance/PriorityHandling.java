package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

/**
 * Priority handling configuration
 * Feature 53: Priority Queues & Message Prioritization
 */
@Data
@Builder
public class PriorityHandling {

    /**
     * Enable priority handling
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Number of threads for high priority messages
     */
    @Builder.Default
    private int highPriorityThreads = 20;

    /**
     * Number of threads for medium priority messages
     */
    @Builder.Default
    private int mediumPriorityThreads = 10;

    /**
     * Number of threads for low priority messages
     */
    @Builder.Default
    private int lowPriorityThreads = 5;

    /**
     * Prevent starvation of low priority messages
     */
    @Builder.Default
    private boolean starvationPrevention = true;

    /**
     * Priority handling strategy
     */
    @Builder.Default
    private Strategy strategy = Strategy.THREAD_POOL;

    /**
     * Priority strategies
     */
    public enum Strategy {
        THREAD_POOL,            // More threads for high priority
        SEPARATE_QUEUES,        // Physical separation
        WEIGHTED_FAIR_QUEUING,  // Process N high-priority for every 1 low
        DEADLINE_BASED          // Process by deadline, not FIFO
    }

    public static PriorityHandling createDefault() {
        return PriorityHandling.builder().build();
    }
}
