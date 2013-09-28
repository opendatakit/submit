package org.opendatakit.submit.address;

import java.util.ArrayList;
import java.util.HashMap;

import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.HttpFlags;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * HttpAddress
 * @author mvigil
 *
 */
public class HttpAddress extends DestinationAddress implements Parcelable {
	// For multipart HttpEntity
	private HashMap<String,String> mParams = null;
	private HttpFlags mFlag = HttpFlags.PUT;

	/**
	 * HttpAddress constructor
	 * @param dest
	 * @throws InvalidAddressException
	 */
	public HttpAddress(String dest, HttpFlags direction) throws InvalidAddressException {
		super();
		//if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
			mParams = new HashMap<String,String>();
			mFlag = direction;
		//} else {
		//	throw new InvalidAddressException("Invalid URL.");
		//}
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
	
	public HttpFlags getHttpFlag() {
		return mFlag;
	}
	
	@Override
	public String getAddress() {
		return super.getAddress();
	}
	
	@Override
	public void setAddress(String dest) throws InvalidAddressException {
		super.setAddress(dest);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// HttpAddress Address
		dest.writeString(getAddress());
		// HttpFlags
		dest.writeString(mFlag.toString());
		dest.writeMap(mParams);
		// Http number of parameters
		/*dest.writeInt(mParams.size()); // How many pairs are in the HashMap
		if (mParams.size() > 0) {
			for(String key : mParams.keySet()) {
				// TODO: Make this a prettier solution
				ArrayList<String> tuple = new ArrayList<String>();
				tuple.add(key);
				tuple.add(mParams.get(key));
				dest.writeStringList(tuple);
			}
		}*/
	}
	
	public void readFromParcel(Parcel in) {
		try {
			// read address and instantiate
			
			// Read HttpAddress
			setAddress(in.readString());
			// Read HttpFlags
			mFlag = HttpFlags.valueOf(in.readString());
			mParams = in.readHashMap(HashMap.class.getClassLoader());
			//Read number of flags
			/*int paramSize = in.readInt(); // How many pairs are in the HashMap
			if (paramSize > 0) {
				for(int i = 0; i < paramSize; i++) {
					ArrayList<String> tuple = new ArrayList<String>();
					in.readStringList(tuple);
					mParams.put(tuple.get(0), tuple.get(1));
				}
			}*/
		} catch (InvalidAddressException e) {
			Log.e(HttpAddress.class.getName(), e.getMessage());
			e.printStackTrace();
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
	

	
	
}
