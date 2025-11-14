package com.messaging.performance;

import lombok.Builder;
import lombok.Data;

/**
 * Zero-copy message transfer configuration
 * Feature 51: Zero-Copy Message Transfer
 *
 * Ultra-fast message transfer with zero memory copying
 */
@Data
@Builder
public class ZeroCopy {

    /**
     * Enable zero-copy transfer
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Use direct ByteBuffers (off-heap memory)
     */
    @Builder.Default
    private boolean directBuffers = true;

    /**
     * Use memory-mapped files
     */
    @Builder.Default
    private boolean memoryMapping = true;

    /**
     * Buffer size in bytes
     */
    @Builder.Default
    private int bufferSize = 65536; // 64KB

    public static ZeroCopy createDefault() {
        return ZeroCopy.builder().build();
    }

    public static ZeroCopy enabled() {
        return ZeroCopy.builder()
                .enabled(true)
                .directBuffers(true)
                .memoryMapping(true)
                .build();
    }
}
