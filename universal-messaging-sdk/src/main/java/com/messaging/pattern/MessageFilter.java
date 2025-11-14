package com.messaging.pattern;

import com.messaging.core.Message;

import java.util.function.Predicate;

/**
 * Message filter for filtering messages
 * Feature 19: Message Filter
 */
@FunctionalInterface
public interface MessageFilter<T> extends Predicate<Message<T>> {

    /**
     * Filter by header value
     */
    static <T> MessageFilter<T> byHeader(String key, String value) {
        return msg -> value.equals(msg.getHeaders().get(key));
    }

    /**
     * Filter by priority
     */
    static <T> MessageFilter<T> byPriority(Message.Priority priority) {
        return msg -> msg.getPriority() == priority;
    }

    /**
     * Filter by topic pattern
     */
    static <T> MessageFilter<T> byTopicPattern(String pattern) {
        return msg -> msg.getTopic().matches(pattern);
    }

    /**
     * Combine filters with AND
     */
    default MessageFilter<T> and(MessageFilter<T> other) {
        return msg -> test(msg) && other.test(msg);
    }

    /**
     * Combine filters with OR
     */
    default MessageFilter<T> or(MessageFilter<T> other) {
        return msg -> test(msg) || other.test(msg);
    }

    /**
     * Negate filter
     */
    default MessageFilter<T> negate() {
        return msg -> !test(msg);
    }
}
