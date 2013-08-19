package org.opendatakit.submit.address;


import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DestinationAddress implements Parcelable {
	
	private String mAddress;
	private static final String TAG = "DestinationAddress";
	
	// constructor
	public DestinationAddress() {
		// Empty constructor
	}
	
	public DestinationAddress(Parcel in) {
		readFromParcel(in);
	}
	
	// Getters
	public String getAddress() {
		return mAddress;
	}
	
	// Setters
	public void setAddress(String dest) throws InvalidAddressException {
		mAddress = dest;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mAddress);
	}
	
	public void readFromParcel(Parcel in) {
		try {
			setAddress(in.readString());
		} catch (InvalidAddressException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public static final Parcelable.Creator<DestinationAddress> CREATOR =
		    new Parcelable.Creator<DestinationAddress>() {
		        public DestinationAddress createFromParcel(Parcel in) {
		            return new DestinationAddress(in);
		        }

		        public DestinationAddress[] newArray(int size) {
		            return new DestinationAddress[size];
		        }
	};
}
