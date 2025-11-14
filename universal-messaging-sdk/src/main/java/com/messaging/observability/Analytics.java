package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Analytics configuration for collecting and exporting messaging metrics.
 *
 * Enables collection of performance, usage, and operational metrics
 * with flexible destination options for analysis and monitoring.
 *
 * Feature 67: Analytics and Metrics
 *
 * @author Messaging SDK
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analytics {

    /**
     * Flag to enable or disable analytics collection.
     */
    private boolean enabled;

    /**
     * List of configured analytics destinations.
     */
    private List<AnalyticsDestination> destinations;

    /**
     * Analytics destination configuration.
     * Specifies where and how to send collected analytics data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsDestination {

        /**
         * Unique identifier for this destination.
         */
        private String id;

        /**
         * Name of the analytics destination.
         */
        private String name;

        /**
         * Type of the analytics destination.
         */
        private DestinationType type;

        /**
         * Endpoint URL or connection string.
         */
        private String endpoint;

        /**
         * Authentication credentials or API key.
         */
        private String credentials;

        /**
         * Batch size for sending metrics.
         */
        @Builder.Default
        private int batchSize = 100;

        /**
         * Batch timeout in seconds.
         */
        @Builder.Default
        private int batchTimeoutSeconds = 30;

        /**
         * Flag to enable data compression.
         */
        @Builder.Default
        private boolean compressionEnabled = true;

        /**
         * Flag to enable asynchronous sending.
         */
        @Builder.Default
        private boolean async = true;

        /**
         * Maximum number of retry attempts.
         */
        @Builder.Default
        private int maxRetries = 3;

        /**
         * Retry backoff multiplier.
         */
        @Builder.Default
        private double backoffMultiplier = 2.0;

        /**
         * Destination type enumeration.
         */
        public enum DestinationType {
            /**
             * Prometheus metrics endpoint.
             */
            PROMETHEUS,

            /**
             * Elasticsearch cluster.
             */
            ELASTICSEARCH,

            /**
             * InfluxDB time series database.
             */
            INFLUXDB,

            /**
             * Datadog analytics service.
             */
            DATADOG,

            /**
             * New Relic monitoring service.
             */
            NEW_RELIC,

            /**
             * CloudWatch metrics service.
             */
            CLOUDWATCH,

            /**
             * Generic HTTP endpoint.
             */
            HTTP,

            /**
             * Kafka topic.
             */
            KAFKA
        }
    }
}
