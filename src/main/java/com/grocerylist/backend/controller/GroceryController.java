package com.grocerylist.backend.controller;

import com.grocerylist.backend.dto.SyncDataResponse;
import com.grocerylist.backend.model.GroceryItemEntity;
import com.grocerylist.backend.model.GroceryListEntity;
import com.grocerylist.backend.service.GroceryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // REMOVED: @RequestMapping("/api") to fix endpoint conflicts
public class GroceryController {
    
    @Autowired
    private GroceryService groceryService;
    
    // ===== ADMIN ENDPOINTS =====
    
    @GetMapping("/admin/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin endpoints are working at " + System.currentTimeMillis());
    }
    
    @DeleteMapping("/admin/clear-all")
    public ResponseEntity<String> clearAllData(@RequestParam String userId, @RequestParam String confirmToken) {
        if (!"CLEAR_GROCERY_DATA_2025".equals(confirmToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
        }
        
        try {
            System.out.println("DEBUG: Clearing all data for userId: " + userId);
            groceryService.clearAllDataForUser(userId);
            System.out.println("DEBUG: Successfully cleared all data");
            return ResponseEntity.ok("All data cleared successfully for user: " + userId);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to clear data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing data: " + e.getMessage());
        }
    }
    
    // ===== SYNC ENDPOINT =====
    
    @GetMapping("/api/sync")
	public ResponseEntity<SyncDataResponse> syncData(@RequestParam String userId) {
		try {
			System.out.println("DEBUG: Sync request for userId: " + userId);
			
			// CHANGED: Use new methods that include deleted items
			List<GroceryListEntity> lists = groceryService.getAllListsForSync(userId);
			List<GroceryItemEntity> items = groceryService.getAllItemsForUserSync(userId);
			
			SyncDataResponse response = new SyncDataResponse(lists, items);
			System.out.println("DEBUG: Returning " + lists.size() + " lists and " + items.size() + " items (including deleted)");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.out.println("ERROR: Sync failed: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new SyncDataResponse("Sync failed: " + e.getMessage()));
		}
	}
    
    // ===== LIST ENDPOINTS =====
    
    @GetMapping("/api/lists")
    public ResponseEntity<List<GroceryListEntity>> getAllLists(@RequestParam String userId) {
        try {
            List<GroceryListEntity> lists = groceryService.getAllLists(userId);
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            System.out.println("ERROR: Get lists failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/api/lists/{id}")
    public ResponseEntity<GroceryListEntity> getListById(@PathVariable String id) {
        Optional<GroceryListEntity> list = groceryService.getListById(id);
        return list.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/lists")
    public ResponseEntity<GroceryListEntity> createList(@RequestBody GroceryListEntity list) {
        try {
            // Debug logging
            System.out.println("DEBUG: Creating list: " + list.getName() + " for user: " + list.getUserId());
            
            // Validation
            if (list.getName() == null || list.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (list.getUserId() == null || list.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Set timestamps if not provided
            if (list.getCreatedAt() == null) {
                list.setCreatedAt(System.currentTimeMillis());
            }
            if (list.getUpdatedAt() == null) {
                list.setUpdatedAt(System.currentTimeMillis());
            }
            if (list.getIsDeleted() == null) {
                list.setIsDeleted(false);
            }
            
            GroceryListEntity savedList = groceryService.createList(list);
            System.out.println("DEBUG: Successfully created list with ID: " + savedList.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedList);
            
        } catch (Exception e) {
            System.out.println("ERROR: Create list failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/api/lists/{id}")
    public ResponseEntity<GroceryListEntity> updateList(@PathVariable String id, @RequestBody GroceryListEntity list) {
        try {
            Optional<GroceryListEntity> existingList = groceryService.getListById(id);
            if (existingList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            list.setId(id);
            list.setUpdatedAt(System.currentTimeMillis());
            GroceryListEntity updatedList = groceryService.updateList(list);
            return ResponseEntity.ok(updatedList);
        } catch (Exception e) {
            System.out.println("ERROR: Update list failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/api/lists/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable String id) {
        try {
            boolean deleted = groceryService.deleteList(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("ERROR: Delete list failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/api/lists/{id}/duplicate")
    public ResponseEntity<GroceryListEntity> duplicateList(
            @PathVariable String id,
            @RequestParam String newName,
            @RequestParam String userId) {
        try {
            GroceryListEntity duplicatedList = groceryService.duplicateList(id, newName, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(duplicatedList);
        } catch (Exception e) {
            System.out.println("ERROR: Duplicate list failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ===== ITEM ENDPOINTS =====
    
    @GetMapping("/api/lists/{listId}/items")
    public ResponseEntity<List<GroceryItemEntity>> getItemsForList(@PathVariable String listId) {
        try {
            List<GroceryItemEntity> items = groceryService.getItemsForList(listId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            System.out.println("ERROR: Get items failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/api/items/{id}")
    public ResponseEntity<GroceryItemEntity> getItemById(@PathVariable String id) {
        Optional<GroceryItemEntity> item = groceryService.getItemById(id);
        return item.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/items")
public ResponseEntity<GroceryItemEntity> createItem(@RequestBody GroceryItemEntity item) {
    try {
        System.out.println("DEBUG: Creating item: " + item.getName() + " for list: " + item.getListId());
        System.out.println("DEBUG: OnOffer: " + item.getOnOffer() + ", Price: " + item.getPrice()); // NEW
        
        if (item.getListId() == null || item.getName() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Set timestamps if not provided
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(System.currentTimeMillis());
        }
        if (item.getUpdatedAt() == null) {
            item.setUpdatedAt(System.currentTimeMillis());
        }
        if (item.getIsDeleted() == null) {
            item.setIsDeleted(false);
        }
        if (item.getIsCompleted() == null) {
            item.setIsCompleted(false);
        }
        if (item.getOnOffer() == null) {           // NEW
            item.setOnOffer(false);
        }
        if (item.getPrice() == null) {             // NEW
            item.setPrice("");
        }
        
        GroceryItemEntity savedItem = groceryService.createItem(item);
        System.out.println("DEBUG: Successfully created item with ID: " + savedItem.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        
    } catch (Exception e) {
        System.out.println("ERROR: Create item failed: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
    
    @PutMapping("/api/items/{id}")
    public ResponseEntity<GroceryItemEntity> updateItem(@PathVariable String id, @RequestBody GroceryItemEntity item) {
        try {
            Optional<GroceryItemEntity> existingItem = groceryService.getItemById(id);
            if (existingItem.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            item.setId(id);
            item.setUpdatedAt(System.currentTimeMillis());
            GroceryItemEntity updatedItem = groceryService.updateItem(item);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            System.out.println("ERROR: Update item failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/api/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        try {
            boolean deleted = groceryService.deleteItem(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("ERROR: Delete item failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/api/lists/{listId}/completed-items")
    public ResponseEntity<Void> clearCompletedItems(@PathVariable String listId) {
        try {
            groceryService.clearCompletedItems(listId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("ERROR: Clear completed items failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}