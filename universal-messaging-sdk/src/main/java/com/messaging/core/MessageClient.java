package com.messaging.core;

import com.messaging.observability.Analytics;
import com.messaging.observability.DisasterRecovery;
import com.messaging.observability.FinancialTracking;
import com.messaging.observability.ProcessIntelligence;
import com.messaging.performance.*;
import com.messaging.provider.ProviderType;
import com.messaging.reliability.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Main entry point for the Universal Messaging SDK.
 * Provides a fluent API for sending and receiving messages across different message brokers.
 *
 * Example usage:
 * <pre>
 * MessageClient client = MessageClient.builder()
 *     .provider("kafka")
 *     .connectionString("localhost:9092")
 *     .build();
 *
 * // Send a message
 * client.send("orders", order).execute();
 *
 * // Receive messages
 * client.receive("orders", Order.class)
 *     .onMessage(order -> processOrder(order))
 *     .start();
 * </pre>
 */
public interface MessageClient {

    /**
     * Create a new MessageClient builder
     */
    static MessageClientBuilder builder() {
        return new MessageClientBuilderImpl();
    }

    /**
     * Send a message to a topic/queue
     *
     * @param topic The topic/queue name
     * @param payload The message payload
     * @param <T> The type of the payload
     * @return SendOperation for fluent configuration
     */
    <T> SendOperation<T> send(String topic, T payload);

    /**
     * Send a message asynchronously (reactive)
     *
     * @param topic The topic/queue name
     * @param payload The message payload
     * @param <T> The type of the payload
     * @return Mono of SendResult
     */
    <T> Mono<SendResult> sendAsync(String topic, T payload);

    /**
     * Receive messages from a topic/queue
     *
     * @param topic The topic/queue name
     * @param payloadType The class of the payload type
     * @param <T> The type of the payload
     * @return ReceiveOperation for fluent configuration
     */
    <T> ReceiveOperation<T> receive(String topic, Class<T> payloadType);

    /**
     * Receive messages reactively (returns Flux)
     *
     * @param topic The topic/queue name
     * @param payloadType The class of the payload type
     * @param <T> The type of the payload
     * @return Flux of messages
     */
    <T> Flux<Message<T>> receiveReactive(String topic, Class<T> payloadType);

    /**
     * Request-reply pattern (synchronous)
     *
     * @param topic The topic to send request to
     * @param request The request payload
     * @param responseType The expected response type
     * @param <REQ> The request type
     * @param <RES> The response type
     * @return The response
     */
    <REQ, RES> RES request(String topic, REQ request, Class<RES> responseType);

    /**
     * Request-reply pattern (asynchronous)
     *
     * @param topic The topic to send request to
     * @param request The request payload
     * @param responseType The expected response type
     * @param timeout Timeout duration
     * @param <REQ> The request type
     * @param <RES> The response type
     * @return Mono of response
     */
    <REQ, RES> Mono<RES> requestAsync(String topic, REQ request, Class<RES> responseType, Duration timeout);

    /**
     * Execute a saga (distributed transaction)
     *
     * @param saga The saga to execute
     * @return SagaResult
     */
    SagaResult executeSaga(Saga saga);

    /**
     * Get health status of the messaging client
     *
     * @return HealthStatus
     */
    HealthStatus getHealth();

    /**
     * Get metrics for the messaging client
     *
     * @return MessageMetrics
     */
    MessageMetrics getMetrics();

    /**
     * Get financial tracking information
     *
     * @return FinancialTracking instance
     */
    FinancialTracking financials();

    /**
     * Get process mining report
     *
     * @return ProcessMiningReport
     */
    ProcessMiningReport getProcessMiningReport();

    /**
     * Restore from backup
     *
     * @return RestoreOperation
     */
    RestoreOperation restore();

    /**
     * Get chaos engineering report
     *
     * @return ChaosReport
     */
    ChaosReport getChaosReport();

    /**
     * Get degradation status
     *
     * @return DegradationStatus
     */
    DegradationStatus getDegradationStatus();

    /**
     * Get adaptive batching metrics
     *
     * @return AdaptiveBatchingMetrics
     */
    AdaptiveBatchingMetrics getBatchingMetrics();

    /**
     * Get financial report
     *
     * @return FinancialReport
     */
    FinancialReport getFinancialReport();

    /**
     * Close the client and release resources
     */
    void close();

    /**
     * Builder interface for MessageClient
     */
    interface MessageClientBuilder {

        /**
         * Set the message broker provider
         * Supported: kafka, rabbitmq, sqs, sns, pubsub, servicebus, activemq, pulsar, nats, redis, etc.
         */
        MessageClientBuilder provider(String provider);

        /**
         * Set the provider type enum
         */
        MessageClientBuilder provider(ProviderType providerType);

        /**
         * Set connection string for the broker
         */
        MessageClientBuilder connectionString(String connectionString);

        /**
         * Enable connection pooling
         */
        MessageClientBuilder connectionPooling(ConnectionPooling pooling);

        /**
         * Configure retry policy
         */
        MessageClientBuilder retry(RetryPolicy retry);

        /**
         * Configure dead letter queue
         */
        MessageClientBuilder deadLetterQueue(DeadLetterQueue dlq);

        /**
         * Configure message deduplication
         */
        MessageClientBuilder deduplication(Deduplication dedup);

        /**
         * Configure message compression
         */
        MessageClientBuilder compression(Compression compression);

        /**
         * Configure message encryption
         */
        MessageClientBuilder encryption(Encryption encryption);

        /**
         * Configure circuit breaker
         */
        MessageClientBuilder circuitBreaker(CircuitBreaker circuitBreaker);

        /**
         * Configure rate limiting
         */
        MessageClientBuilder rateLimit(RateLimit rateLimit);

        /**
         * Configure distributed tracing
         */
        MessageClientBuilder tracing(TracingConfig tracing);

        /**
         * Configure zero-copy transfer
         */
        MessageClientBuilder zeroCopy(ZeroCopy zeroCopy);

        /**
         * Configure chaos engineering
         */
        MessageClientBuilder chaosEngineering(ChaosEngineering chaos);

        /**
         * Configure graceful degradation
         */
        MessageClientBuilder degradation(Degradation degradation);

        /**
         * Configure self-healing
         */
        MessageClientBuilder selfHealing(SelfHealing selfHealing);

        /**
         * Configure disaster recovery
         */
        MessageClientBuilder disasterRecovery(DisasterRecovery dr);

        /**
         * Configure service mesh integration
         */
        MessageClientBuilder serviceMesh(ServiceMesh serviceMesh);

        /**
         * Configure API gateway
         */
        MessageClientBuilder apiGateway(ApiGateway apiGateway);

        /**
         * Configure analytics
         */
        MessageClientBuilder analytics(Analytics analytics);

        /**
         * Configure machine learning
         */
        MessageClientBuilder machineLearning(MachineLearning ml);

        /**
         * Configure process intelligence
         */
        MessageClientBuilder processIntelligence(ProcessIntelligence pi);

        /**
         * Configure financial tracking
         */
        MessageClientBuilder financials(FinancialTracking financials);

        /**
         * Build the MessageClient instance
         */
        MessageClient build();
    }
}
