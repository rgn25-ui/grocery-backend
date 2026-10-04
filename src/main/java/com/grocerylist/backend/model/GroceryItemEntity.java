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

    // Timestamps are set by the client and kept as sent - sync conflict resolution relies on them
    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // When the item was checked off (the purchase time used by the analytics).
    // Set by the backend in GroceryService; null while the item is not completed.
    @Column(name = "completed_at")
    private Long completedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "on_offer")
    private Boolean onOffer = false;

    @Column(name = "price")
    private String price;

    // Constructors
    public GroceryItemEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.onOffer = false;
        this.price = "";
    }

    public GroceryItemEntity(String listId, String name) {
        this();
        this.listId = listId;
        this.name = name;
    }

    // Getters and setters - no side effects, so the client's updatedAt survives deserialization
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

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
                ", onOffer=" + onOffer +
                ", price='" + price + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", completedAt=" + completedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
