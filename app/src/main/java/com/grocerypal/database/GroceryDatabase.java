package com.grocerypal.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GroceryList.class, GroceryItem.class}, version = 2, exportSchema = false)
public abstract class GroceryDatabase extends RoomDatabase {

	private static final String DB_NAME = "grocery_pal_db";
	private static volatile GroceryDatabase instance;

	public static GroceryDatabase getInstance(Context context) {
		if (instance == null) {
			synchronized (GroceryDatabase.class) {
				if (instance == null) {
					instance = Room.databaseBuilder(
									context.getApplicationContext(),
									GroceryDatabase.class,
									DB_NAME
							)
							.fallbackToDestructiveMigration()
							.build(); // ❌ removed allowMainThreadQueries
				}
			}
		}
		return instance;
	}

	public abstract GroceryDao groceryDao();
}
