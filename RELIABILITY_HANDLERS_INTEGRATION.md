# Reliability Handlers Integration Guide

## Overview

The `SendOperationImpl.java` has been fully updated to integrate all reliability handlers created for the messaging SDK. The implementation maintains the fluent API pattern while adding sophisticated reliability features at each stage of message sending.

## Files Updated

### 1. **SendOperationImpl.java**
Location: `/home/user/inst/universal-messaging-sdk/src/main/java/com/messaging/core/SendOperationImpl.java`

#### Changes:
- Added 7 reliability handler fields (all optional for backward compatibility)
- Added `setReliabilityHandlers()` method to accept handler instances
- Enhanced `execute()` method with reliability checks and handlers
- Enhanced `executeAsync()` method with reactive reliability checks and handlers
- Updated JavaDoc to reflect new reliability features

### 2. **MessageClientImpl.java**
Location: `/home/user/inst/universal-messaging-sdk/src/main/java/com/messaging/core/MessageClientImpl.java`

#### Changes:
- Added initialization of all reliability handlers in constructor
- Added private `initializeReliabilityHandlers()` method
- Updated `send()` method to inject handlers into SendOperationImpl
- Updated `close()` method to properly shutdown handlers
- Updated JavaDoc to reflect new reliability features

## Integration Flow

### For `execute()` (Synchronous Execution)

```
1. Build Message
   ↓
2. Rate Limit Check (RateLimiterHandler.tryAcquire())
   ├─ Allowed → Continue
   └─ Rejected → Return error
   ↓
3. Circuit Breaker Check (CircuitBreakerHandler.allowRequest())
   ├─ Allowed → Continue
   └─ Open → Return error
   ↓
4. Deduplication Check (DeduplicationHandler.isDuplicate())
   ├─ Not duplicate → Continue
   └─ Duplicate → Return error
   ↓
5. Compression (if enabled, CompressionHandler.compress())
   ↓
6. Encryption (if enabled, EncryptionHandler.encrypt())
   ↓
7. Send with Retry (RetryExecutor.execute())
   ├─ Success → Record success & Return
   └─ Failure → Record failure & Handle DLQ
   ↓
8. Circuit Breaker Recording
   ├─ recordSuccess() on success
   └─ recordFailure() on failure
   ↓
9. Dead Letter Queue (DeadLetterQueueHandler.handleFailedMessage())
   (Only on failure)
```

### For `executeAsync()` (Asynchronous Execution)

```
1. Build Message (async)
   ↓
2. Rate Limit Check
   ├─ Allowed → Continue
   └─ Rejected → Return failed Mono
   ↓
3. Circuit Breaker Check
   ├─ Allowed → Continue
   └─ Open → Return failed Mono
   ↓
4. Deduplication Check
   ├─ Not duplicate → Continue
   └─ Duplicate → Return failed Mono
   ↓
5. Compression (if enabled)
   ↓
6. Encryption (if enabled)
   ↓
7. Send with Retry (Reactor-based retry)
   ↓
8. Circuit Breaker Recording (doOnSuccess/doOnError)
   ├─ recordSuccess() on success
   └─ recordFailure() on error
   ↓
9. Dead Letter Queue (doOnError)
   (Only on error)
   ↓
10. Error Handling (onErrorResume)
    └─ Return failed result instead of propagating error
```

## Handler Details

### 1. RateLimiterHandler
**Method Used:** `tryAcquire()`
- Supports multiple algorithms: TOKEN_BUCKET, LEAKY_BUCKET, FIXED_WINDOW, SLIDING_WINDOW
- Prevents sending messages when rate limit is exceeded
- Returns `false` if rate limit exceeded, `true` if allowed
- Checked FIRST before any other operations

### 2. CircuitBreakerHandler
**Methods Used:** `allowRequest()`, `recordSuccess()`, `recordFailure()`
- Monitors failure rate and prevents cascading failures
- States: CLOSED (normal) → OPEN (failure rate high) → HALF_OPEN (testing recovery)
- Checked SECOND (after rate limit)
- Success/failure recorded AFTER send attempt
- Returns `false` if circuit is OPEN, `true` if request allowed

### 3. DeduplicationHandler
**Method Used:** `isDuplicate()`
- Prevents duplicate message processing
- Generates deduplication key based on: MESSAGE_ID, CONTENT_HASH, or CUSTOM strategy
- Checked THIRD (after rate limit and circuit breaker)
- Returns `true` if duplicate (should reject), `false` if new message

### 4. CompressionHandler
**Method Used:** `compress(byte[])`
- Compresses message payload if `compress` flag is true
- Supports: GZIP, SNAPPY, LZ4, ZSTD
- Applied FOURTH (after deduplication)
- Only applied if `compress` boolean is true AND handler exists
- Skips if payload size below minimum threshold

### 5. EncryptionHandler
**Method Used:** `encrypt(byte[])`
- Encrypts compressed payload if `encrypt` flag is true
- Supports: AES_256_GCM, AES_128_GCM, AES_256_CBC, AES_128_CBC
- Applied FIFTH (after compression)
- Only applied if `encrypt` boolean is true AND handler exists
- Uses authenticated encryption by default (AES-GCM)

### 6. RetryExecutor
**Methods Used:** `execute()` (sync), `executeAsync()` (async)
- Wraps send operation with automatic retry logic
- Supports strategies: FIXED, LINEAR, EXPONENTIAL, FIBONACCI
- Configurable max attempts, initial/max backoff, jitter
- Evaluates retryable exceptions
- Applied SIXTH (during actual send)

### 7. DeadLetterQueueHandler
**Method Used:** `handleFailedMessage(Message<?>, Throwable)`
- Routes failed messages to Dead Letter Queue
- Tracks failure count per message
- Stores error information, stack trace in message headers
- Invokes configured callback when message sent to DLQ
- Applied EIGHTH (only on failure, LAST operation)

## Backward Compatibility

All handlers are **optional** and null-safe:

```java
// If handler is null, it's simply skipped
if (rateLimiterHandler != null && !rateLimiterHandler.tryAcquire()) {
    // handle rate limit
}
```

This means:
- Existing code without handlers continues to work unchanged
- Handlers can be selectively enabled via MessageClientConfig
- Can be gradually adopted in existing systems
- Zero performance impact if handler is not configured

## Configuration Example

```java
// Create configuration with desired handlers
MessageClientConfig config = MessageClientConfig.builder()
    .providerConfig(kafkaConfig)
    // Optional: Enable rate limiting
    .rateLimit(RateLimit.builder()
        .enabled(true)
        .maxRequests(100)
        .window(Duration.ofSeconds(1))
        .algorithm(RateLimit.Algorithm.TOKEN_BUCKET)
        .build())
    // Optional: Enable circuit breaker
    .circuitBreaker(CircuitBreaker.builder()
        .enabled(true)
        .failureRateThreshold(0.5)
        .minimumNumberOfCalls(10)
        .build())
    // Optional: Enable compression
    .compression(Compression.builder()
        .enabled(true)
        .algorithm(Compression.Algorithm.GZIP)
        .build())
    // Optional: Enable encryption
    .encryption(Encryption.builder()
        .enabled(true)
        .algorithm(Encryption.Algorithm.AES_256_GCM)
        .build())
    // Optional: Enable deduplication
    .deduplication(Deduplication.builder()
        .enabled(true)
        .strategy(Deduplication.Strategy.MESSAGE_ID)
        .window(Duration.ofMinutes(5))
        .build())
    // Optional: Enable retry
    .retryPolicy(RetryPolicy.builder()
        .maxAttempts(3)
        .strategy(RetryPolicy.Strategy.EXPONENTIAL)
        .initialBackoff(Duration.ofMillis(100))
        .build())
    // Optional: Enable dead letter queue
    .deadLetterQueue(DeadLetterQueue.builder()
        .enabled(true)
        .topicPattern("%s-dlq")
        .maxRetries(3)
        .build())
    .build();

// Create client (handlers are auto-initialized)
MessageClient client = new MessageClientImpl(config);

// Use fluent API as before - handlers are automatically applied
SendResult result = client.send("my-topic", payload)
    .priority(Priority.HIGH)
    .compress(true)
    .encrypt(true)
    .execute();  // All 7 handlers applied automatically!
```

## Usage Example

```java
// Send with all reliability features
try {
    SendResult result = messageClient.send("order-topic", orderPayload)
        .partitionKey(order.getId())
        .header("timestamp", System.currentTimeMillis())
        .compress(true)        // Enable compression
        .encrypt(true)         // Enable encryption
        .priority(Priority.HIGH)
        .deliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .execute();            // Automatically applies all handlers!

    if (result.isSuccess()) {
        log.info("Message sent: {}", result.getMessageId());
    } else {
        log.error("Send failed: {}", result.getError());
    }
} catch (Exception e) {
    log.error("Send operation failed", e);
}
```

## Async Example

```java
// Send asynchronously with all reliability features
messageClient.send("order-topic", orderPayload)
    .compress(true)
    .encrypt(true)
    .executeAsync()
    .subscribe(
        result -> {
            if (result.isSuccess()) {
                log.info("Async message sent: {}", result.getMessageId());
            } else {
                log.error("Async send failed: {}", result.getError());
            }
        },
        error -> log.error("Async operation failed", error)
    );
```

## Fluent API Methods (Unchanged)

All existing fluent methods continue to work:

```java
.priority(Priority.HIGH)                    // Set priority
.priority(payload -> calculatePriority())    // Dynamic priority
.partitionKey(key)                          // Set partition key
.header(key, value)                         // Add header
.headers(map)                               // Add multiple headers
.correlationId(id)                          // Set correlation ID
.replyTo(topic)                             // Set reply-to topic
.ttl(duration)                              // Set time-to-live
.version(1)                                 // Set message version
.compress(true)                             // Enable compression (triggers handler)
.encrypt(true)                              // Enable encryption (triggers handler)
.deliveryGuarantee(guarantee)               // Set delivery guarantee
.execute()                                  // Send synchronously
.executeAsync()                             // Send asynchronously
```

## Error Handling

### Synchronous Execution
- Returns `SendResult` with `success=false` and error message
- No exceptions thrown for expected failures (rate limit, circuit open, etc.)
- Exceptions from handler code are caught and logged
- Failures are recorded in CircuitBreaker
- Failed messages are sent to DeadLetterQueue

### Asynchronous Execution
- Returns `Mono<SendResult>` that never fails
- Errors are converted to failed `SendResult` objects
- `doOnError` handler logs errors and records failures
- `onErrorResume` converts errors to failed results
- Same DLQ handling as synchronous mode

## Performance Considerations

1. **Rate Limiting**: Atomic operations, minimal overhead
2. **Circuit Breaker**: Atomic counters, O(1) complexity
3. **Deduplication**: ConcurrentHashMap lookup, O(1) average case
4. **Compression**: CPU-intensive, significant latency added
5. **Encryption**: CPU-intensive, uses standard JDK crypto
6. **Retry Logic**: May increase total latency due to backoff
7. **DLQ Handler**: Async to provider, minimal blocking

## Testing the Integration

```java
// Test rate limiting
@Test
public void testRateLimiting() {
    // Should succeed for first 100 calls
    // Should fail for call 101+
}

// Test circuit breaker
@Test
public void testCircuitBreaker() {
    // Inject failures to trigger circuit open
    // Verify requests are rejected
    // Verify circuit transitions to half-open after timeout
}

// Test deduplication
@Test
public void testDeduplication() {
    // Send same message twice
    // Second send should be rejected as duplicate
}

// Test compression
@Test
public void testCompression() {
    // Send with compress=true
    // Verify compressed header is set
}

// Test encryption
@Test
public void testEncryption() {
    // Send with encrypt=true
    // Verify encrypted header is set
}

// Test retry
@Test
public void testRetry() {
    // Inject transient failures
    // Verify message is retried automatically
}

// Test DLQ
@Test
public void testDeadLetterQueue() {
    // Send message that fails after max retries
    // Verify message is sent to DLQ topic
}
```

## Summary of Changes

| Component | Before | After |
|-----------|--------|-------|
| **Rate Limiting** | Not applied | Applied in Step 1 |
| **Circuit Breaker** | Not checked | Checked in Step 2, recorded in Step 7 |
| **Deduplication** | Not checked | Checked in Step 3 |
| **Compression** | Not integrated | Applied in Step 4 |
| **Encryption** | Not integrated | Applied in Step 5 |
| **Retry** | Not implemented | Applied in Step 6 |
| **DLQ** | Not implemented | Applied in Step 8 |
| **Backward Compat** | N/A | Full backward compatibility maintained |

All handlers are completely optional and zero-impact if not configured.
