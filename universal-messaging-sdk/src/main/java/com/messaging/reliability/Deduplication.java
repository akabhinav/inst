package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.function.Function;

/**
 * Message deduplication configuration
 * Feature 23: Message Deduplication
 *
 * Prevents processing duplicate messages
 */
@Data
@Builder
public class Deduplication {

    /**
     * Enable deduplication
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Deduplication strategy
     */
    @Builder.Default
    private Strategy strategy = Strategy.MESSAGE_ID;

    /**
     * Custom key extractor for CUSTOM strategy
     */
    private Function<Object, String> keyExtractor;

    /**
     * Time window for deduplication
     */
    @Builder.Default
    private Duration window = Duration.ofMinutes(5);

    /**
     * Storage backend for dedup cache
     */
    @Builder.Default
    private StorageBackend storageBackend = StorageBackend.IN_MEMORY;

    /**
     * Redis connection string (if using Redis backend)
     */
    private String redisConnectionString;

    /**
     * Maximum cache size for in-memory backend
     */
    @Builder.Default
    private int maxCacheSize = 100000;

    /**
     * Deduplication strategies
     */
    public enum Strategy {
        MESSAGE_ID,      // Use message ID (default)
        CONTENT_HASH,    // Hash of message content
        CUSTOM           // Custom key extractor
    }

    /**
     * Storage backends
     */
    public enum StorageBackend {
        IN_MEMORY,       // Fast, but lost on restart
        REDIS,           // Distributed, persistent
        DATABASE         // Most persistent
    }

    /**
     * Create default deduplication config
     */
    public static Deduplication createDefault() {
        return Deduplication.builder().build();
    }
}
