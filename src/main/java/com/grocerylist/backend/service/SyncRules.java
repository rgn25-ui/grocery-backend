package com.grocerylist.backend.service;

import com.grocerylist.backend.model.GroceryItemEntity;

/**
 * Pure rules for applying client writes. Kept free of Spring and JPA so they can be unit tested.
 */
public final class SyncRules {

    private SyncRules() {
    }

    /**
     * True when the stored row is newer than the incoming one, so the incoming write must be ignored.
     * Equal timestamps are accepted, which makes retries of the same change harmless.
     */
    public static boolean isStale(Long incomingUpdatedAt, Long storedUpdatedAt) {
        return incomingUpdatedAt != null && storedUpdatedAt != null && storedUpdatedAt > incomingUpdatedAt;
    }

    /**
     * The purchase time to store for an incoming item write.
     * - Not completed: null.
     * - Already completed before: keep the original time, so later edits or deletes don't move it.
     *   Rows completed before completed_at existed fall back to their stored updatedAt,
     *   which is what the analytics used as purchase time until now.
     * - Newly completed: the client's updatedAt for this change.
     */
    public static Long resolveCompletedAt(GroceryItemEntity incoming, GroceryItemEntity stored) {
        if (!Boolean.TRUE.equals(incoming.getIsCompleted())) {
            return null;
        }
        if (stored != null && Boolean.TRUE.equals(stored.getIsCompleted())) {
            return stored.getCompletedAt() != null ? stored.getCompletedAt() : stored.getUpdatedAt();
        }
        return incoming.getUpdatedAt() != null ? incoming.getUpdatedAt() : System.currentTimeMillis();
    }

    /** Purchase time for the analytics: completedAt, or updatedAt for rows that predate completedAt. */
    public static long purchaseTime(GroceryItemEntity item) {
        if (item.getCompletedAt() != null) {
            return item.getCompletedAt();
        }
        return item.getUpdatedAt() != null ? item.getUpdatedAt() : 0L;
    }
}
