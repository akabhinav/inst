package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of saga execution
 */
@Data
@Builder
public class SagaResult {
    private boolean success;
    private String sagaId;
    private int completedSteps;
    private int totalSteps;
    private List<String> errors;
    private boolean compensated;
}
