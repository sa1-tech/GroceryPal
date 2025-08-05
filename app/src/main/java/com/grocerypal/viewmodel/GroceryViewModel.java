package com.grocerypal.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.grocerypal.database.GroceryItem;
import com.grocerypal.database.GroceryList;
import com.grocerypal.repository.GroceryRepository;

import java.util.List;

public class GroceryViewModel extends AndroidViewModel {

	private final GroceryRepository repository;
	public MutableLiveData<List<GroceryList>> listLiveData = new MutableLiveData<>();
	public MutableLiveData<List<GroceryItem>> itemsLiveData = new MutableLiveData<>();

	public GroceryViewModel(@NonNull Application application) {
		super(application);
		repository = new GroceryRepository(application);
		loadLists();
	}

	public void loadLists() {
		listLiveData.setValue(repository.getAllLists());
	}

	public void loadItems(int listId) {
		itemsLiveData.setValue(repository.getItemsByListId(listId));
	}

	public void addList(GroceryList list) {
		repository.insertList(list);
		loadLists();
	}

	public void addItem(GroceryItem item) {
		repository.insertItem(item);
		loadItems(item.getListId());
	}

	public void updateItem(GroceryItem item) {
		repository.updateItem(item);
		loadItems(item.getListId());
	}
}
