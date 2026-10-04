package com.grocerylist.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grocerylist.backend.model.GroceryItemEntity;
import org.junit.jupiter.api.Test;

class SyncRulesTest {

    private static GroceryItemEntity item(boolean completed, Long updatedAt, Long completedAt) {
        GroceryItemEntity item = new GroceryItemEntity("list-1", "mælk");
        item.setIsCompleted(completed);
        item.setUpdatedAt(updatedAt);
        item.setCompletedAt(completedAt);
        return item;
    }

    // ===== isStale =====

    @Test
    void olderIncomingWrite_isStale() {
        assertTrue(SyncRules.isStale(100L, 200L));
    }

    @Test
    void newerOrEqualIncomingWrite_isAccepted() {
        assertFalse(SyncRules.isStale(200L, 100L));
        assertFalse(SyncRules.isStale(200L, 200L)); // a retry of the same change
    }

    @Test
    void missingTimestamps_areAccepted() {
        assertFalse(SyncRules.isStale(null, 200L));
        assertFalse(SyncRules.isStale(100L, null));
    }

    // ===== resolveCompletedAt =====

    @Test
    void notCompleted_hasNoCompletedAt() {
        assertNull(SyncRules.resolveCompletedAt(item(false, 500L, null), item(true, 300L, 300L)));
    }

    @Test
    void newItemCompleted_usesItsUpdatedAt() {
        assertEquals(500L, SyncRules.resolveCompletedAt(item(true, 500L, null), null));
    }

    @Test
    void checkedOffNow_usesIncomingUpdatedAt() {
        assertEquals(500L, SyncRules.resolveCompletedAt(item(true, 500L, null), item(false, 300L, null)));
    }

    @Test
    void laterEditOfCompletedItem_keepsOriginalPurchaseTime() {
        // e.g. soft-deleted or cleared later: the purchase time must not move
        assertEquals(300L, SyncRules.resolveCompletedAt(item(true, 900L, null), item(true, 300L, 300L)));
    }

    @Test
    void legacyCompletedRow_fallsBackToStoredUpdatedAt() {
        assertEquals(400L, SyncRules.resolveCompletedAt(item(true, 900L, null), item(true, 400L, null)));
    }

    @Test
    void uncheckedAndCheckedAgain_getsNewPurchaseTime() {
        GroceryItemEntity unchecked = item(false, 600L, null);
        assertEquals(700L, SyncRules.resolveCompletedAt(item(true, 700L, null), unchecked));
    }

    // ===== purchaseTime =====

    @Test
    void purchaseTime_prefersCompletedAt() {
        assertEquals(300L, SyncRules.purchaseTime(item(true, 900L, 300L)));
        assertEquals(900L, SyncRules.purchaseTime(item(true, 900L, null)));
        assertEquals(0L, SyncRules.purchaseTime(item(true, null, null)));
    }
}
