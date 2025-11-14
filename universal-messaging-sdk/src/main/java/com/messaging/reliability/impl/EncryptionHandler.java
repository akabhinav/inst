package com.messaging.reliability.impl;

import com.messaging.reliability.Encryption;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Message encryption handler
 * Feature 27: Message Encryption
 */
@Slf4j
public class EncryptionHandler {

    private final Encryption config;
    private SecretKey secretKey;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    public EncryptionHandler(Encryption config) {
        this.config = config;
        initializeKey();
    }

    /**
     * Initialize encryption key
     */
    private void initializeKey() {
        if (!config.isEnabled()) {
            return;
        }

        try {
            if (config.getKey() != null && !config.getKey().isEmpty()) {
                // Use provided key
                byte[] decodedKey = Base64.getDecoder().decode(config.getKey());
                this.secretKey = new SecretKeySpec(decodedKey, "AES");
                log.info("Encryption key loaded from configuration");
            } else {
                // Generate new key
                int keySize = config.getAlgorithm().name().contains("256") ? 256 : 128;
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(keySize);
                this.secretKey = keyGen.generateKey();
                log.warn("No encryption key provided, generated new key: {}",
                    Base64.getEncoder().encodeToString(secretKey.getEncoded()));
            }
        } catch (Exception e) {
            log.error("Failed to initialize encryption key", e);
            throw new RuntimeException("Failed to initialize encryption", e);
        }
    }

    /**
     * Encrypt data
     */
    public byte[] encrypt(byte[] data) {
        if (!config.isEnabled()) {
            return data;
        }

        try {
            long startTime = System.currentTimeMillis();

            switch (config.getAlgorithm()) {
                case AES_256_GCM, AES_128_GCM -> {
                    byte[] encrypted = encryptAesGcm(data);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Encrypted {} bytes using {} (time: {}ms)",
                        data.length, config.getAlgorithm(), duration);
                    return encrypted;
                }
                case AES_256_CBC, AES_128_CBC -> {
                    byte[] encrypted = encryptAesCbc(data);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Encrypted {} bytes using {} (time: {}ms)",
                        data.length, config.getAlgorithm(), duration);
                    return encrypted;
                }
                default -> {
                    log.warn("Unknown encryption algorithm: {}", config.getAlgorithm());
                    return data;
                }
            }
        } catch (Exception e) {
            log.error("Encryption failed, returning original data", e);
            return data;
        }
    }

    /**
     * Decrypt data
     */
    public byte[] decrypt(byte[] encryptedData, Encryption.Algorithm algorithm) {
        if (!config.isEnabled()) {
            return encryptedData;
        }

        try {
            long startTime = System.currentTimeMillis();

            switch (algorithm) {
                case AES_256_GCM, AES_128_GCM -> {
                    byte[] decrypted = decryptAesGcm(encryptedData);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Decrypted {} bytes using {} (time: {}ms)",
                        encryptedData.length, algorithm, duration);
                    return decrypted;
                }
                case AES_256_CBC, AES_128_CBC -> {
                    byte[] decrypted = decryptAesCbc(encryptedData);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Decrypted {} bytes using {} (time: {}ms)",
                        encryptedData.length, algorithm, duration);
                    return decrypted;
                }
                default -> {
                    log.warn("Unknown decryption algorithm: {}", algorithm);
                    return encryptedData;
                }
            }
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // AES-GCM encryption (recommended - authenticated encryption)

    private byte[] encryptAesGcm(byte[] data) throws Exception {
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        // Encrypt
        byte[] cipherText = cipher.doFinal(data);

        // Prepend IV to ciphertext
        byte[] encryptedData = new byte[GCM_IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
        System.arraycopy(cipherText, 0, encryptedData, GCM_IV_LENGTH, cipherText.length);

        return encryptedData;
    }

    private byte[] decryptAesGcm(byte[] encryptedData) throws Exception {
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

        // Extract ciphertext
        byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        // Decrypt
        return cipher.doFinal(cipherText);
    }

    // AES-CBC encryption (legacy support)

    private byte[] encryptAesCbc(byte[] data) throws Exception {
        // Generate random IV
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));

        // Encrypt
        byte[] cipherText = cipher.doFinal(data);

        // Prepend IV to ciphertext
        byte[] encryptedData = new byte[16 + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, 16);
        System.arraycopy(cipherText, 0, encryptedData, 16, cipherText.length);

        return encryptedData;
    }

    private byte[] decryptAesCbc(byte[] encryptedData) throws Exception {
        // Extract IV
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, 16);

        // Extract ciphertext
        byte[] cipherText = new byte[encryptedData.length - 16];
        System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));

        // Decrypt
        return cipher.doFinal(cipherText);
    }

    /**
     * Get the encryption key (base64 encoded)
     */
    public String getKeyBase64() {
        if (secretKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
