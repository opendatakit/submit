package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SyncType
 * Type SYNC has several types
 * @author mvigil
 *
 */
public enum SyncType implements Parcelable {
	DATABASE,
	FILESYSTEM,
	KEYVAL;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(ordinal());
	} 
	
	public static final Creator<SyncType> CREATOR = new Creator<SyncType>() {
        @Override
        public SyncType createFromParcel(final Parcel source) {
            return SyncType.values()[source.readInt()];
        }
 
        @Override
        public SyncType[] newArray(final int size) {
            return new SyncType[size];
        }
    };

	public void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
	}
}
