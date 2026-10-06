package com.grocerylist.backend.service;

import com.grocerylist.backend.model.GroceryItemEntity;
import com.grocerylist.backend.model.GroceryListEntity;
import com.grocerylist.backend.repository.GroceryItemRepository;
import com.grocerylist.backend.repository.GroceryListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroceryService {
    
    @Autowired
    private GroceryListRepository listRepository;
    
    @Autowired
    private GroceryItemRepository itemRepository;
    
    // List operations
    public List<GroceryListEntity> getAllLists(String userId) {
        return listRepository.findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(userId);
    }
    
    public Optional<GroceryListEntity> getListById(String id) {
        return listRepository.findByIdAndIsDeletedFalse(id);
    }
    
    /**
     * Creates or updates a list (the client sends the id, so save() is an upsert).
     * A write older than the stored row is ignored, so a late retry cannot overwrite a newer change.
     */
    public GroceryListEntity createList(GroceryListEntity list) {
        Optional<GroceryListEntity> stored = listRepository.findById(list.getId());
        if (stored.isPresent() && SyncRules.isStale(list.getUpdatedAt(), stored.get().getUpdatedAt())) {
            return stored.get();
        }
        return listRepository.save(list);
    }
    
    public GroceryListEntity updateList(GroceryListEntity list) {
        return createList(list);
    }
	
	public boolean deleteList(String id) {
    return listRepository.softDeleteById(id, System.currentTimeMillis()) > 0;
	}
    
    public GroceryListEntity duplicateList(String originalId, String newName, String userId) {
        Optional<GroceryListEntity> originalOpt = getListById(originalId);
        if (originalOpt.isEmpty()) {
            throw new RuntimeException("Original list not found");
        }
        
        // Create new list
        GroceryListEntity newList = new GroceryListEntity(newName, userId);
        newList = createList(newList);
        
        // Copy all items
        List<GroceryItemEntity> originalItems = itemRepository
                .findByListIdAndIsDeletedFalseOrderByPriorityAscCreatedAtAsc(originalId);
        
        for (GroceryItemEntity item : originalItems) {
            GroceryItemEntity newItem = new GroceryItemEntity(newList.getId(), item.getName());
            newItem.setQuantity(item.getQuantity());
            newItem.setUnit(item.getUnit());
            newItem.setNotes(item.getNotes());
            newItem.setCategory(item.getCategory());
            newItem.setPriority(item.getPriority());
            newItem.setIsCompleted(false); // Reset completion status
            itemRepository.save(newItem);
        }
        
        return newList;
    }
    
    // Item operations
    public List<GroceryItemEntity> getItemsForList(String listId) {
        return itemRepository.findByListIdAndIsDeletedFalseOrderByPriorityAscCreatedAtAsc(listId);
    }
    
    public Optional<GroceryItemEntity> getItemById(String id) {
        return itemRepository.findByIdAndIsDeletedFalse(id);
    }
    
    /**
     * Creates or updates an item (the client sends the id, so save() is an upsert).
     * A write older than the stored row is ignored, so a late retry cannot overwrite a newer change.
     * completedAt is maintained here so the purchase time survives later edits and deletes.
     */
    public GroceryItemEntity createItem(GroceryItemEntity item) {
        GroceryItemEntity stored = itemRepository.findById(item.getId()).orElse(null);
        if (stored != null && SyncRules.isStale(item.getUpdatedAt(), stored.getUpdatedAt())) {
            return stored;
        }
        item.setCompletedAt(SyncRules.resolveCompletedAt(item, stored));
        return itemRepository.save(item);
    }
    
    public GroceryItemEntity updateItem(GroceryItemEntity item) {
        return createItem(item);
    }
       
	public boolean deleteItem(String id) {
    return itemRepository.softDeleteById(id, System.currentTimeMillis()) > 0;
	}
    
    /** Soft delete, so the purchases stay in the analytics and other devices see the deletion on sync. */
    public int clearCompletedItems(String listId) {
        return itemRepository.softDeleteCompletedItemsByListId(listId, System.currentTimeMillis());
    }
    
    @Transactional
	public void clearAllDataForUser(String userId) {
		System.out.println("DEBUG: Starting to clear data for userId: " + userId);
		
		// Get ALL lists including deleted ones
		List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
		System.out.println("DEBUG: Found " + userLists.size() + " lists to clear (including deleted)");
		
		// Delete ALL items for these lists (including soft-deleted)
		for (GroceryListEntity list : userLists) {
			// Get ALL items, not just non-deleted
			List<GroceryItemEntity> items = itemRepository.findByListId(list.getId());
			System.out.println("DEBUG: Deleting " + items.size() + " items for list: " + list.getName());
			itemRepository.deleteAll(items);
		}
		
		// Delete ALL lists for this user
		listRepository.deleteAll(userLists);
		System.out.println("DEBUG: Cleared " + userLists.size() + " lists for user: " + userId);
	}
    
    // Bulk sync operations - NEW METHODS FOR SYNC
    
	// Get lists for sync - exclude old deleted items
	public List<GroceryListEntity> getAllListsForSync(String userId) {
		long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
		return listRepository.findActiveAndRecentlyDeletedLists(userId, thirtyDaysAgo);
	}
    	
	/**
	 * Items for sync: only items of the lists the sync returns (active and recently deleted).
	 * Items of lists deleted more than 30 days ago are left out, since the client would not
	 * have their list. Old deleted items are excluded as well.
	 */
	public List<GroceryItemEntity> getItemsForSync(List<GroceryListEntity> syncLists) {
		if (syncLists.isEmpty()) {
			return List.of();
		}
		long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
		List<String> listIds = syncLists.stream().map(GroceryListEntity::getId).toList();
		return itemRepository.findActiveAndRecentlyDeletedItems(listIds, thirtyDaysAgo);
	}
}