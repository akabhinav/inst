package com.messaging.provider;

import com.messaging.core.Message;
import com.messaging.core.HealthStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provider abstraction for different message brokers
 * Feature 1: Multi-Broker Support
 *
 * Each broker (Kafka, RabbitMQ, SQS, etc.) implements this interface
 */
public interface MessageProvider {

    /**
     * Initialize the provider with configuration
     */
    void initialize(ProviderConfig config);

    /**
     * Send a message
     */
    <T> Mono<ProviderSendResult> send(Message<T> message);

    /**
     * Receive messages as a stream
     */
    <T> Flux<Message<T>> receive(String topic, Class<T> payloadType);

    /**
     * Acknowledge a message
     */
    void acknowledge(String messageId);

    /**
     * Negative acknowledge (requeue)
     */
    void nack(String messageId);

    /**
     * Get health status
     * Feature 8: Broker Health Monitoring
     */
    HealthStatus getHealth();

    /**
     * Get provider-specific metrics
     */
    ProviderMetrics getMetrics();

    /**
     * Close and cleanup resources
     */
    void close();

    /**
     * Get provider type
     */
    ProviderType getType();

    /**
     * Get provider name
     */
    String getName();
}
