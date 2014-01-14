package com.pixplicity.hangup;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.pixplicity.hangup.db.Phone;
import com.pixplicity.hangup.db.PhoneProvider;
import com.pixplicity.hangup.db.PhoneTable;

public class CallReceiver extends BroadcastReceiver {

	private static final String TAG = CallReceiver.class.getSimpleName();

	public static final String PREF_FILE = "hangup";
	public static final String PREF_PHONE_NUMBER = "phone_number";

	private SharedPreferences mPrefs;

	private Thread mThread;

	private int mRingerMode;

	private SparseArray<Phone> mPhones;

	private static CallReceiver sInstance;

	public static CallReceiver getInstance() {
		if (sInstance == null) {
			sInstance = new CallReceiver();
		}
		return sInstance;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		sInstance = this;
		String appName = context.getString(R.string.app_name);
		ITelephony telephonyService = getTeleService(context);
		if (telephonyService != null) {
			SparseArray<Phone> phones = getPhones(context, false);
			String incomingNumber = null;
			try {
				incomingNumber = intent.getExtras().getString(
						TelephonyManager.EXTRA_INCOMING_NUMBER);
				if (incomingNumber != null && phones != null && phones.size() > 0) {
					Log.i(TAG, "incoming call from " + incomingNumber);
					boolean reject = false;
					for (int i = 0; i < phones.size(); i++) {
						Phone phone = phones.valueAt(i);
						// Remove various characters to ensure a match
						String matchNumber = phone.getPhoneNumber()
								.replaceAll("[^0-9\\+.,;#\\*N]", "");
						if (incomingNumber.equals(matchNumber)) {
							reject = true;
							break;
						}
					}
					if (reject) {
						if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
							// Old method of silencing the ringer
							telephonyService.silenceRinger();
						} else {
							final AudioManager am = (AudioManager) context
									.getSystemService(Context.AUDIO_SERVICE);
							if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
								// Silence the ringer
								if (mThread == null || !mThread.isAlive()) {
									mRingerMode = am.getRingerMode();
								}
								am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
								mThread = new Thread() {

									public void run() {
										SystemClock.sleep(3000);
										am.setRingerMode(mRingerMode);
									};
								};
								mThread.start();
							}
						}
						telephonyService.endCall();
						Toast.makeText(context, appName + " rejecting call from " + incomingNumber,
								Toast.LENGTH_LONG).show();
						Log.i(TAG, "call ended");
					} else {
						Toast.makeText(context, appName + " accepted call from " + incomingNumber,
								Toast.LENGTH_LONG).show();
						Log.i(TAG, "call accepted");
					}
				} else {
					// A phone state was encountered without incoming number, e.g. an outgoing call; ignore 
				}
			} catch (Exception e) {
				if (incomingNumber == null)
					incomingNumber = "[unknown]";
				e.printStackTrace();
				Log.e(TAG, "failed getting call info", e);
				Toast.makeText(
						context, appName + " failed rejecting call from " + incomingNumber + "\n"
								+ e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private ITelephony getTeleService(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method m = Class.forName(tm.getClass().getName())
					.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			return (ITelephony) m.invoke(tm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setPhone(Context context, int index, Phone phone) {
		getPhones(context, false);
		mPhones.put(index, phone);
	}

	public SparseArray<Phone> getPhones(Context context, boolean reload) {
		String[] projection = {
				PhoneTable.COLUMN_ID,
				PhoneTable.COLUMN_PHONE_NUMBER,
		};
		if (mPhones == null || reload) {
			if (mPhones == null) {
				mPhones = new SparseArray<Phone>();
			} else {
				mPhones.clear();
			}
			Cursor cursor = context.getContentResolver().query(PhoneProvider.CONTENT_URI,
					projection, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(PhoneTable.COLUMN_ID));
					String phoneNumber = cursor.getString(cursor
							.getColumnIndexOrThrow(PhoneTable.COLUMN_PHONE_NUMBER));
					mPhones.put(id, new Phone(id, phoneNumber));
					cursor.moveToNext();
				}
				// always close the cursor
				cursor.close();
			}
		}
		return mPhones;
	}
}
