package com.grocerylist.backend.config;

import com.grocerylist.backend.repository.GroceryItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Gives completed items from before the completed_at column existed their current purchase time,
 * so deleting or clearing them later does not move their date in the analytics.
 * Idempotent: only touches completed rows where completed_at is still null.
 */
@Component
public class CompletedAtBackfill implements ApplicationRunner {

    private final GroceryItemRepository itemRepository;

    public CompletedAtBackfill(GroceryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = itemRepository.backfillCompletedAt();
        if (updated > 0) {
            System.out.println("INFO: Backfilled completed_at for " + updated + " items");
        }
    }
}
