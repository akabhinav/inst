package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

/**
 * Chaos engineering report
 */
@Data
@Builder
public class ChaosReport {
    private long messagesInjected;
    private long failuresInjected;
    private double recoveryRate;
    private long dataLossCount;
}
