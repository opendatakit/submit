package org.opendatakit.submit.data;


import org.opendatakit.submit.flags.DataSize;

import android.os.Parcel;
import android.os.Parcelable;

public class DataPropertiesObject implements Parcelable {
	
	private static final int NUMBER_BOOL_FIELDS = 4; // Number of class fields that are boolean values
	private DataSize mDataSize = DataSize.NORMAL;
	private boolean mReliable = false;
	private boolean mFragmentable = true;
	private boolean mResponseRequired = true;
	private boolean mOrdered = false;
	
	public DataPropertiesObject(DataSize size, boolean reliable, boolean fragmentable, boolean ordered, boolean response, DataSize responsesize) {
		setDataSize(size);
		setReliability(reliable);
		setFragmentability(fragmentable);
		setOrdered(ordered);
		setResponseRequired(response);
	}
	public DataPropertiesObject() {
		// All presets are set
	}
	
	public DataPropertiesObject(Parcel in) {
		readFromParcel(in);
	}
	
	/* Getters */
	public DataSize getDataSize() {
		return mDataSize;
	}
	
	public boolean isReliable() {
		return mReliable;
	}
	
	public boolean isFragmentable() {
		return mFragmentable;
	}
	
	public boolean isOrdered() {
		return mOrdered;
	}
	
	public boolean isResponseRequired() {
		return mResponseRequired;
	}

	
	/* Setters */
	public void setDataSize(DataSize size) {
		mDataSize = size;
	}
	
	public void setReliability(boolean reliable) {
		mReliable = reliable;
	}
	
	public void setFragmentability(boolean fragmentable) {
		mFragmentable = fragmentable;
	}
	
	public void setOrdered(boolean ordered) {
		mOrdered = ordered;
	}
	
	public void setResponseRequired(boolean response) {
		mResponseRequired = response;
	}
	
	
	/* Parcelable */
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		boolean[] booleanFields = new boolean[NUMBER_BOOL_FIELDS];
		booleanFields[0] = mReliable;
		booleanFields[1] = mFragmentable;
		booleanFields[2] = mOrdered;
		booleanFields[3] = mResponseRequired;
		dest.writeString(mDataSize.toString());
		dest.writeBooleanArray(booleanFields);
		
	}
	
	/* For constructor */
	public void readFromParcel(Parcel in) {
		
		// Important this is in the same order that Parcel is written
		// in writeToParcel()
		setDataSize(DataSize.valueOf(in.readString()));
		
		boolean[] booleanFields = new boolean[NUMBER_BOOL_FIELDS];
		in.readBooleanArray(booleanFields);
		setReliability(booleanFields[0]);
		setFragmentability(booleanFields[1]);
		setOrdered(booleanFields[2]);
		setResponseRequired(booleanFields[3]);
		
	}
	
	public static final Parcelable.Creator<DataPropertiesObject> CREATOR =
		    new Parcelable.Creator<DataPropertiesObject>() {
		        public DataPropertiesObject createFromParcel(Parcel in) {
		            return new DataPropertiesObject(in);
		        }

		        public DataPropertiesObject[] newArray(int size) {
		            return new DataPropertiesObject[size];
		        }
		    };
}
