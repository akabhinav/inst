package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Connection pooling configuration
 * Feature 4: Connection Pooling
 */
@Data
@Builder
public class ConnectionPooling {

    /**
     * Enable connection pooling
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Minimum pool size
     */
    @Builder.Default
    private int minSize = 2;

    /**
     * Maximum pool size
     */
    @Builder.Default
    private int maxSize = 10;

    /**
     * Max idle time before connection is closed
     */
    @Builder.Default
    private Duration maxIdleTime = Duration.ofMinutes(10);

    /**
     * Connection timeout
     */
    @Builder.Default
    private Duration connectionTimeout = Duration.ofSeconds(30);

    /**
     * Validate connections periodically
     */
    @Builder.Default
    private boolean validateConnections = true;

    /**
     * Validation interval
     */
    @Builder.Default
    private Duration validationInterval = Duration.ofSeconds(30);

    public static ConnectionPooling createDefault() {
        return ConnectionPooling.builder().build();
    }
}
