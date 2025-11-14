package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Distributed tracing configuration for enabling and configuring trace collection
 * across the messaging system.
 *
 * This class supports multiple tracing providers (OpenTelemetry, Zipkin, Jaeger)
 * and allows configuration of sampling rates for performance optimization.
 *
 * Feature 31: Distributed Tracing
 *
 * @author Messaging SDK
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TracingConfig {

    /**
     * Flag to enable or disable distributed tracing.
     */
    private boolean enabled;

    /**
     * Tracing provider to use for collecting and exporting traces.
     * Supported providers: OPENTELEMETRY, ZIPKIN, JAEGER
     */
    @Builder.Default
    private TracingProvider provider = TracingProvider.OPENTELEMETRY;

    /**
     * Service name for identification in distributed traces.
     * Used to identify this service in trace visualizations.
     */
    private String serviceName;

    /**
     * Sampling rate (0.0 to 1.0) for trace sampling.
     * 0.0 = no sampling, 1.0 = sample all traces.
     * Default is 0.1 (10% sampling) for production environments.
     */
    @Builder.Default
    private double samplingRate = 0.1;

    /**
     * Enum for supported distributed tracing providers.
     */
    public enum TracingProvider {
        /**
         * OpenTelemetry distributed tracing provider.
         */
        OPENTELEMETRY,

        /**
         * Zipkin distributed tracing provider.
         */
        ZIPKIN,

        /**
         * Jaeger distributed tracing provider.
         */
        JAEGER
    }
}
