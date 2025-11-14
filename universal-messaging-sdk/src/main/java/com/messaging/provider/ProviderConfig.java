package com.messaging.provider;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for a message provider
 */
@Data
@Builder
public class ProviderConfig {

    /**
     * Provider type
     */
    private ProviderType providerType;

    /**
     * Connection string (broker URLs)
     */
    private String connectionString;

    /**
     * Authentication username
     */
    private String username;

    /**
     * Authentication password
     */
    private String password;

    /**
     * Additional provider-specific properties
     */
    private Map<String, Object> properties;

    /**
     * Connection timeout in milliseconds
     */
    @Builder.Default
    private long connectionTimeoutMs = 30000;

    /**
     * Request timeout in milliseconds
     */
    @Builder.Default
    private long requestTimeoutMs = 10000;

    /**
     * Enable SSL/TLS
     */
    @Builder.Default
    private boolean enableSsl = false;

    /**
     * SSL trust store path
     */
    private String trustStorePath;

    /**
     * SSL trust store password
     */
    private String trustStorePassword;

    /**
     * Maximum pool size for connection pooling
     * Feature 4: Connection Pooling
     */
    @Builder.Default
    private int maxPoolSize = 10;

    /**
     * Minimum pool size
     */
    @Builder.Default
    private int minPoolSize = 2;
}
