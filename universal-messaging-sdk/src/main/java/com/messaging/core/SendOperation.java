package com.messaging.core;

import com.messaging.core.Message.Priority;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

/**
 * Fluent API for sending messages with various configurations
 *
 * @param <T> The type of the message payload
 */
public interface SendOperation<T> {

    /**
     * Set message priority
     */
    SendOperation<T> priority(Priority priority);

    /**
     * Compute priority dynamically from payload
     */
    SendOperation<T> priority(Function<T, Priority> priorityFunction);

    /**
     * Set partition key for ordered processing
     */
    SendOperation<T> partitionKey(String key);

    /**
     * Add a header
     */
    SendOperation<T> header(String key, String value);

    /**
     * Add multiple headers
     */
    SendOperation<T> headers(Map<String, String> headers);

    /**
     * Set correlation ID for request-reply
     */
    SendOperation<T> correlationId(String correlationId);

    /**
     * Set reply-to topic for request-reply
     */
    SendOperation<T> replyTo(String topic);

    /**
     * Set time-to-live
     */
    SendOperation<T> ttl(Duration ttl);

    /**
     * Set message version for schema evolution
     */
    SendOperation<T> version(int version);

    /**
     * Enable message compression
     */
    SendOperation<T> compress(boolean compress);

    /**
     * Enable message encryption
     */
    SendOperation<T> encrypt(boolean encrypt);

    /**
     * Set delivery guarantee
     * - AT_MOST_ONCE: Fire and forget (fastest)
     * - AT_LEAST_ONCE: Retry until success (default)
     * - EXACTLY_ONCE: Exactly once delivery (slowest, most reliable)
     */
    SendOperation<T> deliveryGuarantee(DeliveryGuarantee guarantee);

    /**
     * Execute the send operation synchronously
     *
     * @return SendResult
     */
    SendResult execute();

    /**
     * Execute the send operation asynchronously
     *
     * @return Mono of SendResult
     */
    Mono<SendResult> executeAsync();

    /**
     * Delivery guarantee options
     */
    enum DeliveryGuarantee {
        AT_MOST_ONCE,   // Fast, may lose messages
        AT_LEAST_ONCE,  // Default, may duplicate
        EXACTLY_ONCE    // Slow, guaranteed once
    }
}
