package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.function.Consumer;

/**
 * Self-healing configuration
 * Feature 60: Self-Healing & Auto-Recovery
 */
@Data
@Builder
public class SelfHealing {

    /**
     * Enable self-healing
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Healing strategies
     */
    @Singular
    private List<HealingStrategy> strategies;

    /**
     * Callback for healing events
     */
    private Consumer<HealingEvent> onHealing;

    /**
     * Healing strategy
     */
    @Data
    @Builder
    public static class HealingStrategy {
        private Trigger trigger;
        private Action action;
        private int maxAttempts;

        public enum Trigger {
            CONNECTION_LOST,
            CONSUMER_LAG_HIGH,
            MEMORY_HIGH,
            HIGH_ERROR_RATE,
            BROKER_DOWN
        }

        public enum Action {
            AUTO_RECONNECT,
            ADD_CONSUMERS,
            EVICT_CACHE,
            SWITCH_TO_BACKUP,
            RESTART_CONSUMER
        }
    }

    /**
     * Healing event
     */
    @Data
    @Builder
    public static class HealingEvent {
        private String type;
        private String problem;
        private String action;
        private String result;
    }

    public static SelfHealing createDefault() {
        return SelfHealing.builder()
                .enabled(true)
                .build();
    }
}
