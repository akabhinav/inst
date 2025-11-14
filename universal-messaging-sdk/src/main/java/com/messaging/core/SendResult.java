package com.messaging.core;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Result of a send operation
 */
@Data
@Builder
public class SendResult {

    /**
     * The message ID
     */
    private String messageId;

    /**
     * The topic the message was sent to
     */
    private String topic;

    /**
     * Partition number (for Kafka and similar)
     */
    private Integer partition;

    /**
     * Offset (for Kafka and similar)
     */
    private Long offset;

    /**
     * Timestamp when message was sent
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Whether the send was successful
     */
    private boolean success;

    /**
     * Error message if send failed
     */
    private String error;

    /**
     * Latency in milliseconds
     */
    private long latencyMs;

    /**
     * Size of the message in bytes
     */
    private long sizeBytes;
}
