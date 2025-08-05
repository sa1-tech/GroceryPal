package com.grocerypal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerypal.R;
import com.grocerypal.adapters.GroceryItemAdapter;
import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryItem;
import com.grocerypal.utils.AISuggestionsHelper;

import java.util.ArrayList;
import java.util.List;

public class ListDetailsActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private GroceryItemAdapter adapter;
	private List<GroceryItem> items = new ArrayList<>();
	private int listId;

	private TextView totalPriceText;
	private AutoCompleteTextView inputName;
	private EditText inputPrice;
	private ImageView addBtn;
	private ArrayAdapter<String> suggestionAdapter;

	private Handler handler = new Handler();
	private Runnable fetchSuggestionsRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_details);

		// Get list ID from intent
		listId = getIntent().getIntExtra("list_id", -1);

		// Initialize UI
		recyclerView = findViewById(R.id.recyclerItems);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		totalPriceText = findViewById(R.id.totalPrice);
		inputName = findViewById(R.id.inputNewItem);
		inputPrice = findViewById(R.id.inputItemPrice);
		addBtn = findViewById(R.id.addItemBtn);

		// Setup suggestions
		inputName.setThreshold(1);
		inputName.requestFocus();
		suggestionAdapter = new ArrayAdapter<>(
				this,
				android.R.layout.simple_dropdown_item_1line,
				new ArrayList<>()
		);
		inputName.setAdapter(suggestionAdapter);

		// Debounced AI suggestions
		inputName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (fetchSuggestionsRunnable != null)
					handler.removeCallbacks(fetchSuggestionsRunnable);

				if (s.length() > 2) {
					fetchSuggestionsRunnable = () ->
							AISuggestionsHelper.getSuggestions(s.toString(), suggestions -> runOnUiThread(() -> {
								suggestionAdapter.clear();
								suggestionAdapter.addAll(suggestions);
								suggestionAdapter.notifyDataSetChanged();
								if (!suggestions.isEmpty()) inputName.showDropDown();
							}));

					handler.postDelayed(fetchSuggestionsRunnable, 600);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		addBtn.setOnClickListener(v -> addNewItem());
	}

	/**
	 * Adds a new grocery item
	 */
	private void addNewItem() {
		String itemName = inputName.getText().toString().trim();
		String priceText = inputPrice.getText().toString().trim();

		if (itemName.isEmpty()) {
			Toast.makeText(this, "Enter item name", Toast.LENGTH_SHORT).show();
			return;
		}

		double price = 0.0;
		if (!priceText.isEmpty()) {
			try {
				price = Double.parseDouble(priceText);
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		GroceryItem item = new GroceryItem(itemName, false, listId, price);

		// Insert in DB on background thread
		new Thread(() -> {
			GroceryDatabase.getInstance(this).groceryDao().insertItem(item);
			runOnUiThread(() -> {
				inputName.setText("");
				inputPrice.setText("");
				loadItems(); // reload list after adding
			});
		}).start();
	}

	/**
	 * Loads all items for this list asynchronously
	 */
	private void loadItems() {
		new Thread(() -> {
			items = GroceryDatabase.getInstance(this).groceryDao().getItemsByListId(listId);

			runOnUiThread(() -> {
				adapter = new GroceryItemAdapter(items, this, this::calculateTotalPriceWithAnimation);
				recyclerView.setAdapter(adapter);
				calculateTotalPriceWithAnimation();
			});
		}).start();
	}

	/**
	 * Calculates total and animates change
	 */
	private void calculateTotalPriceWithAnimation() {
		double tempTotal = 0.0;
		for (GroceryItem item : items) {
			if (!item.isPurchased()) {
				tempTotal += item.getPrice();
			}
		}

		final double totalPrice = tempTotal; // Make it effectively final for lambda

		// Smooth fade animation
		totalPriceText.animate().alpha(0f).setDuration(150).withEndAction(() -> {
			totalPriceText.setText("Total: ₹" + String.format("%.2f", totalPrice));
			totalPriceText.animate().alpha(1f).setDuration(150).start();
		}).start();
	}


	@Override
	protected void onResume() {
		super.onResume();
		loadItems(); // Refresh items
	}
}
