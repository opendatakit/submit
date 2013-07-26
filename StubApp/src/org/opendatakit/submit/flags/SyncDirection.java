package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

public enum SyncDirection implements Parcelable {
	CREATE,
	DOWNLOAD,
	SYNC,
	DELETE
	;

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
	
	public static final Creator<SyncDirection> CREATOR = new Creator<SyncDirection>() {
        @Override
        public SyncDirection createFromParcel(final Parcel source) {
            return SyncDirection.values()[source.readInt()];
        }
 
        @Override
        public SyncDirection[] newArray(final int size) {
            return new SyncDirection[size];
        }
    };

	public void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
	}

}
