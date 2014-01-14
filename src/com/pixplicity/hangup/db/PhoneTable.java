package com.pixplicity.hangup.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PhoneTable {

	private static final String TAG = PhoneTable.class.getName();

	// Database table
	public static final String TABLE_NAME = "todo";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PHONE_NUMBER = "category";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_NAME
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PHONE_NUMBER + " text not null "
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TAG, "Upgrading database from version "
				+ oldVersion + " to " + newVersion);
		// XXX This will delete all data!
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
