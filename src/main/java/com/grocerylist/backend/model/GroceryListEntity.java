package com.grocerylist.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "grocery_lists")
public class GroceryListEntity {

    /** Store used when a list has no category, e.g. lists created before the column existed. */
    public static final String DEFAULT_CATEGORY = "REMA";

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "List name is required")
    @Size(max = 100, message = "List name must be less than 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", length = 36)
    private String userId;

    // Store: REMA, COOP or ANDRE (matches ListCategory in the Android app)
    @Column(name = "category", length = 20)
    private String category;

    // Timestamps are set by the client and kept as sent - sync conflict resolution relies on them
    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Constructors
    public GroceryListEntity() {
        this.id = UUID.randomUUID().toString();
        this.category = DEFAULT_CATEGORY;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public GroceryListEntity(String name, String userId) {
        this();
        this.name = name;
        this.userId = userId;
    }

    // Getters and setters - no side effects, so the client's updatedAt survives deserialization
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    /** Never null towards clients: old rows without a category are reported as the default store. */
    public String getCategory() { return category != null ? category : DEFAULT_CATEGORY; }
    public void setCategory(String category) { this.category = category; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
