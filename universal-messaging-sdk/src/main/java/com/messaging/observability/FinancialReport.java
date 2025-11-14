package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Financial report containing cost and revenue analysis.
 *
 * Provides comprehensive financial insights including costs, revenue, ROI,
 * and detailed breakdowns by topic, resource type, and time period.
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
public class FinancialReport {

    /**
     * Report generation timestamp.
     */
    private Instant generatedAt;

    /**
     * Report period start.
     */
    private Instant periodStart;

    /**
     * Report period end.
     */
    private Instant periodEnd;

    /**
     * Currency code for all monetary values.
     */
    private String currency;

    /**
     * Total operational costs for the period.
     */
    private BigDecimal totalCost;

    /**
     * Total revenue for the period.
     */
    private BigDecimal revenue;

    /**
     * Return on investment (ROI) percentage.
     */
    private BigDecimal roi;

    /**
     * Net profit (revenue - costs).
     */
    private BigDecimal netProfit;

    /**
     * Profit margin percentage.
     */
    private BigDecimal profitMargin;

    /**
     * Cost breakdown by topic.
     */
    private Map<String, BigDecimal> costByTopic;

    /**
     * Revenue breakdown by topic.
     */
    private Map<String, BigDecimal> revenueByTopic;

    /**
     * Cost breakdown by resource type.
     */
    private Map<String, BigDecimal> costByResourceType;

    /**
     * Cost breakdown by component/service.
     */
    private Map<String, BigDecimal> costByComponent;

    /**
     * Monthly cost breakdown.
     */
    private Map<YearMonth, BigDecimal> monthlyCosts;

    /**
     * Monthly revenue breakdown.
     */
    private Map<YearMonth, BigDecimal> monthlyRevenue;

    /**
     * Detailed cost analysis by category.
     */
    private List<CostDetail> costDetails;

    /**
     * Detailed revenue analysis by category.
     */
    private List<RevenueDetail> revenueDetails;

    /**
     * Cost efficiency metrics.
     */
    private CostEfficiency costEfficiency;

    /**
     * Financial trends and projections.
     */
    private FinancialTrends trends;

    /**
     * Detailed cost information for a specific category.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostDetail {

        /**
         * Cost category or component name.
         */
        private String category;

        /**
         * Cost amount.
         */
        private BigDecimal amount;

        /**
         * Percentage of total cost.
         */
        private BigDecimal percentage;

        /**
         * Cost trend (UP, DOWN, STABLE).
         */
        private Trend trend;

        /**
         * Month-over-month change percentage.
         */
        private BigDecimal changePercentage;

        /**
         * Detailed breakdown of this cost category.
         */
        private Map<String, BigDecimal> breakdown;

        /**
         * Cost trend enumeration.
         */
        public enum Trend {
            /**
             * Cost is increasing.
             */
            UP,

            /**
             * Cost is decreasing.
             */
            DOWN,

            /**
             * Cost is stable.
             */
            STABLE
        }
    }

    /**
     * Detailed revenue information for a specific category.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDetail {

        /**
         * Revenue category or source name.
         */
        private String category;

        /**
         * Revenue amount.
         */
        private BigDecimal amount;

        /**
         * Percentage of total revenue.
         */
        private BigDecimal percentage;

        /**
         * Revenue trend (UP, DOWN, STABLE).
         */
        private RevenueTrend trend;

        /**
         * Month-over-month change percentage.
         */
        private BigDecimal changePercentage;

        /**
         * Number of transactions or customers.
         */
        private long count;

        /**
         * Average revenue per transaction/customer.
         */
        private BigDecimal averageRevenue;

        /**
         * Revenue trend enumeration.
         */
        public enum RevenueTrend {
            /**
             * Revenue is increasing.
             */
            UP,

            /**
             * Revenue is decreasing.
             */
            DOWN,

            /**
             * Revenue is stable.
             */
            STABLE
        }
    }

    /**
     * Cost efficiency metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostEfficiency {

        /**
         * Cost per message processed.
         */
        private BigDecimal costPerMessage;

        /**
         * Cost per transaction.
         */
        private BigDecimal costPerTransaction;

        /**
         * Cost per user.
         */
        private BigDecimal costPerUser;

        /**
         * Infrastructure cost as percentage of total cost.
         */
        private BigDecimal infrastructureCostPercentage;

        /**
         * Storage cost as percentage of total cost.
         */
        private BigDecimal storageCostPercentage;

        /**
         * Network cost as percentage of total cost.
         */
        private BigDecimal networkCostPercentage;

        /**
         * Total messages processed in the period.
         */
        private long totalMessagesProcessed;

        /**
         * Total transactions in the period.
         */
        private long totalTransactions;

        /**
         * Total active users in the period.
         */
        private long totalActiveUsers;

        /**
         * Efficiency score (0-100).
         */
        private int efficiencyScore;
    }

    /**
     * Financial trends and projections.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialTrends {

        /**
         * Average monthly cost trend percentage.
         */
        private BigDecimal avgMonthlyCostTrend;

        /**
         * Average monthly revenue trend percentage.
         */
        private BigDecimal avgMonthlyRevenueTrend;

        /**
         * Projected annual cost based on current trends.
         */
        private BigDecimal projectedAnnualCost;

        /**
         * Projected annual revenue based on current trends.
         */
        private BigDecimal projectedAnnualRevenue;

        /**
         * Break-even analysis (months to break-even).
         */
        private int monthsToBreakEven;

        /**
         * Fastest growing cost category.
         */
        private String fastestGrowingCost;

        /**
         * Fastest growing revenue source.
         */
        private String fastestGrowingRevenue;

        /**
         * Cost growth rate percentage.
         */
        private BigDecimal costGrowthRate;

        /**
         * Revenue growth rate percentage.
         */
        private BigDecimal revenueGrowthRate;

        /**
         * Estimated payback period in months.
         */
        private int paybackPeriodMonths;

        /**
         * Sustainability assessment (SUSTAINABLE, AT_RISK, CRITICAL).
         */
        private Sustainability sustainability;

        /**
         * Sustainability assessment enumeration.
         */
        public enum Sustainability {
            /**
             * Financial model is sustainable.
             */
            SUSTAINABLE,

            /**
             * Financial model is at risk.
             */
            AT_RISK,

            /**
             * Financial model is in critical condition.
             */
            CRITICAL
        }
    }
}
