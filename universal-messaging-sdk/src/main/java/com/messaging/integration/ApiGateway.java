package com.messaging.integration;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * API Gateway integration configuration
 * Feature 64: API Gateway Integration
 *
 * Expose messaging via REST/GraphQL
 */
@Data
@Builder
public class ApiGateway {

    /**
     * Enable API gateway
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Port to listen on
     */
    @Builder.Default
    private int port = 8080;

    /**
     * Authentication mechanism
     */
    @Builder.Default
    private Authentication authentication = Authentication.JWT;

    /**
     * Enable REST API
     */
    @Builder.Default
    private boolean enableRest = true;

    /**
     * Enable GraphQL API
     */
    @Builder.Default
    private boolean enableGraphQL = true;

    /**
     * Enable WebSocket API
     */
    @Builder.Default
    private boolean enableWebSocket = true;

    /**
     * Enable gRPC API
     */
    @Builder.Default
    private boolean enableGrpc = false;

    /**
     * Rate limit (requests per duration)
     */
    @Builder.Default
    private int rateLimit = 1000;

    /**
     * Rate limit duration
     */
    @Builder.Default
    private Duration rateLimitDuration = Duration.ofSeconds(1);

    /**
     * Enable API documentation (Swagger/OpenAPI)
     */
    @Builder.Default
    private boolean enableDocs = true;

    /**
     * Enable CORS
     */
    @Builder.Default
    private boolean enableCors = true;

    /**
     * Authentication mechanisms
     */
    public enum Authentication {
        NONE,
        JWT,
        OAUTH2,
        API_KEY,
        BASIC_AUTH
    }

    public static ApiGateway createDefault() {
        return ApiGateway.builder().build();
    }
}
