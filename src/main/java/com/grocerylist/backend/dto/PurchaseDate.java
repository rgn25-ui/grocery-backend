package com.grocerylist.backend.dto;

import java.time.LocalDateTime;

public class PurchaseDate {
    private LocalDateTime purchaseDate;
    private String listName;
    private int quantity;
    private String itemName;  // NEW: Add item name

    public PurchaseDate() {}

    public PurchaseDate(LocalDateTime purchaseDate, String listName, int quantity) {
        this.purchaseDate = purchaseDate;
        this.listName = listName;
        this.quantity = quantity;
    }

    public PurchaseDate(LocalDateTime purchaseDate, String listName, int quantity, String itemName) {
        this.purchaseDate = purchaseDate;
        this.listName = listName;
        this.quantity = quantity;
        this.itemName = itemName;
    }

    // Getters and setters
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getListName() { return listName; }
    public void setListName(String listName) { this.listName = listName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}