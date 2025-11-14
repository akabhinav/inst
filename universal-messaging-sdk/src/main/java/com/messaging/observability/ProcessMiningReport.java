package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Process mining report containing analysis of message processing workflows.
 *
 * Provides comprehensive insights into process variants, bottlenecks,
 * compliance violations, and optimization opportunities.
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
public class ProcessMiningReport {

    /**
     * Report generation timestamp.
     */
    private Instant generatedAt;

    /**
     * Analysis period start time.
     */
    private Instant periodStart;

    /**
     * Analysis period end time.
     */
    private Instant periodEnd;

    /**
     * List of discovered process variants with their frequencies.
     */
    private List<ProcessVariant> variants;

    /**
     * List of identified bottlenecks in the process flow.
     */
    private List<Bottleneck> bottlenecks;

    /**
     * Compliance analysis results.
     */
    private ComplianceAnalysis compliance;

    /**
     * List of optimization recommendations.
     */
    private List<Optimization> optimizations;

    /**
     * Summary statistics for the analysis period.
     */
    private ProcessStatistics statistics;

    /**
     * Process variant representing a specific message handling workflow.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessVariant {

        /**
         * Unique variant identifier.
         */
        private String id;

        /**
         * Sequence of activities in this variant.
         */
        private List<String> activities;

        /**
         * Frequency of this variant (count of occurrences).
         */
        private long frequency;

        /**
         * Percentage of all processes that follow this variant.
         */
        private double percentage;

        /**
         * Average duration of this variant in milliseconds.
         */
        private long averageDurationMs;

        /**
         * Minimum observed duration in milliseconds.
         */
        private long minDurationMs;

        /**
         * Maximum observed duration in milliseconds.
         */
        private long maxDurationMs;

        /**
         * Compliance status of this variant.
         */
        private ComplianceStatus complianceStatus;
    }

    /**
     * Bottleneck detection result.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bottleneck {

        /**
         * Name or identifier of the bottleneck.
         */
        private String name;

        /**
         * Activity or stage causing the bottleneck.
         */
        private String activity;

        /**
         * Severity level of the bottleneck.
         */
        private SeverityLevel severity;

        /**
         * Average wait time in milliseconds.
         */
        private long averageWaitTimeMs;

        /**
         * Number of processes affected.
         */
        private long affectedProcessCount;

        /**
         * Percentage of total processes affected.
         */
        private double affectedPercentage;

        /**
         * Root cause analysis or description.
         */
        private String rootCause;

        /**
         * Recommended remediation action.
         */
        private String recommendation;

        /**
         * Severity level enumeration.
         */
        public enum SeverityLevel {
            /**
             * Low severity bottleneck.
             */
            LOW,

            /**
             * Medium severity bottleneck.
             */
            MEDIUM,

            /**
             * High severity bottleneck.
             */
            HIGH,

            /**
             * Critical severity bottleneck.
             */
            CRITICAL
        }
    }

    /**
     * Compliance analysis results.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceAnalysis {

        /**
         * Overall compliance score (0-100).
         */
        private double complianceScore;

        /**
         * Total number of compliance violations detected.
         */
        private long violationCount;

        /**
         * Map of violation types to counts.
         */
        private Map<String, Long> violationsByType;

        /**
         * List of non-compliant process variants.
         */
        private List<String> nonCompliantVariants;

        /**
         * Compliance assessment status.
         */
        private ComplianceStatus status;
    }

    /**
     * Optimization recommendation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Optimization {

        /**
         * Unique optimization identifier.
         */
        private String id;

        /**
         * Title of the optimization recommendation.
         */
        private String title;

        /**
         * Detailed description of the optimization.
         */
        private String description;

        /**
         * Expected impact category.
         */
        private ImpactCategory category;

        /**
         * Estimated improvement percentage (0-100).
         */
        private double expectedImprovement;

        /**
         * Priority level of this optimization.
         */
        private PriorityLevel priority;

        /**
         * Effort required to implement (LOW, MEDIUM, HIGH).
         */
        private EffortLevel effort;

        /**
         * Affected processes or activities.
         */
        private List<String> affectedActivities;

        /**
         * Impact category enumeration.
         */
        public enum ImpactCategory {
            /**
             * Performance improvement.
             */
            PERFORMANCE,

            /**
             * Cost reduction.
             */
            COST,

            /**
             * Quality improvement.
             */
            QUALITY,

            /**
             * Compliance improvement.
             */
            COMPLIANCE
        }

        /**
         * Priority level enumeration.
         */
        public enum PriorityLevel {
            /**
             * Low priority optimization.
             */
            LOW,

            /**
             * Medium priority optimization.
             */
            MEDIUM,

            /**
             * High priority optimization.
             */
            HIGH
        }

        /**
         * Effort level enumeration.
         */
        public enum EffortLevel {
            /**
             * Low effort required.
             */
            LOW,

            /**
             * Medium effort required.
             */
            MEDIUM,

            /**
             * High effort required.
             */
            HIGH
        }
    }

    /**
     * Process statistics summary.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessStatistics {

        /**
         * Total number of processes analyzed.
         */
        private long totalProcesses;

        /**
         * Number of unique process variants discovered.
         */
        private int uniqueVariants;

        /**
         * Average process duration in milliseconds.
         */
        private long averageProcessDurationMs;

        /**
         * Median process duration in milliseconds.
         */
        private long medianProcessDurationMs;

        /**
         * 95th percentile duration in milliseconds.
         */
        private long p95DurationMs;

        /**
         * 99th percentile duration in milliseconds.
         */
        private long p99DurationMs;

        /**
         * Total number of activities across all processes.
         */
        private long totalActivities;

        /**
         * Average number of activities per process.
         */
        private double averageActivitiesPerProcess;
    }

    /**
     * Enum for compliance status.
     */
    public enum ComplianceStatus {
        /**
         * Process is fully compliant.
         */
        COMPLIANT,

        /**
         * Process is partially compliant.
         */
        PARTIALLY_COMPLIANT,

        /**
         * Process is non-compliant.
         */
        NON_COMPLIANT,

        /**
         * Compliance status is unknown.
         */
        UNKNOWN
    }
}
