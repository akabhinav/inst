package com.messaging.provider.impl;

import com.messaging.core.HealthStatus;
import com.messaging.core.Message;
import com.messaging.provider.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka provider implementation
 * Feature 1: Multi-Broker Support (Kafka)
 */
@Slf4j
public class KafkaProvider implements MessageProvider {

    private KafkaProducer<String, String> producer;
    private ProviderConfig config;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);

    @Override
    public void initialize(ProviderConfig config) {
        if (initialized.getAndSet(true)) {
            log.warn("KafkaProvider already initialized");
            return;
        }

        this.config = config;
        log.info("Initializing Kafka provider with connection: {}", config.getConnectionString());

        // Configure Kafka producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getConnectionString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure durability
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) config.getRequestTimeoutMs());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once semantics

        // Add custom properties
        if (config.getProperties() != null) {
            config.getProperties().forEach((key, value) -> props.put(key, value));
        }

        this.producer = new KafkaProducer<>(props);
        log.info("Kafka producer initialized successfully");
    }

    @Override
    public <T> Mono<ProviderSendResult> send(Message<T> message) {
        if (closed.get()) {
            return Mono.error(new IllegalStateException("KafkaProvider is closed"));
        }

        return Mono.fromCallable(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Serialize message payload to JSON
                String jsonPayload = serializePayload(message.getPayload());

                // Create Kafka record
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    message.getTopic(),
                    message.getPartitionKey(), // Use partition key if provided
                    jsonPayload
                );

                // Add headers
                if (message.getHeaders() != null) {
                    message.getHeaders().forEach((key, value) ->
                        record.headers().add(key, value.getBytes())
                    );
                }

                // Add metadata headers
                record.headers().add("message-id", message.getId().getBytes());
                record.headers().add("priority", String.valueOf(message.getPriority().getValue()).getBytes());
                record.headers().add("timestamp", String.valueOf(message.getTimestamp().toEpochMilli()).getBytes());

                // Send synchronously (for Mono semantics)
                RecordMetadata metadata = producer.send(record).get();

                messagesSent.incrementAndGet();
                long latency = System.currentTimeMillis() - startTime;

                log.debug("Sent message to Kafka topic: {}, partition: {}, offset: {}, latency: {}ms",
                    metadata.topic(), metadata.partition(), metadata.offset(), latency);

                return ProviderSendResult.builder()
                    .messageId(message.getId())
                    .topic(metadata.topic())
                    .partition(metadata.partition())
                    .offset(metadata.offset())
                    .success(true)
                    .build();

            } catch (Exception e) {
                messagesFailed.incrementAndGet();
                log.error("Failed to send message to Kafka", e);

                return ProviderSendResult.builder()
                    .messageId(message.getId())
                    .topic(message.getTopic())
                    .success(false)
                    .error(e.getMessage())
                    .build();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public <T> Flux<Message<T>> receive(String topic, Class<T> payloadType) {
        if (closed.get()) {
            return Flux.error(new IllegalStateException("KafkaProvider is closed"));
        }

        return Flux.create(sink -> {
            // Configure Kafka consumer
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getConnectionString());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "universal-messaging-" + UUID.randomUUID());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(topic));

            log.info("Kafka consumer subscribed to topic: {}", topic);

            // Poll loop
            sink.onDispose(() -> {
                log.info("Closing Kafka consumer for topic: {}", topic);
                consumer.close();
            });

            new Thread(() -> {
                try {
                    while (!sink.isCancelled() && !closed.get()) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                        for (ConsumerRecord<String, String> record : records) {
                            try {
                                // Deserialize payload
                                T payload = deserializePayload(record.value(), payloadType);

                                // Extract headers
                                java.util.Map<String, String> headers = new java.util.HashMap<>();
                                record.headers().forEach(header ->
                                    headers.put(header.key(), new String(header.value()))
                                );

                                // Extract metadata
                                String messageId = headers.getOrDefault("message-id", UUID.randomUUID().toString());
                                String priorityStr = headers.getOrDefault("priority", "5");
                                Message.Priority priority = getPriorityFromValue(Integer.parseInt(priorityStr));

                                // Build message
                                Message<T> message = Message.<T>builder()
                                    .id(messageId)
                                    .topic(record.topic())
                                    .payload(payload)
                                    .headers(headers)
                                    .priority(priority)
                                    .partitionKey(record.key())
                                    .build();

                                messagesReceived.incrementAndGet();
                                sink.next(message);

                                // Commit offset after successful processing
                                consumer.commitSync();

                            } catch (Exception e) {
                                log.error("Error processing Kafka record", e);
                                messagesFailed.incrementAndGet();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in Kafka consumer loop", e);
                    sink.error(e);
                } finally {
                    consumer.close();
                }
            }, "kafka-consumer-" + topic).start();

        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void acknowledge(String messageId) {
        // Kafka commits are handled in receive() method
        log.debug("Acknowledged message: {}", messageId);
    }

    @Override
    public void nack(String messageId) {
        // For Kafka, we would seek back to reprocess
        log.debug("Negative acknowledged message: {}", messageId);
    }

    @Override
    public HealthStatus getHealth() {
        if (closed.get()) {
            return HealthStatus.builder()
                .status(HealthStatus.Status.UNHEALTHY)
                .build();
        }

        try {
            // Try to get metadata to verify connection
            producer.partitionsFor("health-check-topic");

            return HealthStatus.builder()
                .status(HealthStatus.Status.HEALTHY)
                .build();
        } catch (Exception e) {
            return HealthStatus.builder()
                .status(HealthStatus.Status.UNHEALTHY)
                .build();
        }
    }

    @Override
    public ProviderMetrics getMetrics() {
        return ProviderMetrics.builder()
            .messagesSent(messagesSent.get())
            .messagesReceived(messagesReceived.get())
            .messagesFailedSend(messagesFailed.get())
            .activeConnections(closed.get() ? 0 : 1)
            .build();
    }

    @Override
    public void close() {
        if (closed.getAndSet(true)) {
            return;
        }

        log.info("Closing Kafka provider");

        if (producer != null) {
            producer.close(Duration.ofSeconds(10));
        }

        log.info("Kafka provider closed");
    }

    @Override
    public ProviderType getType() {
        return ProviderType.KAFKA;
    }

    @Override
    public String getName() {
        return "Apache Kafka Provider";
    }

    // Helper methods

    private <T> String serializePayload(T payload) {
        if (payload instanceof String) {
            return (String) payload;
        }
        // Use Jackson for JSON serialization
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }

    private <T> T deserializePayload(String json, Class<T> type) {
        if (type == String.class) {
            return type.cast(json);
        }
        // Use Jackson for JSON deserialization
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize payload", e);
        }
    }

    private Message.Priority getPriorityFromValue(int value) {
        for (Message.Priority p : Message.Priority.values()) {
            if (p.getValue() == value) {
                return p;
            }
        }
        return Message.Priority.MEDIUM;
    }
}
