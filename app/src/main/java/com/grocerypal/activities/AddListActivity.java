package com.grocerypal.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grocerypal.R;
import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryList;

public class AddListActivity extends AppCompatActivity {

	private EditText inputListName;
	private Button btnAddList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_list);

		inputListName = findViewById(R.id.inputListName);
		btnAddList = findViewById(R.id.btnAddList);

		btnAddList.setOnClickListener(v -> {
			String listName = inputListName.getText().toString().trim();

			if (listName.isEmpty()) {
				Toast.makeText(this, "Enter list name", Toast.LENGTH_SHORT).show();
				return;
			}

			// Perform DB insertion in background
			new Thread(() -> {
				GroceryList newList = new GroceryList(listName, System.currentTimeMillis());
				GroceryDatabase.getInstance(this).groceryDao().insertList(newList);

				runOnUiThread(() -> {
					Toast.makeText(this, "List added successfully!", Toast.LENGTH_SHORT).show();
					finish(); // Return to MainActivity
				});
			}).start();
		});
	}
}
