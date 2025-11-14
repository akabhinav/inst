package com.messaging.spring;

import com.messaging.core.MessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Auto-Configuration for Universal Messaging SDK
 * Feature 41: Spring Boot Auto-Configuration
 *
 * Automatically configures MessageClient when SDK is on classpath
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
@Slf4j
public class MessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageClient messageClient(MessagingProperties properties) {
        log.info("Auto-configuring Universal Messaging SDK");
        log.info("Provider: {}", properties.getProvider());
        log.info("Connection: {}", properties.getConnectionString());

        var builder = MessageClient.builder()
                .provider(properties.getProvider())
                .connectionString(properties.getConnectionString());

        // Configure retry if enabled
        if (properties.getRetry().isEnabled()) {
            builder.retry(properties.getRetry().toRetryPolicy());
        }

        // Configure DLQ if enabled
        if (properties.getDlq().isEnabled()) {
            builder.deadLetterQueue(properties.getDlq().toDeadLetterQueue());
        }

        // Configure compression if enabled
        if (properties.getCompression().isEnabled()) {
            builder.compression(properties.getCompression().toCompression());
        }

        // Configure encryption if enabled
        if (properties.getEncryption().isEnabled()) {
            builder.encryption(properties.getEncryption().toEncryption());
        }

        log.info("Universal Messaging SDK configured successfully");

        return builder.build();
    }
}
