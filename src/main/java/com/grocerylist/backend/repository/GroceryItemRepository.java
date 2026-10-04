package com.grocerylist.backend.repository;

import com.grocerylist.backend.model.GroceryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItemEntity, String> {
    
    // Find all non-deleted items for a list
    List<GroceryItemEntity> findByListIdAndIsDeletedFalseOrderByPriorityAscCreatedAtAsc(String listId);
    
    // Find a specific non-deleted item
    Optional<GroceryItemEntity> findByIdAndIsDeletedFalse(String id);
    
    // Soft delete an item
	@Modifying
	@Transactional
	@Query("UPDATE GroceryItemEntity SET isDeleted = true, updatedAt = :updatedAt WHERE id = :id")
	int softDeleteById(@Param("id") String id, @Param("updatedAt") Long updatedAt);
    
    // Soft delete completed items from a list (keeps them for the purchase analytics)
    @Modifying
    @Transactional
    @Query("UPDATE GroceryItemEntity SET isDeleted = true, updatedAt = :updatedAt " +
           "WHERE listId = :listId AND isCompleted = true AND isDeleted = false")
    int softDeleteCompletedItemsByListId(@Param("listId") String listId, @Param("updatedAt") Long updatedAt);

    // One-time backfill: completed rows from before completed_at existed get their current purchase time
    @Modifying
    @Transactional
    @Query("UPDATE GroceryItemEntity SET completedAt = updatedAt WHERE isCompleted = true AND completedAt IS NULL")
    int backfillCompletedAt();
    
    // Count items in a list
    long countByListIdAndIsDeletedFalse(String listId);
    
    // Count completed items in a list
    long countByListIdAndIsCompletedTrueAndIsDeletedFalse(String listId);
    
    // Find all items for multiple lists (for bulk sync)
    @Query("SELECT i FROM GroceryItemEntity i WHERE i.listId IN :listIds AND i.isDeleted = false")
    List<GroceryItemEntity> findByListIdInAndIsDeletedFalse(@Param("listIds") List<String> listIds);
	
	// Returns ALL items including deleted
	@Query("SELECT i FROM GroceryItemEntity i WHERE i.listId IN :listIds")
	List<GroceryItemEntity> findByListIdIn(@Param("listIds") List<String> listIds);
	
	// Get ALL items for a list (including deleted) - for clearing
	List<GroceryItemEntity> findByListId(String listId);
	
	// Returns active items + items deleted within last 30 days
	@Query("SELECT i FROM GroceryItemEntity i WHERE i.listId IN :listIds " +
		   "AND (i.isDeleted = false OR (i.isDeleted = true AND i.updatedAt > :cutoffTime))")
	List<GroceryItemEntity> findActiveAndRecentlyDeletedItems(
		@Param("listIds") List<String> listIds,
		@Param("cutoffTime") Long cutoffTime
	);
}