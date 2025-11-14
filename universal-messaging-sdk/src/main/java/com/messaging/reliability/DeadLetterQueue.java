package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

/**
 * Dead Letter Queue configuration
 * Feature 22: Dead Letter Queue
 *
 * Messages that fail processing after max retries are sent to DLQ
 */
@Data
@Builder
public class DeadLetterQueue {

    /**
     * Enable DLQ
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * DLQ topic/queue name pattern
     * %s will be replaced with original topic name
     */
    @Builder.Default
    private String topicPattern = "%s-dlq";

    /**
     * Maximum retries before sending to DLQ
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * Store original error in message headers
     */
    @Builder.Default
    private boolean storeError = true;

    /**
     * Store stack trace
     */
    @Builder.Default
    private boolean storeStackTrace = true;

    /**
     * Callback when message sent to DLQ
     */
    private Consumer<DlqMessage> onDlq;

    /**
     * Enable DLQ replay (ability to replay messages from DLQ)
     */
    @Builder.Default
    private boolean enableReplay = true;

    /**
     * DLQ Message wrapper
     */
    @Data
    @Builder
    public static class DlqMessage {
        private String originalTopic;
        private String messageId;
        private Object payload;
        private Throwable error;
        private int failureCount;
    }

    /**
     * Create default DLQ config
     */
    public static DeadLetterQueue createDefault() {
        return DeadLetterQueue.builder().build();
    }
}
