package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class SmsAddress extends DestinationAddress {

	/**
	 * SmsAddress constructor from String
	 * @param dest
	 * @throws InvalidAddressException
	 */
	public SmsAddress(String dest) throws InvalidAddressException {
		// Verify phone number
		super();
		/*
		 * Note: PhoneNumberUtils recognizes
		 * a valid phone number as one in a
		 * ###-###-#### format
		 */
		if(PhoneNumberUtils.isGlobalPhoneNumber(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid global phone number. Must be in ###-###-#### format");
		}
	}
	
	/**
	 * SmsAddress constructor from Parcel
	 * @param in
	 * @throws InvalidAddressException
	 */
	public SmsAddress(Parcel in) throws InvalidAddressException {
		// Verify phone number
		super();
		String dest = in.readString();
		/*
		 * Note: PhoneNumberUtils recognizes a valid phone number as one in a
		 * ###-###-#### format
		 */
		if (PhoneNumberUtils.isGlobalPhoneNumber(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException(
					"Invalid global phone number. Must be in ###-###-#### format");
		}
	}
	
	@Override
	public void setAddress(String dest) throws InvalidAddressException {
		if(PhoneNumberUtils.isGlobalPhoneNumber(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid global phone number. Must be in ###-###-#### format");
		}
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getAddress());
	}
	
	public static final Parcelable.Creator<SmsAddress> CREATOR =
		    new Parcelable.Creator<SmsAddress>() {
		        public SmsAddress createFromParcel(Parcel in) {
		            try {
						return new SmsAddress(in);
					} catch (InvalidAddressException e) {
						Log.e(SmsAddress.class.getName(), e.getMessage());
						e.printStackTrace();
					}
		            return null;
		        }

		        public SmsAddress[] newArray(int size) {
		            return new SmsAddress[size];
		        }
	};
	
	public void readFromParcel(Parcel in) {
		try {
			this.setAddress(in.readString());
		} catch (InvalidAddressException e) {
			Log.e(SmsAddress.class.getName(), e.getMessage());
			e.printStackTrace();
		}
	}

}
