package com.grocerylist.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "grocery_items")
public class GroceryItemEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "List ID is required")
    @Column(name = "list_id", nullable = false, length = 36)
    private String listId;
    
    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must be less than 100 characters")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "quantity")
    private String quantity;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    // CHANGED: Use Long instead of LocalDateTime to match Android
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
	
	@Column(name = "on_offer")
private Boolean onOffer = false;

@Column(name = "price") 
private String price;
    
    // Constructors
    public GroceryItemEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis(); // Changed to use milliseconds
        this.updatedAt = System.currentTimeMillis(); // Changed to use milliseconds
		this.onOffer = false;           // NEW: Default not on offer
    this.price = "";                // NEW: Default empty price
    }
    
    public GroceryItemEntity(String listId, String name) {
        this();
        this.listId = listId;
        this.name = name;
    }
    
    // Update timestamp before saving
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis(); // Changed to use milliseconds
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.updatedAt = System.currentTimeMillis(); // Changed to use milliseconds
    }
    
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { 
        this.isCompleted = isCompleted;
        this.updatedAt = System.currentTimeMillis(); // Changed to use milliseconds
    }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    // CHANGED: Return Long instead of LocalDateTime
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
	
	public Boolean getOnOffer() { return onOffer; }
public void setOnOffer(Boolean onOffer) { this.onOffer = onOffer; }

public String getPrice() { return price; }
public void setPrice(String price) { this.price = price; }
    
    @Override
public String toString() {
    return "GroceryItemEntity{" +
            "id='" + id + '\'' +
            ", listId='" + listId + '\'' +
            ", name='" + name + '\'' +
            ", quantity='" + quantity + '\'' +
            ", unit='" + unit + '\'' +
            ", category='" + category + '\'' +
            ", isCompleted=" + isCompleted +
            ", priority=" + priority +
            ", onOffer=" + onOffer +         // NEW
            ", price='" + price + '\'' +     // NEW
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", isDeleted=" + isDeleted +
            '}';
}
}