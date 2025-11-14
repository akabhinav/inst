package com.messaging.reliability;

import lombok.Builder;
import lombok.Data;

/**
 * Message encryption configuration
 * Feature 27: Message Encryption
 */
@Data
@Builder
public class Encryption {

    /**
     * Enable encryption
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Encryption algorithm
     */
    @Builder.Default
    private Algorithm algorithm = Algorithm.AES_256_GCM;

    /**
     * Encryption key (base64 encoded)
     */
    private String key;

    /**
     * Key rotation enabled
     */
    @Builder.Default
    private boolean keyRotation = false;

    /**
     * Encrypt headers as well
     */
    @Builder.Default
    private boolean encryptHeaders = false;

    /**
     * Encryption algorithms
     */
    public enum Algorithm {
        AES_128_GCM,
        AES_256_GCM,    // Default, most secure
        AES_128_CBC,
        AES_256_CBC
    }

    public static Encryption createDefault() {
        return Encryption.builder().build();
    }
}
