package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

/**
 * Message compression configuration
 * Feature 26: Message Compression
 */
@Data
@Builder
public class Compression {

    /**
     * Enable compression
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Compression algorithm
     */
    @Builder.Default
    private Algorithm algorithm = Algorithm.GZIP;

    /**
     * Compression level (1-9, higher = better compression but slower)
     */
    @Builder.Default
    private int level = 6;

    /**
     * Only compress messages larger than this (bytes)
     */
    @Builder.Default
    private int minSizeBytes = 1024;

    /**
     * Compression algorithms
     */
    public enum Algorithm {
        GZIP,       // Good compression ratio
        SNAPPY,     // Fast compression
        LZ4,        // Very fast compression
        ZSTD        // Best compression ratio
    }

    public static Compression createDefault() {
        return Compression.builder().build();
    }
}
