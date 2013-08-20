package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.URLUtil;

public class HttpAddress extends DestinationAddress implements Parcelable {

	// Constructor
	public HttpAddress(String dest) throws InvalidAddressException {
		super();
		if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid URL.");
		}
	}
	
	public HttpAddress(Parcel in) throws InvalidAddressException {
		super();
		readFromParcel(in);
	}
	
	@Override
	public void setAddress(String dest) throws InvalidAddressException {
		if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid URL.");
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getAddress());
	}
	
	public static final Parcelable.Creator<HttpAddress> CREATOR =
		    new Parcelable.Creator<HttpAddress>() {
		        public HttpAddress createFromParcel(Parcel in) {
		            try {
						return new HttpAddress(in);
					} catch (InvalidAddressException e) {
						Log.e(HttpAddress.class.getName(), e.getMessage());
						e.printStackTrace();
					}
		            return null;
		        }

		        public HttpAddress[] newArray(int size) {
		            return new HttpAddress[size];
		        }
	};
	
	public void readFromParcel(Parcel in) {
		try {
			this.setAddress(in.readString());
		} catch (InvalidAddressException e) {
			Log.e(HttpAddress.class.getName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
}
