package com.grocerypal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grocery_items")  // <-- FIX: Use proper table name
public class GroceryItem {

	@PrimaryKey(autoGenerate = true)
	private int id;

	private String name;
	private boolean purchased;
	private int listId;
	private double price;

	public GroceryItem(String name, boolean purchased, int listId, double price) {
		this.name = name;
		this.purchased = purchased;
		this.listId = listId;
		this.price = price;
	}

	// --- Getters & Setters ---
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public boolean isPurchased() {
		return purchased;
	}

	public void setPurchased(boolean purchased) {
		this.purchased = purchased;
	}

	public int getListId() {
		return listId;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
