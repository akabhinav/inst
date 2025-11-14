package com.messaging.provider.impl;

import com.messaging.core.HealthStatus;
import com.messaging.core.Message;
import com.messaging.provider.MessageProvider;
import com.messaging.provider.ProviderConfig;
import com.messaging.provider.ProviderMetrics;
import com.messaging.provider.ProviderSendResult;
import com.messaging.provider.ProviderType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of MessageProvider for testing and development.
 * Stores messages in memory using ConcurrentHashMap with Queue for each topic.
 *
 * NOTE: This provider is not suitable for production use as messages are lost on shutdown.
 */
@Slf4j
public class InMemoryProvider implements MessageProvider {

    private final ConcurrentHashMap<String, Queue<Message<?>>> topics = new ConcurrentHashMap<>();
    private final ProviderMetrics.ProviderMetricsBuilder metricsBuilder = ProviderMetrics.builder();
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    @Override
    public void initialize(ProviderConfig config) {
        log.info("Initializing InMemoryProvider");
        initialized = true;
        log.debug("InMemoryProvider initialized with config: {}", config);
    }

    @Override
    public <T> Mono<ProviderSendResult> send(Message<T> message) {
        if (closed) {
            log.error("Provider is closed, cannot send message");
            return Mono.error(new IllegalStateException("Provider is closed"));
        }

        if (!initialized) {
            log.error("Provider is not initialized");
            return Mono.error(new IllegalStateException("Provider is not initialized"));
        }

        return Mono.fromCallable(() -> {
            long startTime = System.currentTimeMillis();
            try {
                String topic = message.getTopic();
                if (topic == null || topic.isEmpty()) {
                    throw new IllegalArgumentException("Message topic cannot be null or empty");
                }

                // Get or create queue for this topic
                Queue<Message<?>> queue = topics.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>());

                // Add message to queue
                queue.offer(message);
                messagesSent.incrementAndGet();

                long latency = System.currentTimeMillis() - startTime;
                log.debug("Message sent to topic: {} with ID: {} (latency: {}ms)", topic, message.getId(), latency);

                return ProviderSendResult.builder()
                        .messageId(message.getId())
                        .topic(topic)
                        .success(true)
                        .build();
            } catch (Exception e) {
                messagesFailed.incrementAndGet();
                log.error("Failed to send message", e);
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public <T> Flux<Message<T>> receive(String topic, Class<T> payloadType) {
        if (closed) {
            log.error("Provider is closed, cannot receive messages");
            return Flux.error(new IllegalStateException("Provider is closed"));
        }

        if (!initialized) {
            log.error("Provider is not initialized");
            return Flux.error(new IllegalStateException("Provider is not initialized"));
        }

        log.debug("Starting to receive messages from topic: {} with type: {}", topic, payloadType.getSimpleName());

        return Flux.create(sink -> {
            Queue<Message<?>> queue = topics.get(topic);

            if (queue == null) {
                log.warn("No messages available for topic: {}", topic);
                sink.complete();
                return;
            }

            // Poll messages from the queue
            Thread pollingThread = new Thread(() -> {
                try {
                    while (!closed && !sink.isCancelled()) {
                        Message<?> message = queue.poll();
                        if (message != null) {
                            try {
                                @SuppressWarnings("unchecked")
                                Message<T> typedMessage = (Message<T>) message;
                                messagesReceived.incrementAndGet();
                                sink.next(typedMessage);
                                log.debug("Message received from topic: {} with ID: {}", topic, message.getId());
                            } catch (ClassCastException e) {
                                messagesFailed.incrementAndGet();
                                log.error("Type mismatch for message in topic: {}", topic, e);
                            }
                        } else {
                            // Sleep briefly to avoid busy waiting
                            Thread.sleep(100);
                        }
                    }
                    sink.complete();
                } catch (InterruptedException e) {
                    log.debug("Polling thread interrupted for topic: {}", topic);
                    Thread.currentThread().interrupt();
                    sink.error(e);
                }
            });

            pollingThread.setName("InMemoryProvider-Polling-" + topic);
            pollingThread.setDaemon(true);
            pollingThread.start();

            // Clean up on cancel
            sink.onDispose(() -> {
                log.debug("Receive operation cancelled for topic: {}", topic);
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void acknowledge(String messageId) {
        log.debug("Acknowledged message ID: {}", messageId);
        // TODO: Implement acknowledgment tracking if needed
    }

    @Override
    public void nack(String messageId) {
        log.debug("Negative acknowledged message ID: {}", messageId);
        // TODO: Implement requeue logic if needed
    }

    @Override
    public HealthStatus getHealth() {
        log.debug("Getting health status");
        return HealthStatus.builder()
                .status(closed ? HealthStatus.Status.UNHEALTHY : HealthStatus.Status.HEALTHY)
                .timestamp(Instant.now())
                .components(List.of(
                        HealthStatus.ComponentHealth.builder()
                                .name("InMemory Storage")
                                .status(HealthStatus.Status.HEALTHY)
                                .message("In-memory queue storage is operational")
                                .responseTimeMs(1.0)
                                .build()
                ))
                .build();
    }

    @Override
    public ProviderMetrics getMetrics() {
        log.debug("Getting provider metrics");
        return ProviderMetrics.builder()
                .messagesSent(messagesSent.get())
                .messagesReceived(messagesReceived.get())
                .messagesFailedSend(messagesFailed.get())
                .avgSendLatencyMs(0.0)
                .avgReceiveLatencyMs(0.0)
                .build();
    }

    @Override
    public void close() {
        if (!closed) {
            log.info("Closing InMemoryProvider");
            closed = true;
            topics.clear();
            log.info("InMemoryProvider closed successfully");
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.IN_MEMORY;
    }

    @Override
    public String getName() {
        return "InMemoryProvider";
    }

    /**
     * Get the queue for a specific topic (for testing purposes)
     *
     * @param topic Topic name
     * @return Queue of messages for the topic, or empty queue if topic doesn't exist
     */
    public Queue<Message<?>> getTopicQueue(String topic) {
        return topics.getOrDefault(topic, new LinkedBlockingQueue<>());
    }

    /**
     * Clear all topics (for testing purposes)
     */
    public void clearAllTopics() {
        topics.clear();
        messagesSent.set(0);
        messagesReceived.set(0);
        messagesFailed.set(0);
        log.debug("All topics cleared");
    }
}
