package com.messaging.reliability.impl;

import com.messaging.core.Message;
import com.messaging.provider.MessageProvider;
import com.messaging.reliability.DeadLetterQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Dead Letter Queue handler
 * Feature 22: Dead Letter Queue
 */
@Slf4j
public class DeadLetterQueueHandler {

    private final DeadLetterQueue config;
    private final MessageProvider provider;
    private final Map<String, Integer> failureCountMap = new HashMap<>();

    public DeadLetterQueueHandler(DeadLetterQueue config, MessageProvider provider) {
        this.config = config;
        this.provider = provider;
    }

    /**
     * Handle failed message
     * Returns true if message should be sent to DLQ, false to retry
     */
    public boolean handleFailedMessage(Message<?> message, Throwable error) {
        if (!config.isEnabled()) {
            return false;
        }

        String messageId = message.getId();
        int failureCount = failureCountMap.getOrDefault(messageId, 0) + 1;
        failureCountMap.put(messageId, failureCount);

        log.warn("Message {} failed {} times", messageId, failureCount);

        // Check if should send to DLQ
        if (failureCount >= config.getMaxRetries()) {
            log.error("Message {} exceeded max retries ({}), sending to DLQ",
                messageId, config.getMaxRetries());

            sendToDLQ(message, error, failureCount);
            failureCountMap.remove(messageId); // Clean up
            return true;
        }

        return false;
    }

    /**
     * Send message to DLQ
     */
    private void sendToDLQ(Message<?> message, Throwable error, int failureCount) {
        try {
            // Calculate DLQ topic name
            String dlqTopic = String.format(config.getTopicPattern(), message.getTopic());

            // Create DLQ message with error metadata
            Map<String, String> dlqHeaders = new HashMap<>(message.getHeaders());
            dlqHeaders.put("dlq-original-topic", message.getTopic());
            dlqHeaders.put("dlq-failure-count", String.valueOf(failureCount));

            if (config.isStoreError()) {
                dlqHeaders.put("dlq-error-message", error.getMessage());
                dlqHeaders.put("dlq-error-type", error.getClass().getName());
            }

            if (config.isStoreStackTrace()) {
                dlqHeaders.put("dlq-stack-trace", getStackTrace(error));
            }

            Message<Object> dlqMessage = Message.builder()
                .id(message.getId())
                .topic(dlqTopic)
                .payload(message.getPayload())
                .headers(dlqHeaders)
                .priority(message.getPriority())
                .build();

            // Send to DLQ
            provider.send(dlqMessage).block();

            log.info("Message {} sent to DLQ topic: {}", message.getId(), dlqTopic);

            // Trigger callback
            if (config.getOnDlq() != null) {
                DeadLetterQueue.DlqMessage dlqMsg = DeadLetterQueue.DlqMessage.builder()
                    .originalTopic(message.getTopic())
                    .messageId(message.getId())
                    .payload(message.getPayload())
                    .error(error)
                    .failureCount(failureCount)
                    .build();

                config.getOnDlq().accept(dlqMsg);
            }

        } catch (Exception e) {
            log.error("Failed to send message to DLQ", e);
        }
    }

    /**
     * Reset failure count for a message
     */
    public void resetFailureCount(String messageId) {
        failureCountMap.remove(messageId);
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable error) {
        StringBuilder sb = new StringBuilder();
        sb.append(error.toString()).append("\n");

        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        if (error.getCause() != null) {
            sb.append("Caused by: ").append(getStackTrace(error.getCause()));
        }

        return sb.toString();
    }
}
