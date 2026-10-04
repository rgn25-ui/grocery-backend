package com.grocerylist.backend.dto;

public class TopItemStats {
    private String itemName;
    private int totalCount;
    private long lastPurchased; // timestamp

    public TopItemStats() {}

    public TopItemStats(String itemName, int totalCount, long lastPurchased) {
        this.itemName = itemName;
        this.totalCount = totalCount;
        this.lastPurchased = lastPurchased;
    }

    // Getters and setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public long getLastPurchased() { return lastPurchased; }
    public void setLastPurchased(long lastPurchased) { this.lastPurchased = lastPurchased; }
}