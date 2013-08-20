package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum DataSize implements Parcelable {
	SMALL,
	NORMAL,
	LARGE;
	
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
	
	public static final Creator<DataSize> CREATOR = new Creator<DataSize>() {
        @Override
        public DataSize createFromParcel(final Parcel source) {
            return DataSize.values()[source.readInt()];
        }
 
        @Override
        public DataSize[] newArray(final int size) {
            return new DataSize[size];
        }
    };
}
