package com.grocerylist.backend.repository;

import com.grocerylist.backend.model.GroceryListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryListRepository extends JpaRepository<GroceryListEntity, String> {
    
    // Find all non-deleted lists for a user
    List<GroceryListEntity> findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(String userId);
    
    // NEW: Returns ALL lists including deleted (for sync)
    @Query("SELECT l FROM GroceryListEntity l WHERE l.userId = :userId ORDER BY l.updatedAt DESC")
    List<GroceryListEntity> findAllByUserId(@Param("userId") String userId);
	
	// Returns active lists + lists deleted within last 30 days
	@Query("SELECT l FROM GroceryListEntity l WHERE l.userId = :userId " +
		   "AND (l.isDeleted = false OR (l.isDeleted = true AND l.updatedAt > :cutoffTime)) " +
		   "ORDER BY l.updatedAt DESC")
	List<GroceryListEntity> findActiveAndRecentlyDeletedLists(
		@Param("userId") String userId, 
		@Param("cutoffTime") Long cutoffTime
	);
    
    // Find a specific non-deleted list
    Optional<GroceryListEntity> findByIdAndIsDeletedFalse(String id);
    
    // Soft delete a list   
	@Modifying
	@Transactional
	@Query("UPDATE GroceryListEntity SET isDeleted = true, updatedAt = :updatedAt WHERE id = :id")
	int softDeleteById(@Param("id") String id, @Param("updatedAt") Long updatedAt);
    
    // Count non-deleted lists for a user
    long countByUserIdAndIsDeletedFalse(String userId);
}