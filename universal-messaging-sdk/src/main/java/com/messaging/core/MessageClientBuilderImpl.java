package com.messaging.core;

import com.messaging.integration.ApiGateway;
import com.messaging.integration.ServiceMesh;
import com.messaging.intelligence.MachineLearning;
import com.messaging.observability.*;
import com.messaging.performance.*;
import com.messaging.provider.ProviderConfig;
import com.messaging.provider.ProviderType;
import com.messaging.provider.impl.ProviderFactory;
import com.messaging.reliability.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MessageClientBuilder
 */
@Slf4j
public class MessageClientBuilderImpl implements MessageClient.MessageClientBuilder {

    private ProviderType providerType = ProviderType.IN_MEMORY;
    private String connectionString;
    private ConnectionPooling connectionPooling;
    private RetryPolicy retryPolicy;
    private DeadLetterQueue deadLetterQueue;
    private Deduplication deduplication;
    private Compression compression;
    private Encryption encryption;
    private CircuitBreaker circuitBreaker;
    private RateLimit rateLimit;
    private TracingConfig tracingConfig;
    private ZeroCopy zeroCopy;
    private ChaosEngineering chaosEngineering;
    private Degradation degradation;
    private SelfHealing selfHealing;
    private DisasterRecovery disasterRecovery;
    private ServiceMesh serviceMesh;
    private ApiGateway apiGateway;
    private Analytics analytics;
    private MachineLearning machineLearning;
    private ProcessIntelligence processIntelligence;
    private FinancialTracking financialTracking;

    @Override
    public MessageClient.MessageClientBuilder provider(String provider) {
        this.providerType = ProviderType.autoDetect(provider);
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder provider(ProviderType providerType) {
        this.providerType = providerType;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder connectionPooling(ConnectionPooling pooling) {
        this.connectionPooling = pooling;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder retry(RetryPolicy retry) {
        this.retryPolicy = retry;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder deadLetterQueue(DeadLetterQueue dlq) {
        this.deadLetterQueue = dlq;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder deduplication(Deduplication dedup) {
        this.deduplication = dedup;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder compression(Compression compression) {
        this.compression = compression;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder encryption(Encryption encryption) {
        this.encryption = encryption;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder circuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder rateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder tracing(TracingConfig tracing) {
        this.tracingConfig = tracing;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder zeroCopy(ZeroCopy zeroCopy) {
        this.zeroCopy = zeroCopy;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder chaosEngineering(ChaosEngineering chaos) {
        this.chaosEngineering = chaos;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder degradation(Degradation degradation) {
        this.degradation = degradation;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder selfHealing(SelfHealing selfHealing) {
        this.selfHealing = selfHealing;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder disasterRecovery(DisasterRecovery dr) {
        this.disasterRecovery = dr;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder serviceMesh(ServiceMesh serviceMesh) {
        this.serviceMesh = serviceMesh;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder apiGateway(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder analytics(Analytics analytics) {
        this.analytics = analytics;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder machineLearning(MachineLearning ml) {
        this.machineLearning = ml;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder processIntelligence(ProcessIntelligence pi) {
        this.processIntelligence = pi;
        return this;
    }

    @Override
    public MessageClient.MessageClientBuilder financials(FinancialTracking financials) {
        this.financialTracking = financials;
        return this;
    }

    @Override
    public MessageClient build() {
        log.info("Building MessageClient with provider: {}", providerType);

        // Build provider config
        ProviderConfig providerConfig = ProviderConfig.builder()
                .providerType(providerType)
                .connectionString(connectionString != null ? connectionString : "localhost")
                .maxPoolSize(connectionPooling != null ? connectionPooling.getMaxSize() : 10)
                .minPoolSize(connectionPooling != null ? connectionPooling.getMinSize() : 2)
                .build();

        // Build configuration
        MessageClientConfig config = MessageClientConfig.builder()
                .providerConfig(providerConfig)
                .retryPolicy(retryPolicy != null ? retryPolicy : RetryPolicy.createDefault())
                .deadLetterQueue(deadLetterQueue != null ? deadLetterQueue : DeadLetterQueue.createDefault())
                .deduplication(deduplication != null ? deduplication : Deduplication.createDefault())
                .compression(compression != null ? compression : Compression.createDefault())
                .encryption(encryption != null ? encryption : Encryption.createDefault())
                .circuitBreaker(circuitBreaker != null ? circuitBreaker : CircuitBreaker.createDefault())
                .rateLimit(rateLimit != null ? rateLimit : RateLimit.createDefault())
                .zeroCopy(zeroCopy != null ? zeroCopy : ZeroCopy.createDefault())
                .degradation(degradation != null ? degradation : Degradation.createDefault())
                .selfHealing(selfHealing != null ? selfHealing : SelfHealing.createDefault())
                .build();

        // Create and initialize client
        MessageClient client = new MessageClientImpl(config);

        log.info("MessageClient built successfully with provider: {}", providerType.getDisplayName());

        return client;
    }
}
