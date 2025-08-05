package com.grocerypal.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroceryDao {

	// ---------- Grocery Lists ----------
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insertList(GroceryList list);

	@Delete
	void deleteList(GroceryList list);

	@Update
	void updateList(GroceryList list);

	// Get all lists
	@Query("SELECT * FROM grocery_lists ORDER BY createdAt DESC")
	List<GroceryList> getAllLists();

	@Query("SELECT * FROM grocery_lists ORDER BY createdAt DESC")
	LiveData<List<GroceryList>> getAllListsLive();

	// ---------- Grocery Items ----------
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertItem(GroceryItem item);

	@Delete
	void deleteItem(GroceryItem item);

	@Update
	void updateItem(GroceryItem item);

	@Query("SELECT * FROM grocery_items WHERE listId = :listId")
	List<GroceryItem> getItemsByListId(int listId);

	@Query("SELECT * FROM grocery_items WHERE listId = :listId")
	LiveData<List<GroceryItem>> getItemsByListIdLive(int listId);

	@Query("SELECT COUNT(*) FROM grocery_items WHERE listId = :listId")
	int getItemCount(int listId);

	@Query("SELECT SUM(price) FROM grocery_items WHERE listId = :listId")
	Double getTotalPrice(int listId);

}
