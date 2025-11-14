package com.messaging.core;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Overall message metrics
 * Feature 32: Metrics & Analytics
 */
@Data
@Builder
public class MessageMetrics {

    // Throughput metrics
    private long totalMessagesSent;
    private long totalMessagesReceived;
    private long totalMessagesFailed;
    private double messagesPerSecond;

    // Latency metrics (in milliseconds)
    private double avgLatencyMs;
    private double p50LatencyMs;
    private double p95LatencyMs;
    private double p99LatencyMs;

    // Size metrics
    private long totalBytesIn;
    private long totalBytesOut;
    private long avgMessageSizeBytes;

    // Error metrics
    private long totalErrors;
    private double errorRate;

    // Per-topic metrics
    private Map<String, TopicMetrics> topicMetrics;

    /**
     * Per-topic metrics
     */
    @Data
    @Builder
    public static class TopicMetrics {
        private String topic;
        private long messageCount;
        private double avgLatencyMs;
        private long errorCount;
        private double throughput;
    }
}
