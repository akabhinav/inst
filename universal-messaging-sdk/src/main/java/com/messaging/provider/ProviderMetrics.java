package com.messaging.provider;

import lombok.Builder;
import lombok.Data;

/**
 * Provider-specific metrics
 */
@Data
@Builder
public class ProviderMetrics {
    private long messagesSent;
    private long messagesReceived;
    private long messagesFailedSend;
    private long messagesFailedReceive;
    private double avgSendLatencyMs;
    private double avgReceiveLatencyMs;
    private long bytesIn;
    private long bytesOut;
    private int activeConnections;
}
