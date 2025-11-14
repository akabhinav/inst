package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Financial tracking configuration for monitoring costs and revenue.
 *
 * Enables tracking of operational costs, revenue generation, and financial
 * metrics related to messaging operations and resource usage.
 *
 * Feature 70: Financial Tracking and Cost Analysis
 *
 * @author Messaging SDK
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTracking {

    /**
     * Flag to enable or disable financial tracking.
     */
    private boolean enabled;

    /**
     * Cost tracking configuration.
     */
    private CostTracking costTracking;

    /**
     * Revenue tracking configuration.
     */
    private RevenueTracking revenueTracking;

    /**
     * Cost tracking configuration for operational expense monitoring.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostTracking {

        /**
         * Flag to enable cost tracking.
         */
        private boolean enabled;

        /**
         * Flag to track infrastructure costs.
         */
        @Builder.Default
        private boolean trackInfrastructureCosts = true;

        /**
         * Flag to track licensing costs.
         */
        @Builder.Default
        private boolean trackLicensingCosts = true;

        /**
         * Flag to track storage costs.
         */
        @Builder.Default
        private boolean trackStorageCosts = true;

        /**
         * Flag to track network transfer costs.
         */
        @Builder.Default
        private boolean trackNetworkCosts = true;

        /**
         * Flag to track compute/processing costs.
         */
        @Builder.Default
        private boolean trackComputeCosts = true;

        /**
         * Currency code for cost tracking (e.g., USD, EUR).
         */
        @Builder.Default
        private String currency = "USD";

        /**
         * Cost allocation method.
         */
        @Builder.Default
        private CostAllocationMethod allocationMethod = CostAllocationMethod.BY_TOPIC;

        /**
         * Flag to enable detailed cost breakdown by resource.
         */
        @Builder.Default
        private boolean detailedBreakdown = true;

        /**
         * Cost allocation method enumeration.
         */
        public enum CostAllocationMethod {
            /**
             * Allocate costs by topic.
             */
            BY_TOPIC,

            /**
             * Allocate costs by application.
             */
            BY_APPLICATION,

            /**
             * Allocate costs by department/team.
             */
            BY_DEPARTMENT,

            /**
             * Allocate costs by resource type.
             */
            BY_RESOURCE_TYPE
        }
    }

    /**
     * Revenue tracking configuration for revenue generation monitoring.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTracking {

        /**
         * Flag to enable revenue tracking.
         */
        private boolean enabled;

        /**
         * Flag to track transaction-based revenue.
         */
        @Builder.Default
        private boolean trackTransactionRevenue = true;

        /**
         * Flag to track subscription-based revenue.
         */
        @Builder.Default
        private boolean trackSubscriptionRevenue = true;

        /**
         * Flag to track usage-based revenue.
         */
        @Builder.Default
        private boolean trackUsageRevenue = true;

        /**
         * Currency code for revenue tracking (e.g., USD, EUR).
         */
        @Builder.Default
        private String currency = "USD";

        /**
         * Revenue model being used.
         */
        @Builder.Default
        private RevenueModel model = RevenueModel.HYBRID;

        /**
         * Billing period in days.
         */
        @Builder.Default
        private int billingPeriodDays = 30;

        /**
         * Flag to enable revenue forecasting.
         */
        @Builder.Default
        private boolean enableForecasting = true;

        /**
         * Revenue model enumeration.
         */
        public enum RevenueModel {
            /**
             * Subscription-based revenue model.
             */
            SUBSCRIPTION,

            /**
             * Pay-per-use revenue model.
             */
            PAY_PER_USE,

            /**
             * Freemium revenue model.
             */
            FREEMIUM,

            /**
             * Hybrid revenue model combining multiple approaches.
             */
            HYBRID
        }
    }
}
