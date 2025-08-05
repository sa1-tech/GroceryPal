package com.grocerypal.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerypal.R;
import com.grocerypal.activities.ListDetailsActivity;
import com.grocerypal.database.GroceryDatabase;
import com.grocerypal.database.GroceryItem;
import com.grocerypal.database.GroceryList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.ViewHolder> {

	private final List<GroceryList> list;
	private final Context context;
	private final GroceryDatabase db;
	private final LifecycleOwner lifecycleOwner;
	private final OnListDeletedListener deleteListener;
	public GroceryListAdapter(List<GroceryList> list, Context context, LifecycleOwner lifecycleOwner,
	                          OnListDeletedListener listener) {
		this.list = list;
		this.context = context;
		this.lifecycleOwner = lifecycleOwner;
		this.db = GroceryDatabase.getInstance(context);
		this.deleteListener = listener;
	}

	@NonNull
	@Override
	public GroceryListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_grocery_list, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull GroceryListAdapter.ViewHolder holder, int position) {
		GroceryList currentList = list.get(position);

		// Set list title and creation date
		holder.listTitle.setText(currentList.getName());
		holder.createdAt.setText(formatDate(currentList.getCreatedAt()));

		// Observe items for this list
		LiveData<List<GroceryItem>> liveItems = db.groceryDao().getItemsByListIdLive(currentList.getId());
		liveItems.observe(lifecycleOwner, items -> {
			int itemCount = items.size();
			double totalPrice = 0;
			for (GroceryItem item : items) {
				if (!item.isPurchased()) {
					totalPrice += item.getPrice();
				}
			}
			holder.itemCount.setText("Items: " + itemCount);
			holder.totalPrice.setText("Total: ₹" + String.format(Locale.getDefault(), "%.2f", totalPrice));
		});

		// Open List Details on card click
		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(context, ListDetailsActivity.class);
			intent.putExtra("list_id", currentList.getId());
			context.startActivity(intent);
		});

		// Export list
		holder.btnExport.setOnClickListener(v -> new Thread(() -> {
			List<GroceryItem> items = db.groceryDao().getItemsByListId(currentList.getId());
			exportList(currentList, items);
		}).start());

		// Share list
		holder.btnShare.setOnClickListener(v -> new Thread(() -> {
			List<GroceryItem> items = db.groceryDao().getItemsByListId(currentList.getId());
			shareList(currentList, items);
		}).start());

		// Delete list
		holder.btnDelete.setOnClickListener(v -> new Thread(() -> {
			db.groceryDao().deleteList(currentList);
			((AppCompatActivity) context).runOnUiThread(() -> {
				int pos = holder.getAdapterPosition();
				if (pos != RecyclerView.NO_POSITION) {
					list.remove(pos);
					notifyItemRemoved(pos);
				}
				Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show();

				if (deleteListener != null) {
					deleteListener.onListDeleted(currentList);
				}
			});
		}).start());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	/**
	 * Export list data to a .txt file in Downloads/GroceryPal
	 */
	private void exportList(GroceryList currentList, List<GroceryItem> items) {
		double total = 0;
		StringBuilder exportData = new StringBuilder();
		exportData.append("Grocery List: ").append(currentList.getName()).append("\n");
		exportData.append("Created: ").append(formatDate(currentList.getCreatedAt())).append("\n\n");

		for (GroceryItem item : items) {
			exportData.append("- ").append(item.getName())
					.append("  ₹").append(item.getPrice())
					.append(item.isPurchased() ? " (Purchased)" : "")
					.append("\n");
			if (!item.isPurchased()) total += item.getPrice();
		}
		exportData.append("\nTotal: ₹").append(String.format("%.2f", total));

		((AppCompatActivity) context).runOnUiThread(() -> {
			try {
				String fileName = currentList.getName() + ".txt";

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					ContentValues values = new ContentValues();
					values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
					values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
					values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/GroceryPal");
					Uri uri = context.getContentResolver()
							.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

					if (uri != null) {
						try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
							outputStream.write(exportData.toString().getBytes());
							outputStream.flush();
						}
						Toast.makeText(context, "Exported to Downloads/GroceryPal", Toast.LENGTH_LONG).show();
					}
				} else {
					File folder = new File(Environment.getExternalStoragePublicDirectory(
							Environment.DIRECTORY_DOWNLOADS), "GroceryPal");
					if (!folder.exists()) folder.mkdirs();
					File file = new File(folder, fileName);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(exportData.toString().getBytes());
					fos.flush();
					fos.close();
					Toast.makeText(context, "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Share list data using an Intent
	 */
	private void shareList(GroceryList currentList, List<GroceryItem> items) {
		double total = 0;
		StringBuilder shareData = new StringBuilder();
		shareData.append("Grocery List: ").append(currentList.getName()).append("\n\n");

		for (GroceryItem item : items) {
			shareData.append("- ").append(item.getName())
					.append("  ₹").append(item.getPrice())
					.append("\n");
			if (!item.isPurchased()) total += item.getPrice();
		}
		shareData.append("\nTotal: ₹").append(String.format("%.2f", total));

		((AppCompatActivity) context).runOnUiThread(() -> {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Grocery List");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareData.toString());
			context.startActivity(Intent.createChooser(shareIntent, "Share Grocery List"));
		});
	}

	private String formatDate(long millis) {
		return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
	}

	public interface OnListDeletedListener {
		void onListDeleted(GroceryList deletedList);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		TextView listTitle, createdAt, itemCount, totalPrice;
		ImageButton btnExport, btnShare, btnDelete;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			listTitle = itemView.findViewById(R.id.listTitle);
			createdAt = itemView.findViewById(R.id.createdAt);
			itemCount = itemView.findViewById(R.id.itemCount);
			totalPrice = itemView.findViewById(R.id.totalPrice);
			btnExport = itemView.findViewById(R.id.btnExport);
			btnShare = itemView.findViewById(R.id.btnShare);
			btnDelete = itemView.findViewById(R.id.btnDelete);
		}
	}
}
