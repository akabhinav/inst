package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Graceful degradation configuration
 * Feature 59: Graceful Degradation
 */
@Data
@Builder
public class Degradation {

    /**
     * Enable graceful degradation
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Fallback provider when primary fails
     */
    private String fallbackProvider;

    /**
     * Cache messages locally if all brokers down
     */
    @Builder.Default
    private boolean cacheMessages = true;

    /**
     * Maximum cache size
     */
    @Builder.Default
    private int maxCacheSize = 100000;

    /**
     * How long to serve stale data
     */
    @Builder.Default
    private Duration stalenessLimit = Duration.ofMinutes(5);

    /**
     * Callback when degradation level changes
     */
    private Consumer<DegradationLevel> onDegradation;

    /**
     * Degradation levels
     */
    public enum DegradationLevel {
        NORMAL,         // All systems operational
        DEGRADED,       // Some systems down, using fallback
        CRITICAL        // All systems down, using cache
    }

    public static Degradation createDefault() {
        return Degradation.builder().build();
    }
}
