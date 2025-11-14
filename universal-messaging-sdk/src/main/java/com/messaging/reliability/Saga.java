package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Saga pattern for distributed transactions
 * Feature 57: Saga Pattern Support
 */
@Data
@Builder
public class Saga {

    /**
     * Saga ID
     */
    private String id;

    /**
     * Saga steps
     */
    @Singular
    private List<SagaStep> steps;

    /**
     * Saga pattern type
     */
    @Builder.Default
    private PatternType pattern = PatternType.ORCHESTRATION;

    /**
     * Persistence for long-running sagas
     */
    @Builder.Default
    private SagaPersistence persistence = SagaPersistence.IN_MEMORY;

    /**
     * Saga pattern types
     */
    public enum PatternType {
        CHOREOGRAPHY,       // Event-driven (decentralized)
        ORCHESTRATION       // Centralized coordinator
    }

    /**
     * Saga persistence
     */
    public enum SagaPersistence {
        IN_MEMORY,          // Fast but lost on restart
        DATABASE,           // Survives restarts
        REDIS               // Distributed persistence
    }

    /**
     * Saga step
     */
    @Data
    @Builder
    public static class SagaStep {
        private String name;
        private Supplier<Void> action;
        private Supplier<Void> compensation;
        @Builder.Default
        private Duration timeout = Duration.ofSeconds(30);
    }
}
