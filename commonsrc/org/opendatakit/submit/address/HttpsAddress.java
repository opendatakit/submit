package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.HttpFlags;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * TODO needs to be fleshed out. For now just use HttpAddress
 * @author mvigil
 *
 */
public class HttpsAddress extends HttpAddress implements Parcelable {

	/**
	 * TODO
	 * HttpsAddress constructor
	 * @param dest
	 * @throws InvalidAddressException
	 */
	public HttpsAddress(String dest, HttpFlags flag) throws InvalidAddressException {
		super(dest, flag);
	}
	
	/**
	 * HttpsAddress constructor from Parcel
	 * @param in
	 * @throws InvalidAddressException
	 */
	public HttpsAddress(Parcel in) throws InvalidAddressException {
		super(in);
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getAddress());
	}
	
	
	public static final Parcelable.Creator<HttpsAddress> CREATOR =
		    new Parcelable.Creator<HttpsAddress>() {
		        public HttpsAddress createFromParcel(Parcel in) {
		            try {
						return new HttpsAddress(in);
					} catch (InvalidAddressException e) {
						Log.e(HttpsAddress.class.getName(), e.getMessage());
						e.printStackTrace();
					}
		            return null;
		        }

		        public HttpsAddress[] newArray(int size) {
		            return new HttpsAddress[size];
		        }
	};
	
	public void readFromParcel(Parcel in) {
		try {
			this.setAddress(in.readString());
		} catch (InvalidAddressException e) {
			Log.e(HttpsAddress.class.getName(), e.getMessage());
			e.printStackTrace();
		}
	}

}
