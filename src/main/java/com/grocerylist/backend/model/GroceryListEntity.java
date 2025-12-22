package com.grocerylist.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "grocery_lists")
public class GroceryListEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "List name is required")
    @Size(max = 100, message = "List name must be less than 100 characters")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "user_id", length = 36)
    private String userId;
    
    // CHANGED: Use long instead of LocalDateTime to match Android
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    // Constructors
    public GroceryListEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    public GroceryListEntity(String name, String userId) {
        this();
        this.name = name;
        this.userId = userId;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}