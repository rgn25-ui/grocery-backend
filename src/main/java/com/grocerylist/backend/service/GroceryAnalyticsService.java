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
                .filter(item -> item.getUpdatedAt() != null && item.getUpdatedAt() > oneWeekAgo)
                .count();

        int lastMonth = (int) matchingItems.stream()
                .filter(item -> item.getUpdatedAt() != null && item.getUpdatedAt() > oneMonthAgo)
                .count();

        int lastQuarter = (int) matchingItems.stream()
                .filter(item -> item.getUpdatedAt() != null && item.getUpdatedAt() > threeMonthsAgo)
                .count();

        int lastYear = (int) matchingItems.stream()
                .filter(item -> item.getUpdatedAt() != null && item.getUpdatedAt() > oneYearAgo)
                .count();

        int totalAllTime = matchingItems.size();

        return new ItemPurchaseAnalytics(itemName, lastWeek, lastMonth, lastQuarter, lastYear, totalAllTime);
    }

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
                .map(item -> {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(item.getUpdatedAt()),
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

                    return new PurchaseDate(dateTime, listName, qty);
                })
                .sorted((a, b) -> b.getPurchaseDate().compareTo(a.getPurchaseDate())) // Most recent first
                .collect(Collectors.toList());
    }

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

        return allItems.stream()
                .filter(item -> item.getIsCompleted() != null && item.getIsCompleted())
                .map(GroceryItemEntity::getName)
                .distinct()
                .filter(name -> name != null && name.toLowerCase().contains(query.toLowerCase()))
                .sorted()
                .limit(20) // Limit to 20 results for performance
                .collect(Collectors.toList());
    }
}