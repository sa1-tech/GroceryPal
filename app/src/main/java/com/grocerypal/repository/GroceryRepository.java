package com.grocerypal.repository;

import android.content.Context;

import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryItem;
import com.grocerypal.database.GroceryList;

import java.util.List;

public class GroceryRepository {

	private final GroceryDatabase db;

	public GroceryRepository(Context context) {
		db = GroceryDatabase.getInstance(context);
	}

	public List<GroceryList> getAllLists() {
		return db.groceryDao().getAllLists();
	}

	public List<GroceryItem> getItemsByListId(int listId) {
		return db.groceryDao().getItemsByListId(listId);
	}

	public void insertList(GroceryList list) {
		db.groceryDao().insertList(list);
	}

	public void insertItem(GroceryItem item) {
		db.groceryDao().insertItem(item);
	}

	public void updateItem(GroceryItem item) {
		db.groceryDao().updateItem(item);
	}

	public void deleteList(GroceryList list) {
		db.groceryDao().deleteList(list);
	}

	public void deleteItem(GroceryItem item) {
		db.groceryDao().deleteItem(item);
	}
}
