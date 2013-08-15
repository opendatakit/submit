package org.opendatakit.submit.data;


import org.opendatakit.submit.flags.DataSize;

import android.os.Parcel;
import android.os.Parcelable;

public class DataObject implements Parcelable {
	
	private static final int NUMBER_BOOL_FIELDS = 4; // Number of class fields that are boolean values
	private String mPath = null;
	private DataSize mDataSize = DataSize.NORMAL;
	private boolean mReliable = false;
	private boolean mFragmentable = true;
	private boolean mResponseRequired = true;
	private boolean mOrdered = false;
	private DataSize mResponseSize = DataSize.NORMAL;
	
	public DataObject(String path, DataSize size, boolean reliable, boolean fragmentable, boolean ordered, boolean response, DataSize responsesize) {
		setDataSize(size);
		setReliability(reliable);
		setFragmentability(fragmentable);
		setOrdered(ordered);
		setResponseRequired(response);
		setResponseSize(responsesize);
	}
	public DataObject() {
		// All presets are set
	}
	
	public DataObject(Parcel in) {
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
	
	public DataSize getResponseSize() {
		return mResponseSize;
	}
	
	public String getDataPath() {
		return mPath;
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
	
	public void setResponseSize(DataSize size) {
		mResponseSize = size;
	}
	
	public void setDataPath(String path) {
		mPath = path;
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
		
		dest.writeString(mPath);
		dest.writeParcelable(mDataSize, flags);
		dest.writeBooleanArray(booleanFields);
		dest.writeParcelable(mResponseSize, flags);
		
	}
	
	/* For constructor */
	public void readFromParcel(Parcel in) {
		
		// Important this is in the same order that Parcel is written
		// in writeToParcel()
		setDataPath(in.readString());
		setDataSize((DataSize)in.readParcelable(null));
		
		boolean[] booleanFields = new boolean[NUMBER_BOOL_FIELDS];
		in.readBooleanArray(booleanFields);
		setReliability(booleanFields[0]);
		setFragmentability(booleanFields[1]);
		setOrdered(booleanFields[2]);
		setResponseRequired(booleanFields[3]);
		
		setResponseSize((DataSize)in.readParcelable(null));
	}
	
	public static final Parcelable.Creator<DataObject> CREATOR =
		    new Parcelable.Creator<DataObject>() {
		        public DataObject createFromParcel(Parcel in) {
		            return new DataObject(in);
		        }

		        public DataObject[] newArray(int size) {
		            return new DataObject[size];
		        }
		    };
}
