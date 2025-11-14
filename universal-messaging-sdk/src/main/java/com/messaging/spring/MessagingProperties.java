package com.messaging.spring;

import com.messaging.reliability.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Spring Boot configuration properties for messaging
 */
@Data
@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {

    /**
     * Message broker provider (kafka, rabbitmq, sqs, etc.)
     */
    private String provider = "memory";

    /**
     * Connection string for the broker
     */
    private String connectionString = "localhost";

    /**
     * Retry configuration
     */
    private RetryProperties retry = new RetryProperties();

    /**
     * Dead letter queue configuration
     */
    private DlqProperties dlq = new DlqProperties();

    /**
     * Compression configuration
     */
    private CompressionProperties compression = new CompressionProperties();

    /**
     * Encryption configuration
     */
    private EncryptionProperties encryption = new EncryptionProperties();

    @Data
    public static class RetryProperties {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private long initialBackoffMs = 100;
        private long maxBackoffMs = 30000;
        private double backoffMultiplier = 2.0;

        public RetryPolicy toRetryPolicy() {
            return RetryPolicy.builder()
                    .maxAttempts(maxAttempts)
                    .initialBackoff(Duration.ofMillis(initialBackoffMs))
                    .maxBackoff(Duration.ofMillis(maxBackoffMs))
                    .backoffMultiplier(backoffMultiplier)
                    .build();
        }
    }

    @Data
    public static class DlqProperties {
        private boolean enabled = true;
        private String topicPattern = "%s-dlq";
        private int maxRetries = 3;

        public DeadLetterQueue toDeadLetterQueue() {
            return DeadLetterQueue.builder()
                    .enabled(enabled)
                    .topicPattern(topicPattern)
                    .maxRetries(maxRetries)
                    .build();
        }
    }

    @Data
    public static class CompressionProperties {
        private boolean enabled = false;
        private String algorithm = "GZIP";
        private int level = 6;
        private int minSizeBytes = 1024;

        public Compression toCompression() {
            return Compression.builder()
                    .enabled(enabled)
                    .algorithm(Compression.Algorithm.valueOf(algorithm))
                    .level(level)
                    .minSizeBytes(minSizeBytes)
                    .build();
        }
    }

    @Data
    public static class EncryptionProperties {
        private boolean enabled = false;
        private String algorithm = "AES_256_GCM";
        private String key;

        public Encryption toEncryption() {
            return Encryption.builder()
                    .enabled(enabled)
                    .algorithm(Encryption.Algorithm.valueOf(algorithm))
                    .key(key)
                    .build();
        }
    }
}
