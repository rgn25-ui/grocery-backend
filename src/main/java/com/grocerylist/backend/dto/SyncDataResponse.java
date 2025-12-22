package com.grocerylist.backend.dto;

import com.grocerylist.backend.model.GroceryItemEntity;
import com.grocerylist.backend.model.GroceryListEntity;

import java.util.List;

public class SyncDataResponse {
    private List<GroceryListEntity> lists;
    private List<GroceryItemEntity> items;
    private String message;
    private long timestamp;
    
    public SyncDataResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public SyncDataResponse(List<GroceryListEntity> lists, List<GroceryItemEntity> items) {
        this();
        this.lists = lists;
        this.items = items;
        this.message = "Sync successful";
    }
    
    public SyncDataResponse(String message) {
        this();
        this.message = message;
    }
    
    // Getters and Setters
    public List<GroceryListEntity> getLists() { return lists; }
    public void setLists(List<GroceryListEntity> lists) { this.lists = lists; }
    
    public List<GroceryItemEntity> getItems() { return items; }
    public void setItems(List<GroceryItemEntity> items) { this.items = items; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}