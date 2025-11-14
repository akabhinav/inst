package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.util.List;

/**
 * Chaos engineering configuration
 * Feature 58: Chaos Engineering Integration
 */
@Data
@Builder
public class ChaosEngineering {

    /**
     * Enable chaos engineering
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Chaos scenarios to inject
     */
    @Singular
    private List<ChaosScenario> scenarios;

    /**
     * Only enable in specific environment
     */
    @Builder.Default
    private Environment environment = Environment.STAGING;

    /**
     * Environments
     */
    public enum Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION  // Use with extreme caution!
    }

    /**
     * Chaos scenario
     */
    @Data
    @Builder
    public static class ChaosScenario {
        private ScenarioType type;
        private double probability;  // 0.0 to 1.0
        private Duration delay;
        private int copies;  // For duplicate messages
        private DataCorruption corruption;

        public enum ScenarioType {
            NETWORK_LATENCY,
            MESSAGE_LOSS,
            DUPLICATE_MESSAGE,
            BROKER_DOWN,
            CORRUPTED_DATA,
            PARTIAL_FAILURE
        }
    }

    /**
     * Data corruption types
     */
    public enum DataCorruption {
        RANDOM_BYTES,
        TRUNCATE,
        NULL_FIELDS
    }

    public static ChaosEngineering createDefault() {
        return ChaosEngineering.builder()
                .enabled(false)
                .build();
    }
}
