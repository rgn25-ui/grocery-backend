package com.grocerylist.backend.service;

import com.grocerylist.backend.dto.ItemPurchaseAnalytics;
import com.grocerylist.backend.dto.PurchaseDate;
import com.grocerylist.backend.model.GroceryItemEntity;
import com.grocerylist.backend.model.GroceryListEntity;
import com.grocerylist.backend.repository.GroceryItemRepository;
import com.grocerylist.backend.repository.GroceryListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroceryAnalyticsService {

    @Autowired
    private GroceryItemRepository itemRepository;

    @Autowired
    private GroceryListRepository listRepository;

    // Single item frequency
    public ItemPurchaseAnalytics getItemPurchaseFrequency(String userId, String itemName) {
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000);
        long oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000);
        long threeMonthsAgo = now - (90L * 24 * 60 * 60 * 1000);
        long oneYearAgo = now - (365L * 24 * 60 * 60 * 1000);

        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return new ItemPurchaseAnalytics(itemName, 0, 0, 0, 0, 0);
        }

        // Get all items (including deleted) for these lists
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        // Filter for completed items with matching name
        List<GroceryItemEntity> matchingItems = allItems.stream()
                .filter(item -> item.getName() != null &&
                        item.getName().equalsIgnoreCase(itemName) &&
                        item.getIsCompleted() != null &&
                        item.getIsCompleted())
                .collect(Collectors.toList());

        int lastWeek = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneWeekAgo)
                .count();

        int lastMonth = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneMonthAgo)
                .count();

        int lastQuarter = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > threeMonthsAgo)
                .count();

        int lastYear = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneYearAgo)
                .count();

        int totalAllTime = matchingItems.size();

        return new ItemPurchaseAnalytics(itemName, lastWeek, lastMonth, lastQuarter, lastYear, totalAllTime);
    }

    // NEW: Multiple items frequency (combined)
    public ItemPurchaseAnalytics getItemsPurchaseFrequency(String userId, List<String> itemNames) {
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000);
        long oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000);
        long threeMonthsAgo = now - (90L * 24 * 60 * 60 * 1000);
        long oneYearAgo = now - (365L * 24 * 60 * 60 * 1000);

        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return new ItemPurchaseAnalytics(String.join(", ", itemNames), 0, 0, 0, 0, 0);
        }

        // Get all items (including deleted) for these lists
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        // Filter for completed items matching ANY of the item names
        List<GroceryItemEntity> matchingItems = allItems.stream()
                .filter(item -> item.getName() != null &&
                        item.getIsCompleted() != null &&
                        item.getIsCompleted() &&
                        itemNames.stream().anyMatch(name -> name.equalsIgnoreCase(item.getName())))
                .collect(Collectors.toList());

        int lastWeek = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneWeekAgo)
                .count();

        int lastMonth = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneMonthAgo)
                .count();

        int lastQuarter = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > threeMonthsAgo)
                .count();

        int lastYear = (int) matchingItems.stream()
                .filter(item -> SyncRules.purchaseTime(item) > oneYearAgo)
                .count();

        int totalAllTime = matchingItems.size();

        // Use search query as display name with variant count
        String displayName = itemNames.size() == 1
                ? itemNames.get(0)
                : itemNames.get(0) + " (" + itemNames.size() + " varianter)";

        return new ItemPurchaseAnalytics(displayName, lastWeek, lastMonth, lastQuarter, lastYear, totalAllTime);
    }

    // Single item purchase dates
    public List<PurchaseDate> getPurchaseDates(String userId, String itemName) {
        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);

        // Create a map of listId -> listName for quick lookup
        Map<String, String> listNameMap = new HashMap<>();
        for (GroceryListEntity list : userLists) {
            listNameMap.put(list.getId(), list.getName());
        }

        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return List.of();
        }

        // Get all items for these lists
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        // Filter for completed items with matching name
        return allItems.stream()
                .filter(item -> item.getName() != null &&
                        item.getName().equalsIgnoreCase(itemName) &&
                        item.getIsCompleted() != null &&
                        item.getIsCompleted())
                .map(item -> createPurchaseDate(item, listNameMap))
                .sorted((a, b) -> b.getPurchaseDate().compareTo(a.getPurchaseDate())) // Most recent first
                .collect(Collectors.toList());
    }

    // NEW: Multiple items purchase dates (combined)
    public List<PurchaseDate> getItemsPurchaseDates(String userId, List<String> itemNames) {
        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);

        // Create a map of listId -> listName for quick lookup
        Map<String, String> listNameMap = new HashMap<>();
        for (GroceryListEntity list : userLists) {
            listNameMap.put(list.getId(), list.getName());
        }

        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return List.of();
        }

        // Get all items for these lists
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        // Filter for completed items matching ANY of the item names
        return allItems.stream()
                .filter(item -> item.getName() != null &&
                        item.getIsCompleted() != null &&
                        item.getIsCompleted() &&
                        itemNames.stream().anyMatch(name -> name.equalsIgnoreCase(item.getName())))
                .map(item -> createPurchaseDate(item, listNameMap))
                .sorted((a, b) -> b.getPurchaseDate().compareTo(a.getPurchaseDate())) // Most recent first
                .collect(Collectors.toList());
    }

    // Helper method to create PurchaseDate from GroceryItemEntity
    private PurchaseDate createPurchaseDate(GroceryItemEntity item, Map<String, String> listNameMap) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(SyncRules.purchaseTime(item)),
                ZoneId.systemDefault()
        );

        String listName = listNameMap.getOrDefault(item.getListId(), "Unknown");

        // Parse quantity - default to 1 if not set or invalid
        int qty = 1;
        try {
            if (item.getQuantity() != null && !item.getQuantity().isEmpty()) {
                qty = Integer.parseInt(item.getQuantity().replaceAll("[^0-9]", ""));
                if (qty <= 0) qty = 1;
            }
        } catch (NumberFormatException e) {
            qty = 1;
        }

        return new PurchaseDate(dateTime, listName, qty, item.getName());
    }

    // Search items
    public List<String> searchUserItems(String userId, String query) {
        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return List.of();
        }

        // Get all completed items for these lists
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        String lowerQuery = query.toLowerCase();
        boolean isShortQuery = lowerQuery.length() < 4;

        return allItems.stream()
                .filter(item -> item.getIsCompleted() != null && item.getIsCompleted())
                .map(GroceryItemEntity::getName)
                .distinct()
                .filter(name -> {
                    if (name == null) return false;
                    String lowerName = name.toLowerCase();

                    // Exact match
                    if (lowerName.equals(lowerQuery)) return true;

                    // Starts with query followed by space (e.g., "Is te" matches "is")
                    if (lowerName.startsWith(lowerQuery + " ")) return true;

                    // Query appears after space (e.g., "Vanilje is")
                    if (lowerName.contains(" " + lowerQuery + " ")) return true;
                    if (lowerName.endsWith(" " + lowerQuery)) return true;

                    // Only match at end of compound words for longer queries (4+ chars)
                    // This avoids "gris" matching "is", but allows "Letmælk" matching "mælk"
                    if (!isShortQuery && lowerName.endsWith(lowerQuery) && !lowerName.equals(lowerQuery)) {
                        return true;
                    }

                    return false;
                })
                .sorted()
                .limit(20)
                .collect(Collectors.toList());
    }

    // NEW: Get top items by purchase count
    public List<com.grocerylist.backend.dto.TopItemStats> getTopItems(String userId, int limit) {
        // Get all user's lists
        List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
        List<String> listIds = userLists.stream()
                .map(GroceryListEntity::getId)
                .collect(Collectors.toList());

        if (listIds.isEmpty()) {
            return List.of();
        }

        // Get all completed items
        List<GroceryItemEntity> allItems = itemRepository.findByListIdIn(listIds);

        // Group by item name and count occurrences
        Map<String, List<GroceryItemEntity>> groupedItems = allItems.stream()
                .filter(item -> item.getName() != null &&
                        item.getIsCompleted() != null &&
                        item.getIsCompleted())
                .collect(Collectors.groupingBy(GroceryItemEntity::getName));

        // Convert to TopItemStats and sort by count
        return groupedItems.entrySet().stream()
                .map(entry -> {
                    String itemName = entry.getKey();
                    List<GroceryItemEntity> items = entry.getValue();
                    int count = items.size();

                    // Find most recent purchase
                    long lastPurchased = items.stream()
                            .mapToLong(SyncRules::purchaseTime)
                            .max()
                            .orElse(0L);

                    return new com.grocerylist.backend.dto.TopItemStats(itemName, count, lastPurchased);
                })
                .sorted((a, b) -> Integer.compare(b.getTotalCount(), a.getTotalCount())) // Descending
                .limit(limit)
                .collect(Collectors.toList());
    }
}