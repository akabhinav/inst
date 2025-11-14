package com.messaging.reliability.impl;

import com.messaging.reliability.Compression;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.*;

/**
 * Message compression handler
 * Feature 26: Message Compression
 */
@Slf4j
public class CompressionHandler {

    private final Compression config;

    public CompressionHandler(Compression config) {
        this.config = config;
    }

    /**
     * Compress data
     */
    public byte[] compress(byte[] data) {
        if (!config.isEnabled()) {
            return data;
        }

        // Check minimum size threshold
        if (data.length < config.getMinSizeBytes()) {
            log.debug("Data size ({} bytes) below compression threshold ({} bytes), skipping compression",
                data.length, config.getMinSizeBytes());
            return data;
        }

        try {
            long startTime = System.currentTimeMillis();
            byte[] compressed;

            switch (config.getAlgorithm()) {
                case GZIP -> compressed = compressGzip(data);
                case SNAPPY -> compressed = compressSnappy(data);
                case LZ4 -> compressed = compressLZ4(data);
                case ZSTD -> compressed = compressZstd(data);
                default -> {
                    log.warn("Unknown compression algorithm: {}", config.getAlgorithm());
                    return data;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            double ratio = (1.0 - ((double) compressed.length / data.length)) * 100;

            log.debug("Compressed {} bytes to {} bytes using {} (ratio: {:.2f}%, time: {}ms)",
                data.length, compressed.length, config.getAlgorithm(), ratio, duration);

            return compressed;

        } catch (Exception e) {
            log.error("Compression failed, returning original data", e);
            return data;
        }
    }

    /**
     * Decompress data
     */
    public byte[] decompress(byte[] data, Compression.Algorithm algorithm) {
        if (!config.isEnabled()) {
            return data;
        }

        try {
            long startTime = System.currentTimeMillis();
            byte[] decompressed;

            switch (algorithm) {
                case GZIP -> decompressed = decompressGzip(data);
                case SNAPPY -> decompressed = decompressSnappy(data);
                case LZ4 -> decompressed = decompressLZ4(data);
                case ZSTD -> decompressed = decompressZstd(data);
                default -> {
                    log.warn("Unknown decompression algorithm: {}", algorithm);
                    return data;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Decompressed {} bytes to {} bytes using {} (time: {}ms)",
                data.length, decompressed.length, algorithm, duration);

            return decompressed;

        } catch (Exception e) {
            log.error("Decompression failed, returning original data", e);
            return data;
        }
    }

    // GZIP compression

    private byte[] compressGzip(byte[] data) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos) {{
            def.setLevel(config.getLevel());
        }}) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] decompressGzip(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }

        return baos.toByteArray();
    }

    // Snappy compression (simplified - would use real Snappy library in production)

    private byte[] compressSnappy(byte[] data) {
        // For now, use Deflater as placeholder
        // In production, use: org.xerial.snappy.Snappy.compress(data)
        log.warn("Snappy compression not fully implemented, using Deflater");
        return compressDeflate(data);
    }

    private byte[] decompressSnappy(byte[] data) {
        // For now, use Inflater as placeholder
        // In production, use: org.xerial.snappy.Snappy.uncompress(data)
        log.warn("Snappy decompression not fully implemented, using Inflater");
        return decompressInflate(data);
    }

    // LZ4 compression (simplified)

    private byte[] compressLZ4(byte[] data) {
        // For now, use Deflater as placeholder
        // In production, use: net.jpountz.lz4.LZ4Factory
        log.warn("LZ4 compression not fully implemented, using Deflater");
        return compressDeflate(data);
    }

    private byte[] decompressLZ4(byte[] data) {
        // For now, use Inflater as placeholder
        // In production, use: net.jpountz.lz4.LZ4Factory
        log.warn("LZ4 decompression not fully implemented, using Inflater");
        return decompressInflate(data);
    }

    // ZSTD compression (simplified)

    private byte[] compressZstd(byte[] data) {
        // For now, use Deflater as placeholder
        // In production, use: com.github.luben.zstd.Zstd.compress(data)
        log.warn("ZSTD compression not fully implemented, using Deflater");
        return compressDeflate(data);
    }

    private byte[] decompressZstd(byte[] data) {
        // For now, use Inflater as placeholder
        // In production, use: com.github.luben.zstd.Zstd.decompress(data)
        log.warn("ZSTD decompression not fully implemented, using Inflater");
        return decompressInflate(data);
    }

    // Fallback: Deflate/Inflate (Java built-in)

    private byte[] compressDeflate(byte[] data) {
        Deflater deflater = new Deflater(config.getLevel());
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            baos.write(buffer, 0, count);
        }

        deflater.end();
        return baos.toByteArray();
    }

    private byte[] decompressInflate(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                baos.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException("Failed to decompress data", e);
        } finally {
            inflater.end();
        }

        return baos.toByteArray();
    }

    /**
     * Check if compression would be beneficial
     */
    public boolean shouldCompress(int dataSize) {
        return config.isEnabled() && dataSize >= config.getMinSizeBytes();
    }
}
