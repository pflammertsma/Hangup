package com.pixplicity.hangup;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pixplicity.hangup.db.Phone;
import com.pixplicity.hangup.db.PhoneProvider;
import com.pixplicity.hangup.db.PhoneTable;

public class PhoneAdapter extends BaseAdapter {

	private static class ViewHolder {

		public EditText etPhone;
		public ImageButton ibDelete;
		public Button btAdd;

	}

	private Context mContext;
	private ArrayList<Phone> mData;
	private LayoutInflater mInflater;

	public PhoneAdapter(Context context, ArrayList<Phone> data) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mData = data;
	}

	@Override
	public int getCount() {
		// Extra row for add button
		return mData.size() + 1;
	}

	@Override
	public Phone getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder = null;
		if (rowView != null) {
			holder = (ViewHolder) rowView.getTag();
			if (holder.btAdd != null && position < getCount() - 1) {
				// Change an 'add' row into an 'edit' row
				rowView = null;
			} else if (holder.btAdd == null && position == getCount() - 1) {
				// Change an 'edit' row into an 'add' row
				rowView = null;
			}
		}
		if (rowView == null) {
			int resId = R.layout.in_row_phone;
			if (position == getCount() - 1)
				resId = R.layout.in_row_add;
			rowView = mInflater.inflate(resId, null);
			holder = new ViewHolder();
			holder.etPhone = (EditText) rowView.findViewById(R.id.et_phone_number);
			holder.ibDelete = (ImageButton) rowView.findViewById(R.id.ib_delete);
			holder.btAdd = (Button) rowView.findViewById(R.id.bt_add);
			if (holder.etPhone != null) {
				holder.etPhone.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

					@Override
					public void afterTextChanged(Editable s) {
						// Update the current entry
						Phone phone = getItem(position);
						phone.setPhoneNumber(s.toString());
						((MainActivity) mContext).update(phone);
					}
				});
			}
			if (holder.ibDelete != null) {
				holder.ibDelete.setTag(position);
				holder.ibDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Insert a new, empty row
						Uri uri = Uri.parse(PhoneProvider.CONTENT_URI + "/"
								+ getItem(position).getId());
						mContext.getContentResolver().delete(uri, null, null);
					}
				});
			}
			if (holder.btAdd != null) {
				holder.btAdd.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Insert a new, empty row
						ContentValues values = new ContentValues();
						values.put(PhoneTable.COLUMN_PHONE_NUMBER, "");
						mContext.getContentResolver().insert(PhoneProvider.CONTENT_URI, values);
					}
				});
			}
			rowView.setTag(holder);
		}
		if (position < getCount() - 1) {
			holder.etPhone.setText(getItem(position).getPhoneNumber());
		}
		return rowView;
	}

}
