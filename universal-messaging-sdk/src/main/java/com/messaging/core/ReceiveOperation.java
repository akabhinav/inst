package com.messaging.core;

import com.messaging.pattern.MessageFilter;
import com.messaging.performance.AdaptiveBatching;
import com.messaging.performance.ParallelProcessing;
import com.messaging.performance.Prefetch;
import com.messaging.performance.PriorityHandling;
import com.messaging.reliability.VersionHandling;

import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent API for receiving messages with various configurations
 *
 * @param <T> The type of the message payload
 */
public interface ReceiveOperation<T> {

    /**
     * Set message handler (callback for each message)
     */
    ReceiveOperation<T> onMessage(Consumer<Message<T>> handler);

    /**
     * Set batch handler (callback for batch of messages)
     */
    ReceiveOperation<T> onBatch(Consumer<List<Message<T>>> handler);

    /**
     * Set error handler
     */
    ReceiveOperation<T> onError(Consumer<Throwable> errorHandler);

    /**
     * Filter messages
     */
    ReceiveOperation<T> filter(MessageFilter<T> filter);

    /**
     * Set consumer group (for load balancing)
     */
    ReceiveOperation<T> consumerGroup(String group);

    /**
     * Set number of concurrent consumers
     */
    ReceiveOperation<T> concurrency(int concurrency);

    /**
     * Enable adaptive batching
     */
    ReceiveOperation<T> adaptiveBatching(AdaptiveBatching batching);

    /**
     * Enable priority handling
     */
    ReceiveOperation<T> priorityHandling(PriorityHandling priorityHandling);

    /**
     * Set priority lane (route to specific queue based on priority)
     */
    ReceiveOperation<T> priorityLane(Message.Priority priority, String topic);

    /**
     * Enable parallel processing with ordering guarantee
     */
    ReceiveOperation<T> parallelism(ParallelProcessing parallel);

    /**
     * Enable message prefetching
     */
    ReceiveOperation<T> prefetch(Prefetch prefetch);

    /**
     * Enable version handling for schema evolution
     */
    ReceiveOperation<T> versionHandling(VersionHandling versionHandling);

    /**
     * Enable ML-based priority prediction
     */
    ReceiveOperation<T> mlPriority(boolean enabled);

    /**
     * Auto-acknowledge messages after successful processing
     */
    ReceiveOperation<T> autoAck(boolean autoAck);

    /**
     * Set batch size for batch processing
     */
    ReceiveOperation<T> batchSize(int size);

    /**
     * Start consuming messages
     */
    void start();

    /**
     * Stop consuming messages
     */
    void stop();
}
