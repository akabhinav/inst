package com.messaging.core;

import com.messaging.core.SendOperation.DeliveryGuarantee;
import com.messaging.core.Message.Priority;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementation of SendOperation interface.
 * Provides fluent builder pattern for configuring and sending messages.
 *
 * @param <T> The type of the message payload
 */
@Slf4j
public class SendOperationImpl<T> implements SendOperation<T> {

    private final MessageClientImpl client;
    private final String topic;
    private final T payload;

    // Configuration attributes
    private Priority priority = Priority.MEDIUM;
    private Function<T, Priority> priorityFunction;
    private String partitionKey;
    private final Map<String, String> headers = new HashMap<>();
    private String correlationId;
    private String replyTo;
    private Duration ttl;
    private int version = 1;
    private boolean compress = false;
    private boolean encrypt = false;
    private DeliveryGuarantee deliveryGuarantee = DeliveryGuarantee.AT_LEAST_ONCE;

    /**
     * Constructor for SendOperationImpl
     *
     * @param client MessageClientImpl instance
     * @param topic Topic name
     * @param payload Message payload
     */
    public SendOperationImpl(MessageClientImpl client, String topic, T payload) {
        this.client = client;
        this.topic = topic;
        this.payload = payload;
        log.debug("Created SendOperationImpl for topic: {} with payload type: {}", topic, payload.getClass().getSimpleName());
    }

    @Override
    public SendOperation<T> priority(Priority priority) {
        log.debug("Setting message priority to: {}", priority);
        this.priority = priority;
        return this;
    }

    @Override
    public SendOperation<T> priority(Function<T, Priority> priorityFunction) {
        log.debug("Setting dynamic priority function");
        this.priorityFunction = priorityFunction;
        return this;
    }

    @Override
    public SendOperation<T> partitionKey(String key) {
        log.debug("Setting partition key: {}", key);
        this.partitionKey = key;
        return this;
    }

    @Override
    public SendOperation<T> header(String key, String value) {
        log.debug("Adding header: {} = {}", key, value);
        this.headers.put(key, value);
        return this;
    }

    @Override
    public SendOperation<T> headers(Map<String, String> headers) {
        log.debug("Adding {} headers", headers.size());
        this.headers.putAll(headers);
        return this;
    }

    @Override
    public SendOperation<T> correlationId(String correlationId) {
        log.debug("Setting correlation ID: {}", correlationId);
        this.correlationId = correlationId;
        return this;
    }

    @Override
    public SendOperation<T> replyTo(String topic) {
        log.debug("Setting reply-to topic: {}", topic);
        this.replyTo = topic;
        return this;
    }

    @Override
    public SendOperation<T> ttl(Duration ttl) {
        log.debug("Setting TTL: {}", ttl);
        this.ttl = ttl;
        return this;
    }

    @Override
    public SendOperation<T> version(int version) {
        log.debug("Setting message version: {}", version);
        this.version = version;
        return this;
    }

    @Override
    public SendOperation<T> compress(boolean compress) {
        log.debug("Setting compression: {}", compress);
        this.compress = compress;
        return this;
    }

    @Override
    public SendOperation<T> encrypt(boolean encrypt) {
        log.debug("Setting encryption: {}", encrypt);
        this.encrypt = encrypt;
        return this;
    }

    @Override
    public SendOperation<T> deliveryGuarantee(DeliveryGuarantee guarantee) {
        log.debug("Setting delivery guarantee: {}", guarantee);
        this.deliveryGuarantee = guarantee;
        return this;
    }

    @Override
    public SendResult execute() {
        log.info("Executing send operation for topic: {}", topic);
        try {
            // Build the message with all configured options
            Message<T> message = buildMessage();

            // Send via provider (blocking)
            SendResult result = client.getProvider()
                    .send(message)
                    .block();

            if (result != null && result.isSuccess()) {
                log.info("Message sent successfully to topic: {} with ID: {}", topic, result.getMessageId());
                return result;
            } else {
                log.error("Message send failed for topic: {}", topic);
                return SendResult.builder()
                        .topic(topic)
                        .success(false)
                        .error("Send operation failed")
                        .build();
            }
        } catch (Exception e) {
            log.error("Exception during send operation for topic: {}", topic, e);
            return SendResult.builder()
                    .topic(topic)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Override
    public Mono<SendResult> executeAsync() {
        log.info("Executing async send operation for topic: {}", topic);
        return Mono.fromCallable(this::buildMessage)
                .flatMap(message -> {
                    log.debug("Sending message asynchronously to topic: {}", topic);
                    return client.getProvider().send(message);
                })
                .map(this::convertToSendResult)
                .doOnSuccess(result -> {
                    if (result.isSuccess()) {
                        log.info("Async message sent successfully to topic: {} with ID: {}", topic, result.getMessageId());
                    } else {
                        log.error("Async message send failed for topic: {}", topic);
                    }
                })
                .doOnError(error -> log.error("Exception during async send operation for topic: {}", topic, error));
    }

    /**
     * Build the message with all configured options
     *
     * @return Message instance
     */
    private Message<T> buildMessage() {
        // Determine priority
        Priority finalPriority = priority;
        if (priorityFunction != null) {
            finalPriority = priorityFunction.apply(payload);
        }

        // Build TTL in milliseconds
        Long ttlMs = null;
        if (ttl != null) {
            ttlMs = ttl.toMillis();
        }

        // Use provided correlation ID or generate one
        String correlId = correlationId != null ? correlationId : UUID.randomUUID().toString();

        log.debug("Building message with priority: {}, version: {}, compression: {}, encryption: {}",
                finalPriority, version, compress, encrypt);

        return Message.builder()
                .topic(topic)
                .payload(payload)
                .priority(finalPriority)
                .partitionKey(partitionKey)
                .headers(new HashMap<>(headers))
                .correlationId(correlId)
                .replyTo(replyTo)
                .ttl(ttlMs)
                .version(version)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Convert ProviderSendResult to SendResult
     *
     * @param providerResult ProviderSendResult from provider
     * @return SendResult
     */
    private SendResult convertToSendResult(com.messaging.provider.ProviderSendResult providerResult) {
        return SendResult.builder()
                .messageId(providerResult.getMessageId())
                .topic(providerResult.getTopic())
                .partition(providerResult.getPartition())
                .offset(providerResult.getOffset())
                .timestamp(Instant.now())
                .success(providerResult.isSuccess())
                .error(providerResult.getError())
                .latencyMs(0) // TODO: Calculate actual message latency
                .sizeBytes(0) // TODO: Calculate actual message size
                .build();
    }
}
