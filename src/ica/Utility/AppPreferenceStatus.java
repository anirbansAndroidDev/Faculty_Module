package ica.Utility;

import ica.ICAConstants.CarouselItems;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferenceStatus {

	public static final String IS_LOGGED_OUT = "IS_LOGGED_OUT";
	public static final String APP_PREFERENCES = "APP_PREFERENCES";

	public static boolean getLoggedOutStatus(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				APP_PREFERENCES, Context.MODE_WORLD_READABLE);
		boolean prefLogStatus = myPrefs.getBoolean(IS_LOGGED_OUT, true);

		return prefLogStatus;
	}

	public static void setLoggedOutStatus(Context context, boolean isLoggedOut) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				APP_PREFERENCES, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putBoolean(IS_LOGGED_OUT, isLoggedOut);

		prefsEditor.commit();

	}

	public static final String LAST_EXAM_STATUS = "LAST_EXAM_STATUS";
	public static final String LAST_EXAM_PREFERENCES = "LAST_EXAM_PREFERENCES";

	public static boolean getLastExamStatus(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				LAST_EXAM_PREFERENCES, Context.MODE_WORLD_READABLE);
		boolean prefLogStatus = myPrefs.getBoolean(LAST_EXAM_STATUS, true);

		return prefLogStatus;
	}

	public static void setLastExamStatus(Context context, boolean isLoggedOut) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				LAST_EXAM_PREFERENCES, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putBoolean(LAST_EXAM_STATUS, isLoggedOut);

		prefsEditor.commit();

	}


	public static final String LAST_CAROUSEL_ITEM = "LAST_CAROUSEL_ITEM";
	public static final String LAST_CAROUSEL_ITEM_PREF = "LAST_CAROUSEL_ITEM_PREF";

	public static int getLastCarouselItem(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				LAST_CAROUSEL_ITEM_PREF, Context.MODE_WORLD_READABLE);
		int itemnum = myPrefs.getInt(LAST_CAROUSEL_ITEM,CarouselItems.syncing_icon.ordinal());

		
		return itemnum;
	}

	public static void setLastCarouselItem(Context context,int ItemNum) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				LAST_CAROUSEL_ITEM_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(LAST_CAROUSEL_ITEM, ItemNum);

		prefsEditor.commit();

	}


}
