package com.pixplicity.hangup.db;


public class Phone {

	private int mId;
	private String mPhoneNumber;

	public Phone(int id, String phoneNumber) {
		setId(id);
		setPhoneNumber(phoneNumber);
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.mPhoneNumber = phoneNumber;
	}

	@Override
	public String toString() {
		return mPhoneNumber;
	}

}
