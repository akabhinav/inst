package com.messaging.intelligence;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Machine learning integration configuration
 * Feature 68: Machine Learning Integration
 */
@Data
@Builder
public class MachineLearning {

    /**
     * Enable machine learning
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * ML features to enable
     */
    @Singular
    private List<MLFeature> features;

    /**
     * ML Feature types
     */
    @Data
    @Builder
    public static class MLFeature {
        private FeatureType type;
        private Object model;
        private Consumer<Object> onEvent;

        public enum FeatureType {
            ANOMALY_DETECTION,
            MESSAGE_CATEGORIZATION,
            PRIORITY_PREDICTION,
            LOAD_FORECASTING,
            SENTIMENT_ANALYSIS,
            FRAUD_DETECTION
        }

        public static MLFeature anomalyDetection() {
            return MLFeature.builder()
                    .type(FeatureType.ANOMALY_DETECTION)
                    .build();
        }

        public static MLFeature messageCategorization() {
            return MLFeature.builder()
                    .type(FeatureType.MESSAGE_CATEGORIZATION)
                    .build();
        }

        public static MLFeature priorityPrediction() {
            return MLFeature.builder()
                    .type(FeatureType.PRIORITY_PREDICTION)
                    .build();
        }

        public static MLFeature loadForecasting() {
            return MLFeature.builder()
                    .type(FeatureType.LOAD_FORECASTING)
                    .build();
        }
    }

    /**
     * ML Models
     */
    public enum AnomalyModel {
        ISOLATION_FOREST,
        AUTOENCODER,
        STATISTICAL
    }

    public enum ClassificationModel {
        BERT,
        NAIVE_BAYES,
        RANDOM_FOREST
    }

    public enum RegressionModel {
        XGBOOST,
        LINEAR_REGRESSION,
        NEURAL_NETWORK
    }

    public enum TimeSeriesModel {
        PROPHET,
        ARIMA,
        LSTM
    }

    public static MachineLearning createDefault() {
        return MachineLearning.builder()
                .enabled(false)
                .build();
    }
}
