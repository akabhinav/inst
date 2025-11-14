package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Process mining configuration for analyzing and optimizing message processing workflows.
 *
 * Enables process intelligence capabilities to discover, analyze, and optimize
 * message handling patterns and workflows within the messaging system.
 *
 * Feature 69: Process Mining and Intelligence
 *
 * @author Messaging SDK
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessIntelligence {

    /**
     * Flag to enable or disable process intelligence and mining.
     */
    private boolean enabled;

    /**
     * Process mining configuration.
     */
    private ProcessMining processMining;

    /**
     * Process mining configuration for workflow analysis.
     * Captures and analyzes message processing patterns and bottlenecks.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessMining {

        /**
         * Flag to enable event logging for process mining.
         */
        private boolean enabled;

        /**
         * Minimum process frequency threshold for discovery.
         * Filters out infrequent process variants.
         */
        @Builder.Default
        private double minFrequencyThreshold = 0.05;

        /**
         * Flag to enable automatic bottleneck detection.
         */
        @Builder.Default
        private boolean detectBottlenecks = true;

        /**
         * Flag to enable compliance checking against process models.
         */
        @Builder.Default
        private boolean enableComplianceCheck = true;

        /**
         * Flag to enable automated optimization suggestions.
         */
        @Builder.Default
        private boolean enableOptimizationSuggestions = true;

        /**
         * Maximum number of process variants to track.
         */
        @Builder.Default
        private int maxVariants = 1000;

        /**
         * Event history retention period in days.
         */
        @Builder.Default
        private int eventRetentionDays = 90;

        /**
         * Flag to enable real-time process analysis.
         */
        @Builder.Default
        private boolean realtimeAnalysis = false;

        /**
         * Batch processing interval in seconds for non-realtime analysis.
         */
        @Builder.Default
        private int batchIntervalSeconds = 300;
    }
}
