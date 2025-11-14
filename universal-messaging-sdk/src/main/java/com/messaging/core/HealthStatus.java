package com.messaging.core;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Health status of the messaging system
 * Feature 8: Broker Health Monitoring
 */
@Data
@Builder
public class HealthStatus {

    /**
     * Overall health status
     */
    @Builder.Default
    private Status status = Status.UNKNOWN;

    /**
     * Health check timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Individual component healths
     */
    private List<ComponentHealth> components;

    /**
     * Overall status enum
     */
    public enum Status {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    /**
     * Health of individual component
     */
    @Data
    @Builder
    public static class ComponentHealth {
        private String name;
        private Status status;
        private String message;
        private Double responseTimeMs;
    }
}
