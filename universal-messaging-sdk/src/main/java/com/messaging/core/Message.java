package com.messaging.core;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Universal Message abstraction that works across all message brokers.
 * Represents a message with payload, headers, and metadata.
 *
 * @param <T> The type of the message payload
 */
@Data
@Builder
public class Message<T> {

    /**
     * Unique message identifier
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * The actual message payload
     */
    private T payload;

    /**
     * Message headers for metadata
     */
    @Singular
    private Map<String, String> headers;

    /**
     * Topic/queue name
     */
    private String topic;

    /**
     * Message timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Message priority (HIGH, MEDIUM, LOW)
     */
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Partition key for ordered processing
     */
    private String partitionKey;

    /**
     * Message version for schema evolution
     */
    @Builder.Default
    private int version = 1;

    /**
     * Correlation ID for request-reply pattern
     */
    private String correlationId;

    /**
     * Reply-to topic for request-reply pattern
     */
    private String replyTo;

    /**
     * Time-to-live in milliseconds
     */
    private Long ttl;

    /**
     * Number of times this message has been delivered
     */
    @Builder.Default
    private int deliveryCount = 0;

    /**
     * Original exception if message failed
     */
    private transient Throwable error;

    /**
     * Message priority levels
     */
    public enum Priority {
        LOW(0),
        MEDIUM(5),
        HIGH(10),
        CRITICAL(15);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
