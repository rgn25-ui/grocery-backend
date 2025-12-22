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
    
    public GroceryListEntity createList(GroceryListEntity list) {
        return listRepository.save(list);
    }
    
    public GroceryListEntity updateList(GroceryListEntity list) {
        return listRepository.save(list);
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
    
    public GroceryItemEntity createItem(GroceryItemEntity item) {
        return itemRepository.save(item);
    }
    
    public GroceryItemEntity updateItem(GroceryItemEntity item) {
        return itemRepository.save(item);
    }
       
	public boolean deleteItem(String id) {
    return itemRepository.softDeleteById(id, System.currentTimeMillis()) > 0;
	}
    
    public int clearCompletedItems(String listId) {
        return itemRepository.deleteCompletedItemsByListId(listId);
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
    	
	// Get items for sync - exclude old deleted items
	public List<GroceryItemEntity> getAllItemsForUserSync(String userId) {
		long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
		
		List<GroceryListEntity> userLists = listRepository.findAllByUserId(userId);
		List<String> listIds = userLists.stream().map(GroceryListEntity::getId).toList();
		
		return itemRepository.findActiveAndRecentlyDeletedItems(listIds, thirtyDaysAgo);
	}
}