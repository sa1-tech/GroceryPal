package com.grocerypal.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerypal.R;
import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryItem;

import java.util.List;

public class GroceryItemAdapter extends RecyclerView.Adapter<GroceryItemAdapter.ViewHolder> {

	private final List<GroceryItem> itemList;
	private final Context context;
	private final OnPriceChangeListener priceChangeListener;

	public GroceryItemAdapter(List<GroceryItem> itemList, Context context, OnPriceChangeListener listener) {
		this.itemList = itemList;
		this.context = context;
		this.priceChangeListener = listener;
	}

	@NonNull
	@Override
	public GroceryItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_grocery_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull GroceryItemAdapter.ViewHolder holder, int position) {
		GroceryItem item = itemList.get(position);

		holder.checkBox.setChecked(item.isPurchased());
		holder.itemName.setText(item.getName());
		holder.itemPrice.setText("₹" + String.format("%.2f", item.getPrice()));

		// Apply strike-through if purchased
		applyStrikeThrough(holder.itemName, item.isPurchased());

		holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			// Update local model
			item.setPurchased(isChecked);

			// Update strike-through immediately
			applyStrikeThrough(holder.itemName, isChecked);

			// Update DB in background thread
			new Thread(() -> GroceryDatabase.getInstance(context).groceryDao().updateItem(item)).start();

			// Notify activity to recalculate total
			if (priceChangeListener != null) {
				priceChangeListener.onPriceChanged();
			}
		});
	}

	@Override
	public int getItemCount() {
		return itemList.size();
	}

	/**
	 * Helper to toggle strike-through
	 */
	private void applyStrikeThrough(TextView textView, boolean isPurchased) {
		if (isPurchased) {
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
		}
	}

	/**
	 * Interface to notify activity when any checkbox is toggled
	 */
	public interface OnPriceChangeListener {
		void onPriceChanged();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		CheckBox checkBox;
		TextView itemName, itemPrice;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			checkBox = itemView.findViewById(R.id.checkboxItem);
			itemName = itemView.findViewById(R.id.itemText);
			itemPrice = itemView.findViewById(R.id.itemPrice);
		}
	}
}
