package com.messaging.provider;

/**
 * Supported message broker providers
 *
 * Features 1-10: Universal Broker Support
 */
public enum ProviderType {

    // Open Source Brokers
    KAFKA("Apache Kafka", "kafka", 9092),
    RABBITMQ("RabbitMQ", "rabbitmq", 5672),
    ACTIVEMQ("Apache ActiveMQ", "activemq", 61616),
    ARTEMIS("Apache ActiveMQ Artemis", "artemis", 61616),
    PULSAR("Apache Pulsar", "pulsar", 6650),
    NATS("NATS", "nats", 4222),
    REDIS("Redis Pub/Sub", "redis", 6379),
    ZEROMQ("ZeroMQ", "zeromq", 5555),

    // Cloud Providers
    AWS_SQS("Amazon SQS", "sqs", -1),
    AWS_SNS("Amazon SNS", "sns", -1),
    AWS_KINESIS("Amazon Kinesis", "kinesis", -1),
    GOOGLE_PUBSUB("Google Cloud Pub/Sub", "pubsub", -1),
    AZURE_SERVICEBUS("Azure Service Bus", "servicebus", -1),
    AZURE_EVENTHUB("Azure Event Hub", "eventhub", -1),

    // Specialized
    KAFKA_STREAMS("Kafka Streams", "kafka-streams", -1),
    RABBITMQ_STREAMS("RabbitMQ Streams", "rabbitmq-streams", -1),

    // In-Memory (for testing)
    IN_MEMORY("In-Memory", "memory", -1),
    EMBEDDED("Embedded Broker", "embedded", -1);

    private final String displayName;
    private final String code;
    private final int defaultPort;

    ProviderType(String displayName, String code, int defaultPort) {
        this.displayName = displayName;
        this.code = code;
        this.defaultPort = defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Auto-detect provider from connection string or code
     * Feature 2: Provider Auto-Detection
     */
    public static ProviderType autoDetect(String input) {
        if (input == null || input.isEmpty()) {
            return IN_MEMORY;
        }

        String lower = input.toLowerCase();

        // Check if it's a code
        for (ProviderType type : values()) {
            if (type.code.equals(lower)) {
                return type;
            }
        }

        // Auto-detect from connection string patterns
        if (lower.contains("kafka") || lower.contains(":9092")) {
            return KAFKA;
        } else if (lower.contains("rabbitmq") || lower.contains(":5672")) {
            return RABBITMQ;
        } else if (lower.contains("sqs.") || lower.contains("amazonaws.com/sqs")) {
            return AWS_SQS;
        } else if (lower.contains("sns.") || lower.contains("amazonaws.com/sns")) {
            return AWS_SNS;
        } else if (lower.contains("pubsub.googleapis.com")) {
            return GOOGLE_PUBSUB;
        } else if (lower.contains("servicebus.windows.net")) {
            return AZURE_SERVICEBUS;
        } else if (lower.contains("redis") || lower.contains(":6379")) {
            return REDIS;
        } else if (lower.contains("nats") || lower.contains(":4222")) {
            return NATS;
        } else if (lower.contains("pulsar") || lower.contains(":6650")) {
            return PULSAR;
        }

        // Default to in-memory for testing
        return IN_MEMORY;
    }

    /**
     * Check if provider supports feature
     */
    public boolean supportsFeature(BrokerFeature feature) {
        return switch (this) {
            case KAFKA, KAFKA_STREAMS -> true; // Kafka supports all features
            case RABBITMQ, RABBITMQ_STREAMS -> feature != BrokerFeature.PARTITIONING;
            case AWS_SQS -> feature == BrokerFeature.QUEUE || feature == BrokerFeature.DLQ;
            case AWS_SNS -> feature == BrokerFeature.PUBSUB;
            default -> true; // Assume supported by default
        };
    }

    /**
     * Broker features enum
     */
    public enum BrokerFeature {
        QUEUE,
        PUBSUB,
        PARTITIONING,
        TRANSACTIONS,
        DLQ,
        TTL,
        PRIORITY,
        ROUTING
    }
}
