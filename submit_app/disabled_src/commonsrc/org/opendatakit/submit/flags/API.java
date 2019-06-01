package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * API
 * List of API resources available to use with Submit.
 * 
 * NOTE: If third-party developer wishes to add support
 * for another API, a corresponding value needs to 
 * be defined HERE if they wish for it to be recognized.
 * @author mvigil
 * 
 */
public enum API implements Parcelable {
	SMS, // TODO SMS for messages only
	GCM, // TODO GCM for messages only
	ODKv2, // ODKv2 for stored data
	STUB,
	APP, 
	APACHE_HTTP,
	APACHE_HTTPS; // for testing

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ordinal());
	}
	
	public static final Creator<API> CREATOR = new Creator<API>() {
        @Override
        public API createFromParcel(final Parcel source) {
            return API.values()[source.readInt()];
        }
 
        @Override
        public API[] newArray(final int size) {
            return new API[size];
        }
    };
}
