package com.grocerypal.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

	private final SharedPreferences prefs;

	public PreferenceUtils(Context context) {
		prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	}

	public boolean isDarkModeEnabled() {
		return prefs.getBoolean(Constants.DARK_MODE_KEY, false);
	}

	public void setDarkModeEnabled(boolean isEnabled) {
		prefs.edit().putBoolean(Constants.DARK_MODE_KEY, isEnabled).apply();
	}
}
