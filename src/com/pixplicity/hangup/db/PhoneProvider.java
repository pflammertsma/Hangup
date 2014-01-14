package com.pixplicity.hangup.db;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class PhoneProvider extends ContentProvider {

	// database
	private DatabaseHelper database;

	// used for the UriMacher
	private static final int PHONES = 10;
	private static final int PHONE_ID = 20;

	private static final String AUTHORITY = "com.pixplicity.hangup.contentprovider";

	private static final String BASE_PATH = "todos";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/todos";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/todo";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, PHONES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PHONE_ID);
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Utility for building queries
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(PhoneTable.TABLE_NAME);

		// Always check if the projection maps with known columns
		checkColumns(projection);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PHONES:
			// Obtains all rows
			break;
		case PHONE_ID:
			// Select one row by its ID
			queryBuilder.appendWhere(PhoneTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);

		// Notify all listeners of changes
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		// Return the cursor
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case PHONES:
			id = sqlDB.insert(PhoneTable.TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify all listeners of changes
		getContext().getContentResolver().notifyChange(uri, null);

		// Return the URI to the new row
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case PHONES:
			rowsUpdated = sqlDB.update(PhoneTable.TABLE_NAME,
					values,
					selection,
					selectionArgs);
			break;
		case PHONE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(PhoneTable.TABLE_NAME,
						values,
						PhoneTable.COLUMN_ID + "=" + id,
						null);
			} else {
				rowsUpdated = sqlDB.update(PhoneTable.TABLE_NAME,
						values,
						PhoneTable.COLUMN_ID + "=" + id
								+ " and "
								+ selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify all listeners of changes
		// FIXME Presently, PhoneAdapter delays executing updates until the 
		// activity is paused to prevent this operation from causing a loop
		getContext().getContentResolver().notifyChange(uri, null);

		// Return the number of updated rows
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case PHONES:
			rowsDeleted = sqlDB.delete(PhoneTable.TABLE_NAME, selection,
					selectionArgs);
			break;
		case PHONE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(PhoneTable.TABLE_NAME,
						PhoneTable.COLUMN_ID + "=" + id,
						null);
			} else {
				rowsDeleted = sqlDB.delete(PhoneTable.TABLE_NAME,
						PhoneTable.COLUMN_ID + "=" + id
								+ " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify all listeners of changes
		getContext().getContentResolver().notifyChange(uri, null);

		// Return the number of deleted rows
		return rowsDeleted;
	}

	private void checkColumns(String[] projection) {
		String[] available = {
				PhoneTable.COLUMN_ID,
				PhoneTable.COLUMN_PHONE_NUMBER,
		};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

}
