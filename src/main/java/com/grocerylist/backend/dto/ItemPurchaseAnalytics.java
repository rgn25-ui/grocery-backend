package com.grocerylist.backend.dto;

public class ItemPurchaseAnalytics {
    private String itemName;
    private int lastWeek;
    private int lastMonth;
    private int lastQuarter;
    private int lastYear;
    private int totalAllTime;

    public ItemPurchaseAnalytics() {}

    public ItemPurchaseAnalytics(String itemName, int lastWeek, int lastMonth,
                                 int lastQuarter, int lastYear, int totalAllTime) {
        this.itemName = itemName;
        this.lastWeek = lastWeek;
        this.lastMonth = lastMonth;
        this.lastQuarter = lastQuarter;
        this.lastYear = lastYear;
        this.totalAllTime = totalAllTime;
    }

    // Getters and setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getLastWeek() { return lastWeek; }
    public void setLastWeek(int lastWeek) { this.lastWeek = lastWeek; }

    public int getLastMonth() { return lastMonth; }
    public void setLastMonth(int lastMonth) { this.lastMonth = lastMonth; }

    public int getLastQuarter() { return lastQuarter; }
    public void setLastQuarter(int lastQuarter) { this.lastQuarter = lastQuarter; }

    public int getLastYear() { return lastYear; }
    public void setLastYear(int lastYear) { this.lastYear = lastYear; }

    public int getTotalAllTime() { return totalAllTime; }
    public void setTotalAllTime(int totalAllTime) { this.totalAllTime = totalAllTime; }
}