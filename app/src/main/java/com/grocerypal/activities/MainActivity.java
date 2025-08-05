package com.grocerypal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerypal.R;
import com.grocerypal.adapters.GroceryListAdapter;
import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GroceryListAdapter.OnListDeletedListener {

	private final List<GroceryList> groceryLists = new ArrayList<>();   // Master list
	private final List<GroceryList> filteredLists = new ArrayList<>();  // Filtered for search

	private RecyclerView recyclerView;
	private GroceryListAdapter adapter;
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		recyclerView = findViewById(R.id.recyclerViewLists);
		searchView = findViewById(R.id.searchView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Initialize adapter with filtered list and delete callback
		adapter = new GroceryListAdapter(filteredLists, this, this, this);
		recyclerView.setAdapter(adapter);

		// Floating Action Button to add a new list
		findViewById(R.id.fabAddList).setOnClickListener(view ->
				startActivity(new Intent(MainActivity.this, AddListActivity.class))
		);

		// Setup Search
		setupSearchView();

		// Load all lists initially
		loadGroceryLists();
	}

	/**
	 * Setup search to filter in real-time
	 */
	private void setupSearchView() {
		searchView.clearFocus();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				filterLists(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				filterLists(newText);
				return true;
			}
		});
	}

	/**
	 * Loads all grocery lists from the database asynchronously
	 */
	private void loadGroceryLists() {
		new Thread(() -> {
			List<GroceryList> allLists = GroceryDatabase.getInstance(this).groceryDao().getAllLists();
			runOnUiThread(() -> {
				groceryLists.clear();
				groceryLists.addAll(allLists);

				// Reset filter list to match the search query
				String currentQuery = searchView.getQuery().toString();
				filterLists(currentQuery);
			});
		}).start();
	}

	/**
	 * Filters the grocery lists by name
	 */
	private void filterLists(String query) {
		filteredLists.clear();
		if (TextUtils.isEmpty(query)) {
			filteredLists.addAll(groceryLists);
		} else {
			for (GroceryList list : groceryLists) {
				if (list.getName().toLowerCase().contains(query.toLowerCase())) {
					filteredLists.add(list);
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * Callback when a list is deleted from the adapter
	 */
	@Override
	public void onListDeleted(GroceryList deletedList) {
		groceryLists.remove(deletedList);
		// Update filtered lists to reflect deletion
		String currentQuery = searchView.getQuery().toString();
		filterLists(currentQuery);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadGroceryLists(); // Refresh list when returning to activity
	}
}
