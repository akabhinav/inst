package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

/**
 * Message versioning and schema evolution
 * Feature 61: Message Versioning & Schema Evolution
 */
@Data
@Builder
public class VersionHandling {

    /**
     * Enable version handling
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Compatibility mode
     */
    @Builder.Default
    private Compatibility compatibility = Compatibility.BACKWARD;

    /**
     * Message transformer for version conversion
     */
    private MessageTransformer transformer;

    /**
     * Compatibility modes
     */
    public enum Compatibility {
        BACKWARD,       // New code reads old messages
        FORWARD,        // Old code reads new messages
        FULL,           // Both directions
        BREAKING        // Separate topics for incompatible changes
    }

    /**
     * Message transformer interface
     */
    public interface MessageTransformer {
        <FROM, TO> TO transform(FROM from, Class<TO> toType);
    }

    public static VersionHandling createDefault() {
        return VersionHandling.builder().build();
    }
}
