package com.messaging.core;

import com.messaging.pattern.MessageFilter;
import com.messaging.performance.AdaptiveBatching;
import com.messaging.performance.ParallelProcessing;
import com.messaging.performance.Prefetch;
import com.messaging.performance.PriorityHandling;
import com.messaging.reliability.VersionHandling;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Implementation of ReceiveOperation interface.
 * Provides fluent builder pattern for configuring message consumption.
 *
 * @param <T> The type of the message payload
 */
@Slf4j
public class ReceiveOperationImpl<T> implements ReceiveOperation<T> {

    private final MessageClientImpl client;
    private final String topic;
    private final Class<T> payloadType;

    // Configuration attributes
    private Consumer<Message<T>> messageHandler;
    private Consumer<List<Message<T>>> batchHandler;
    private Consumer<Throwable> errorHandler;
    private MessageFilter<T> filter;
    private String consumerGroup;
    private int concurrency = 1;
    private AdaptiveBatching adaptiveBatching;
    private PriorityHandling priorityHandling;
    private ParallelProcessing parallelProcessing;
    private Prefetch prefetch;
    private VersionHandling versionHandling;
    private boolean mlPriority = false;
    private boolean autoAck = false;
    private int batchSize = 1;

    // State management
    private volatile Disposable subscription;
    private final Object subscriptionLock = new Object();
    private volatile boolean running = false;

    /**
     * Constructor for ReceiveOperationImpl
     *
     * @param client MessageClientImpl instance
     * @param topic Topic name
     * @param payloadType Payload class type
     */
    public ReceiveOperationImpl(MessageClientImpl client, String topic, Class<T> payloadType) {
        this.client = client;
        this.topic = topic;
        this.payloadType = payloadType;
        log.debug("Created ReceiveOperationImpl for topic: {} with type: {}", topic, payloadType.getSimpleName());
    }

    @Override
    public ReceiveOperation<T> onMessage(Consumer<Message<T>> handler) {
        log.debug("Setting message handler for topic: {}", topic);
        this.messageHandler = handler;
        return this;
    }

    @Override
    public ReceiveOperation<T> onBatch(Consumer<List<Message<T>>> handler) {
        log.debug("Setting batch handler for topic: {} with batch size: {}", topic, batchSize);
        this.batchHandler = handler;
        return this;
    }

    @Override
    public ReceiveOperation<T> onError(Consumer<Throwable> errorHandler) {
        log.debug("Setting error handler for topic: {}", topic);
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    public ReceiveOperation<T> filter(MessageFilter<T> filter) {
        log.debug("Setting message filter for topic: {}", topic);
        this.filter = filter;
        return this;
    }

    @Override
    public ReceiveOperation<T> consumerGroup(String group) {
        log.debug("Setting consumer group: {} for topic: {}", group, topic);
        this.consumerGroup = group;
        return this;
    }

    @Override
    public ReceiveOperation<T> concurrency(int concurrency) {
        log.debug("Setting concurrency to: {} for topic: {}", concurrency, topic);
        this.concurrency = Math.max(1, concurrency);
        return this;
    }

    @Override
    public ReceiveOperation<T> adaptiveBatching(AdaptiveBatching batching) {
        log.debug("Enabling adaptive batching for topic: {}", topic);
        this.adaptiveBatching = batching;
        return this;
    }

    @Override
    public ReceiveOperation<T> priorityHandling(PriorityHandling priorityHandling) {
        log.debug("Setting priority handling for topic: {}", topic);
        this.priorityHandling = priorityHandling;
        return this;
    }

    @Override
    public ReceiveOperation<T> priorityLane(Message.Priority priority, String topic) {
        log.debug("Setting priority lane for priority: {} to topic: {}", priority, topic);
        // TODO: Implement priority lane routing
        return this;
    }

    @Override
    public ReceiveOperation<T> parallelism(ParallelProcessing parallel) {
        log.debug("Enabling parallel processing for topic: {}", topic);
        this.parallelProcessing = parallel;
        return this;
    }

    @Override
    public ReceiveOperation<T> prefetch(Prefetch prefetch) {
        log.debug("Setting prefetch for topic: {}", topic);
        this.prefetch = prefetch;
        return this;
    }

    @Override
    public ReceiveOperation<T> versionHandling(VersionHandling versionHandling) {
        log.debug("Enabling version handling for topic: {}", topic);
        this.versionHandling = versionHandling;
        return this;
    }

    @Override
    public ReceiveOperation<T> mlPriority(boolean enabled) {
        log.debug("Setting ML-based priority prediction to: {} for topic: {}", enabled, topic);
        this.mlPriority = enabled;
        return this;
    }

    @Override
    public ReceiveOperation<T> autoAck(boolean autoAck) {
        log.debug("Setting auto-acknowledge to: {} for topic: {}", autoAck, topic);
        this.autoAck = autoAck;
        return this;
    }

    @Override
    public ReceiveOperation<T> batchSize(int size) {
        log.debug("Setting batch size to: {} for topic: {}", size, topic);
        this.batchSize = Math.max(1, size);
        return this;
    }

    @Override
    public void start() {
        synchronized (subscriptionLock) {
            if (running) {
                log.warn("Consumer already running for topic: {}", topic);
                return;
            }

            log.info("Starting message consumption for topic: {} with consumer group: {}", topic, consumerGroup);
            running = true;

            try {
                // Create the reactive stream
                Flux<Message<T>> messageStream = createMessageStream();

                // Subscribe to the stream
                subscription = messageStream
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                                this::handleMessage,
                                this::handleError,
                                this::handleComplete
                        );

                log.info("Consumer started for topic: {}", topic);
            } catch (Exception e) {
                running = false;
                log.error("Failed to start consumer for topic: {}", topic, e);
                throw new RuntimeException("Failed to start consumer", e);
            }
        }
    }

    @Override
    public void stop() {
        synchronized (subscriptionLock) {
            if (!running) {
                log.warn("Consumer not running for topic: {}", topic);
                return;
            }

            log.info("Stopping message consumption for topic: {}", topic);

            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
                log.debug("Subscription disposed for topic: {}", topic);
            }

            running = false;
            log.info("Consumer stopped for topic: {}", topic);
        }
    }

    /**
     * Create the message stream based on configuration
     *
     * @return Flux of messages
     */
    private Flux<Message<T>> createMessageStream() {
        log.debug("Creating message stream for topic: {} with batch size: {}", topic, batchSize);

        Flux<Message<T>> stream = client.getProvider()
                .receive(topic, payloadType);

        // Apply filtering
        if (filter != null) {
            stream = stream.filter(message -> {
                try {
                    boolean passes = filter.test(message);
                    if (!passes) {
                        log.debug("Message {} filtered out from topic: {}", message.getId(), topic);
                    }
                    return passes;
                } catch (Exception e) {
                    log.error("Error in filter for topic: {}", topic, e);
                    return false;
                }
            });
        }

        // Apply batching if batch size > 1
        if (batchSize > 1) {
            stream = stream
                    .buffer(batchSize)
                    .flatMap(batch -> {
                        log.debug("Processing batch of {} messages from topic: {}", batch.size(), topic);
                        if (batchHandler != null) {
                            try {
                                batchHandler.accept(batch);
                            } catch (Exception e) {
                                log.error("Error in batch handler for topic: {}", topic, e);
                            }
                        }
                        return Flux.fromIterable(batch);
                    });
        }

        // Apply concurrency if configured
        if (concurrency > 1) {
            stream = stream
                    .parallel(concurrency)
                    .sequential();
        }

        return stream;
    }

    /**
     * Handle individual message
     *
     * @param message Message to handle
     */
    private void handleMessage(Message<T> message) {
        log.debug("Received message {} from topic: {} with priority: {}", message.getId(), topic, message.getPriority());

        try {
            if (messageHandler != null) {
                messageHandler.accept(message);
            }

            if (autoAck) {
                client.getProvider().acknowledge(message.getId());
                log.debug("Message {} auto-acknowledged", message.getId());
            }
        } catch (Exception e) {
            log.error("Error handling message {} from topic: {}", message.getId(), topic, e);
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
            if (!autoAck) {
                // Negative acknowledge if not already auto-acknowledged
                client.getProvider().nack(message.getId());
            }
        }
    }

    /**
     * Handle errors from the stream
     *
     * @param error The error that occurred
     */
    private void handleError(Throwable error) {
        log.error("Error in message stream for topic: {}", topic, error);
        if (errorHandler != null) {
            try {
                errorHandler.accept(error);
            } catch (Exception e) {
                log.error("Error in error handler for topic: {}", topic, e);
            }
        }
    }

    /**
     * Handle stream completion
     */
    private void handleComplete() {
        log.info("Message stream completed for topic: {}", topic);
        running = false;
    }

    /**
     * Check if consumer is currently running
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
