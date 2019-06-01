package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

public enum HttpFlags implements Parcelable {
	POST,
	PUT,
	GET,
	DELETE,
	HEAD;

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
	
	public static final Creator<HttpFlags> CREATOR = new Creator<HttpFlags>() {
        @Override
        public HttpFlags createFromParcel(final Parcel source) {
            return HttpFlags.values()[source.readInt()];
        }
 
        @Override
        public HttpFlags[] newArray(final int size) {
            return new HttpFlags[size];
        }
    };

	public void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
	}

}
