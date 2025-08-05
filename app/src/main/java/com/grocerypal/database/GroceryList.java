package com.grocerypal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grocery_lists")
public class GroceryList {

	@PrimaryKey(autoGenerate = true)
	private int id;
	private String name;
	private long createdAt;

	public GroceryList(String name, long createdAt) {
		this.name = name;
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public long getCreatedAt() {
		return createdAt;
	}
}
