package com.messaging.observability;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for restore operations in disaster recovery scenarios.
 *
 * Provides fluent API for configuring and executing data restore operations
 * with point-in-time recovery, verification, and destination management.
 *
 * Feature 62: Disaster Recovery
 *
 * @author Messaging SDK
 * @version 1.0
 */
public interface RestoreOperation {

    /**
     * Set the backup source to restore from.
     *
     * @param backupId The identifier of the backup to restore from
     * @return This RestoreOperation for method chaining
     */
    RestoreOperation from(String backupId);

    /**
     * Set the point-in-time to restore to.
     *
     * Allows recovery to a specific point in time within the backup window.
     *
     * @param timestamp The point-in-time instant to restore to
     * @return This RestoreOperation for method chaining
     */
    RestoreOperation pointInTime(Instant timestamp);

    /**
     * Set the destination for restored data.
     *
     * Can be the original location or a separate location for testing.
     *
     * @param destination The target location for restored data
     * @return This RestoreOperation for method chaining
     */
    RestoreOperation destination(String destination);

    /**
     * Enable or disable verification of restored data integrity.
     *
     * When enabled, the system will verify data consistency and completeness
     * after restoration.
     *
     * @param verify Flag to enable verification
     * @return This RestoreOperation for method chaining
     */
    RestoreOperation verify(boolean verify);

    /**
     * Execute the restore operation asynchronously.
     *
     * Performs the actual data restoration with configured settings.
     * Returns a CompletableFuture that completes when restoration is finished.
     *
     * @return CompletableFuture that completes with restoration result
     * @throws IllegalStateException if required parameters are not configured
     */
    CompletableFuture<RestoreResult> execute();

    /**
     * Result information from a restore operation.
     */
    interface RestoreResult {

        /**
         * Get the restoration status.
         *
         * @return The status of the restore operation
         */
        RestoreStatus getStatus();

        /**
         * Get the number of restored records.
         *
         * @return Count of successfully restored records
         */
        long getRestoredRecordCount();

        /**
         * Get the number of failed records during restoration.
         *
         * @return Count of records that failed to restore
         */
        long getFailedRecordCount();

        /**
         * Get the restoration start time.
         *
         * @return Instant when restoration started
         */
        Instant getStartTime();

        /**
         * Get the restoration end time.
         *
         * @return Instant when restoration completed
         */
        Instant getEndTime();

        /**
         * Get error message if restoration failed.
         *
         * @return Error message or empty string if successful
         */
        String getErrorMessage();
    }

    /**
     * Enum for restore operation status.
     */
    enum RestoreStatus {
        /**
         * Restore operation is in progress.
         */
        IN_PROGRESS,

        /**
         * Restore operation completed successfully.
         */
        SUCCESS,

        /**
         * Restore operation completed with errors.
         */
        PARTIAL_SUCCESS,

        /**
         * Restore operation failed.
         */
        FAILED,

        /**
         * Restore operation was cancelled.
         */
        CANCELLED
    }
}
