package com.messaging.provider;

import lombok.Builder;
import lombok.Data;

/**
 * Provider-specific send result
 */
@Data
@Builder
public class ProviderSendResult {
    private String messageId;
    private String topic;
    private Integer partition;
    private Long offset;
    private boolean success;
    private String error;
}
