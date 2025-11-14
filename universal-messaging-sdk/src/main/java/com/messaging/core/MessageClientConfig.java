package com.messaging.core;

import com.messaging.performance.ZeroCopy;
import com.messaging.provider.ProviderConfig;
import com.messaging.reliability.*;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for MessageClient
 */
@Data
@Builder
public class MessageClientConfig {
    private ProviderConfig providerConfig;
    private RetryPolicy retryPolicy;
    private DeadLetterQueue deadLetterQueue;
    private Deduplication deduplication;
    private Compression compression;
    private Encryption encryption;
    private CircuitBreaker circuitBreaker;
    private RateLimit rateLimit;
    private ZeroCopy zeroCopy;
    private Degradation degradation;
    private SelfHealing selfHealing;
}
