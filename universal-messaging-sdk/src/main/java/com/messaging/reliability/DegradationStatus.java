package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

/**
 * Current degradation status
 */
@Data
@Builder
public class DegradationStatus {
    private Degradation.DegradationLevel level;
    private String reason;
    private boolean usingFallback;
    private boolean usingCache;
    private int cachedMessages;
}
