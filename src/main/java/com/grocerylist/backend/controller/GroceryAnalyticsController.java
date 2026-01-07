package com.grocerylist.backend.controller;

import com.grocerylist.backend.dto.ItemPurchaseAnalytics;
import com.grocerylist.backend.dto.PurchaseDate;
import com.grocerylist.backend.service.GroceryAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class GroceryAnalyticsController {

    @Autowired
    private GroceryAnalyticsService analyticsService;

    /**
     * Get purchase count for a specific item over different time periods
     * @param userId The user ID
     * @param itemName The grocery item name
     * @return Analytics data with counts for week, month, quarter, year
     */
    @GetMapping("/item-frequency")
    public ResponseEntity<ItemPurchaseAnalytics> getItemFrequency(
            @RequestParam String userId,
            @RequestParam String itemName) {

        ItemPurchaseAnalytics analytics = analyticsService.getItemPurchaseFrequency(userId, itemName);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get all dates when a specific item was purchased
     * @param userId The user ID
     * @param itemName The grocery item name
     * @return List of purchase dates
     */
    @GetMapping("/purchase-dates")
    public ResponseEntity<List<PurchaseDate>> getPurchaseDates(
            @RequestParam String userId,
            @RequestParam String itemName) {

        List<PurchaseDate> dates = analyticsService.getPurchaseDates(userId, itemName);
        return ResponseEntity.ok(dates);
    }

    /**
     * Search for items that match a query (for autocomplete)
     * @param userId The user ID
     * @param query The search query
     * @return List of matching item names
     */
    @GetMapping("/search-items")
    public ResponseEntity<List<String>> searchItems(
            @RequestParam String userId,
            @RequestParam String query) {

        List<String> items = analyticsService.searchUserItems(userId, query);
        return ResponseEntity.ok(items);
    }
}