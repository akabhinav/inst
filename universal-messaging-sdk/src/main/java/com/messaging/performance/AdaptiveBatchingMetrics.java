package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

/**
 * Metrics for adaptive batching
 */
@Data
@Builder
public class AdaptiveBatchingMetrics {
    private double averageBatchSize;
    private double improvementPercent;
    private double costSavings;
    private long totalBatches;
    private long totalMessages;
}
