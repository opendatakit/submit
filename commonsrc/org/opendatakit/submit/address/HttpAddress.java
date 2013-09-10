package org.opendatakit.submit.address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.URLUtil;

public class HttpAddress extends DestinationAddress implements Parcelable {
	// For multipart HttpEntity
	private HashMap<String,String> mParams = null;

	/**
	 * HttpAddress constructor
	 * @param dest
	 * @throws InvalidAddressException
	 */
	public HttpAddress(String dest) throws InvalidAddressException {
		super();
		if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
			mParams = new HashMap<String,String>();
		} else {
			throw new InvalidAddressException("Invalid URL.");
		}
	}
	
	/**
	 * Add a parameter for multi-part Http Requests
	 * @param param
	 * @param value
	 */
	public void addHeader(String param, String value) {
		mParams.put(param, value);
	}
	
	/**
	 * Get the HashMap of all the parans
	 * for multi-part Http Requests
	 * @return
	 */
	public HashMap<String,String> getHeaders() {
		return mParams;
	}
	
	/**
	 * HttpAddress constructor from Parcel
	 * @param in
	 * @throws InvalidAddressException
	 */
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
		dest.writeInt(mParams.size()); // How many pairs are in the HashMap
		for(String key : mParams.keySet()) {
			ArrayList<String> tuple = new ArrayList<String>();
			tuple.add(key);
			tuple.add(mParams.get(key));
			dest.writeStringList(tuple);
		}
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
			int paramSize = in.readInt(); // How many pairs are in the HashMap
			for(int i = 0; i < paramSize; i++) {
				ArrayList<String> tuple = new ArrayList<String>();
				in.readArrayList(String.class.getClassLoader());
				mParams.put(tuple.get(0), tuple.get(1));
			}
		} catch (InvalidAddressException e) {
			Log.e(HttpAddress.class.getName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
}
