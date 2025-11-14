package com.messaging.observability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.util.List;

/**
 * Disaster recovery configuration for system resilience and data protection.
 *
 * This class provides comprehensive disaster recovery capabilities including
 * automated backups and data replication across multiple locations.
 *
 * Feature 62: Disaster Recovery
 *
 * @author Messaging SDK
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisasterRecovery {

    /**
     * Flag to enable or disable disaster recovery mechanisms.
     */
    private boolean enabled;

    /**
     * Backup configuration for data protection.
     */
    private Backup backup;

    /**
     * Replication configuration for high availability.
     */
    private Replication replication;

    /**
     * Backup configuration for disaster recovery.
     * Manages backup strategy, schedule, and retention policies.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Backup {

        /**
         * Flag to enable automated backups.
         */
        private boolean enabled;

        /**
         * Backup strategy to use.
         */
        @Builder.Default
        private BackupStrategy strategy = BackupStrategy.INCREMENTAL;

        /**
         * Backup interval/frequency.
         */
        private Duration interval;

        /**
         * Retention period for backups.
         */
        private Duration retention;

        /**
         * Backup storage location(s).
         */
        private List<String> destinations;

        /**
         * Flag to enable encryption for backup data.
         */
        @Builder.Default
        private boolean encrypted = true;

        /**
         * Backup strategy options.
         */
        public enum BackupStrategy {
            /**
             * Full backup of all data.
             */
            FULL,

            /**
             * Incremental backup of changed data only.
             */
            INCREMENTAL,

            /**
             * Differential backup of data changed since last full backup.
             */
            DIFFERENTIAL
        }
    }

    /**
     * Replication configuration for data redundancy and high availability.
     * Supports replication across multiple zones and regions.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Replication {

        /**
         * Flag to enable data replication.
         */
        private boolean enabled;

        /**
         * Replication mode to use.
         */
        @Builder.Default
        private ReplicationMode mode = ReplicationMode.ASYNCHRONOUS;

        /**
         * Number of replica copies to maintain.
         */
        @Builder.Default
        private int replicaCount = 2;

        /**
         * Replica locations/regions.
         */
        private List<String> locations;

        /**
         * Maximum lag for replication in milliseconds.
         */
        private long maxReplicationLagMs;

        /**
         * Flag to enable cross-region replication.
         */
        private boolean crossRegionEnabled;

        /**
         * Replication mode options.
         */
        public enum ReplicationMode {
            /**
             * Synchronous replication - waits for replicas to acknowledge.
             */
            SYNCHRONOUS,

            /**
             * Asynchronous replication - does not wait for replica acknowledgment.
             */
            ASYNCHRONOUS,

            /**
             * Semi-synchronous replication - waits for at least one replica.
             */
            SEMI_SYNCHRONOUS
        }
    }
}
