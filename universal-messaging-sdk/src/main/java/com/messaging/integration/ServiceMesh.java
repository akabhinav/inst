package com.messaging.integration;

import lombok.Builder;
import lombok.Data;

/**
 * Service mesh integration configuration
 * Feature 63: Service Mesh Integration (Istio/Linkerd)
 */
@Data
@Builder
public class ServiceMesh {

    /**
     * Service mesh type
     */
    @Builder.Default
    private ServiceMeshType type = ServiceMeshType.ISTIO;

    /**
     * Enable mutual TLS
     */
    @Builder.Default
    private boolean enableMTLS = true;

    /**
     * Enable traffic management
     */
    @Builder.Default
    private boolean trafficManagement = true;

    /**
     * Enable circuit breaking
     */
    @Builder.Default
    private boolean circuitBreaking = true;

    /**
     * Enable automatic retries
     */
    @Builder.Default
    private boolean retries = true;

    /**
     * Enable timeouts
     */
    @Builder.Default
    private boolean timeouts = true;

    /**
     * Service mesh types
     */
    public enum ServiceMeshType {
        ISTIO,
        LINKERD,
        CONSUL,
        CUSTOM
    }

    public static ServiceMesh createDefault() {
        return ServiceMesh.builder().build();
    }
}
