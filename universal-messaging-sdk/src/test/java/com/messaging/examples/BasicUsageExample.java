package com.messaging.examples;

import com.messaging.core.MessageClient;
import com.messaging.core.Message;
import com.messaging.reliability.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Example demonstrating basic usage of Universal Messaging SDK
 */
@Slf4j
public class BasicUsageExample {

    public static void main(String[] args) throws InterruptedException {
        // Example 1: Simple Send/Receive
        simpleExample();

        // Example 2: With Retry and DLQ
        reliableExample();

        // Example 3: With Compression and Encryption
        secureExample();

        // Example 4: Priority Messages
        priorityExample();

        // Example 5: Reactive API
        reactiveExample();

        Thread.sleep(2000); // Let async operations complete
    }

    /**
     * Example 1: Simple send and receive
     */
    public static void simpleExample() {
        log.info("=== Example 1: Simple Send/Receive ===");

        // Create client with in-memory provider (for testing)
        MessageClient client = MessageClient.builder()
                .provider("memory")
                .build();

        // Send a message
        Order order = new Order("ORDER-001", "Customer A", 99.99);
        client.send("orders", order).execute();
        log.info("Sent order: {}", order);

        // Receive messages
        client.receive("orders", Order.class)
                .onMessage(msg -> {
                    log.info("Received order: {}", msg.getPayload());
                })
                .start();
    }

    /**
     * Example 2: With retry and dead letter queue
     */
    public static void reliableExample() {
        log.info("\n=== Example 2: Reliable Messaging with Retry & DLQ ===");

        MessageClient client = MessageClient.builder()
                .provider("memory")
                .retry(RetryPolicy.builder()
                        .maxAttempts(3)
                        .initialBackoff(Duration.ofMillis(100))
                        .backoffMultiplier(2.0)
                        .build())
                .deadLetterQueue(DeadLetterQueue.builder()
                        .enabled(true)
                        .topicPattern("%s-dlq")
                        .maxRetries(3)
                        .onDlq(dlqMsg -> log.warn("Message sent to DLQ: {}", dlqMsg.getMessageId()))
                        .build())
                .build();

        Order order = new Order("ORDER-002", "Customer B", 199.99);
        client.send("orders-reliable", order).execute();
        log.info("Sent order with retry policy: {}", order);
    }

    /**
     * Example 3: With compression and encryption
     */
    public static void secureExample() {
        log.info("\n=== Example 3: Secure Messaging with Compression & Encryption ===");

        MessageClient client = MessageClient.builder()
                .provider("memory")
                .compression(Compression.builder()
                        .enabled(true)
                        .algorithm(Compression.Algorithm.GZIP)
                        .level(6)
                        .build())
                .encryption(Encryption.builder()
                        .enabled(true)
                        .algorithm(Encryption.Algorithm.AES_256_GCM)
                        .key("your-secret-key-base64-encoded")
                        .build())
                .build();

        Order order = new Order("ORDER-003", "Customer C", 299.99);
        client.send("orders-secure", order)
                .compress(true)
                .encrypt(true)
                .execute();
        log.info("Sent encrypted and compressed order: {}", order);
    }

    /**
     * Example 4: Priority messages
     */
    public static void priorityExample() {
        log.info("\n=== Example 4: Priority Messages ===");

        MessageClient client = MessageClient.builder()
                .provider("memory")
                .build();

        // High priority order
        Order highPriorityOrder = new Order("ORDER-004", "VIP Customer", 999.99);
        client.send("orders-priority", highPriorityOrder)
                .priority(Message.Priority.HIGH)
                .execute();
        log.info("Sent HIGH priority order: {}", highPriorityOrder);

        // Normal priority order
        Order normalOrder = new Order("ORDER-005", "Regular Customer", 49.99);
        client.send("orders-priority", normalOrder)
                .priority(Message.Priority.MEDIUM)
                .execute();
        log.info("Sent MEDIUM priority order: {}", normalOrder);

        // Dynamic priority based on amount
        Order dynamicOrder = new Order("ORDER-006", "Dynamic Customer", 5000.00);
        client.send("orders-priority", dynamicOrder)
                .priority(order -> {
                    if (order.getAmount() > 1000) return Message.Priority.HIGH;
                    if (order.getAmount() > 100) return Message.Priority.MEDIUM;
                    return Message.Priority.LOW;
                })
                .execute();
        log.info("Sent dynamically prioritized order: {}", dynamicOrder);
    }

    /**
     * Example 5: Reactive API
     */
    public static void reactiveExample() {
        log.info("\n=== Example 5: Reactive API ===");

        MessageClient client = MessageClient.builder()
                .provider("memory")
                .build();

        // Send async
        Order order = new Order("ORDER-007", "Reactive Customer", 149.99);
        client.sendAsync("orders-reactive", order)
                .subscribe(
                        result -> log.info("Async send completed: {}", result.getMessageId()),
                        error -> log.error("Async send failed", error)
                );

        // Receive reactively
        client.receiveReactive("orders-reactive", Order.class)
                .filter(msg -> msg.getPayload().getAmount() > 100)
                .map(Message::getPayload)
                .subscribe(o -> log.info("Received reactive order: {}", o));

        log.info("Reactive operations started (async)");
    }

    /**
     * Example Order class
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order {
        private String orderId;
        private String customer;
        private Double amount;
    }
}
