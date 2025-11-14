package com.messaging.reliability.impl;

import com.messaging.core.Message;
import com.messaging.reliability.Deduplication;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Message deduplication handler
 * Feature 23: Message Deduplication
 */
@Slf4j
public class DeduplicationHandler {

    private final Deduplication config;
    private final Map<String, Instant> messageCache;
    private final ScheduledExecutorService cleanupExecutor;

    public DeduplicationHandler(Deduplication config) {
        this.config = config;
        this.messageCache = new ConcurrentHashMap<>();

        // Start cleanup task
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredEntries,
            config.getWindow().toMillis(),
            config.getWindow().toMillis(),
            TimeUnit.MILLISECONDS
        );

        log.info("Deduplication handler initialized with window: {}", config.getWindow());
    }

    /**
     * Check if message is duplicate
     * Returns true if duplicate (should be ignored), false if new message
     */
    public boolean isDuplicate(Message<?> message) {
        if (!config.isEnabled()) {
            return false;
        }

        String dedupKey = generateDeduplicationKey(message);

        if (messageCache.containsKey(dedupKey)) {
            log.warn("Duplicate message detected: {}", dedupKey);
            return true;
        }

        // Add to cache
        messageCache.put(dedupKey, Instant.now());

        // Enforce max cache size
        if (messageCache.size() > config.getMaxCacheSize()) {
            log.warn("Deduplication cache size ({}) exceeded max ({}), removing oldest entries",
                messageCache.size(), config.getMaxCacheSize());
            removeOldestEntries();
        }

        return false;
    }

    /**
     * Generate deduplication key based on strategy
     */
    private String generateDeduplicationKey(Message<?> message) {
        return switch (config.getStrategy()) {
            case MESSAGE_ID -> {
                // Use message ID
                yield message.getId();
            }
            case CONTENT_HASH -> {
                // Hash message content
                yield hashContent(message);
            }
            case CUSTOM -> {
                // Use custom key extractor
                if (config.getKeyExtractor() != null) {
                    yield config.getKeyExtractor().apply(message.getPayload());
                }
                log.warn("Custom key extractor not configured, falling back to message ID");
                yield message.getId();
            }
        };
    }

    /**
     * Hash message content
     */
    private String hashContent(Message<?> message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Include topic
            digest.update(message.getTopic().getBytes());

            // Include payload
            if (message.getPayload() != null) {
                String payloadStr = message.getPayload().toString();
                digest.update(payloadStr.getBytes());
            }

            // Include partition key if present
            if (message.getPartitionKey() != null) {
                digest.update(message.getPartitionKey().getBytes());
            }

            byte[] hash = digest.digest();
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            log.error("Failed to hash message content, using message ID", e);
            return message.getId();
        }
    }

    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        try {
            Instant cutoff = Instant.now().minus(config.getWindow());
            int removed = 0;

            messageCache.entrySet().removeIf(entry -> {
                if (entry.getValue().isBefore(cutoff)) {
                    removed++;
                    return true;
                }
                return false;
            });

            if (removed > 0) {
                log.debug("Removed {} expired entries from deduplication cache", removed);
            }

        } catch (Exception e) {
            log.error("Error during deduplication cache cleanup", e);
        }
    }

    /**
     * Remove oldest entries to enforce max cache size
     */
    private void removeOldestEntries() {
        int toRemove = messageCache.size() - (config.getMaxCacheSize() * 3 / 4); // Remove 25%

        messageCache.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(toRemove)
            .forEach(entry -> messageCache.remove(entry.getKey()));

        log.debug("Removed {} oldest entries from deduplication cache", toRemove);
    }

    /**
     * Get cache size
     */
    public int getCacheSize() {
        return messageCache.size();
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        messageCache.clear();
        log.info("Deduplication cache cleared");
    }

    /**
     * Shutdown handler
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        messageCache.clear();
        log.info("Deduplication handler shut down");
    }
}
