package org.opendatakit.submit.data;

import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.flags.API;

import android.os.Parcel;
import android.os.Parcelable;

public class SendObject implements Parcelable {
	ArrayList<DestinationAddress> mAddresses = null;
	ArrayList<API> mAPIs = null;
	
	// constructors
	public SendObject() {
		// Empty constructor
		mAddresses = new ArrayList<DestinationAddress>();
		mAPIs = new ArrayList<API>();
	}
	
	public SendObject(Parcel in) {
		this();
		readFromParcel(in);
	}
	
	// Getters
	
	public ArrayList<DestinationAddress> getAddresses() {
		return mAddresses;
	}
	
	public ArrayList<API> getAPIs() {
		return mAPIs;
	}
	
	// Setters
	
	public void setAddresses(ArrayList<DestinationAddress> destaddrs) {
		mAddresses = destaddrs;
	}
	
	public void setAPIs(ArrayList<API> apis) {
		mAPIs = apis;
	}
	
	public void addAddress(DestinationAddress destaddr) {
		mAddresses.add(destaddr);
	}
	
	public void addAPI(API api) {
		mAPIs.add(api);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Convert API enums to API strings
		ArrayList<String> strapi = new ArrayList<String>();
		for(API api : mAPIs) {
			strapi.add(api.toString());
		}
		// Write ArrayList<DestinationAddress>
		dest.writeTypedList(mAddresses);
		// Write ArrayList<String> version of mAPIs
		dest.writeStringList(strapi);
	}
	
	public void readFromParcel(Parcel in) {
		mAPIs = new ArrayList<API>();
		mAddresses = new ArrayList<DestinationAddress>();
		
		ArrayList<String> strapi = new ArrayList<String>();
		// Read ArrayList<DestinationAddress>
		in.readTypedList(mAddresses, DestinationAddress.CREATOR);
		// Read ArrayList<String> representation of APIs
		in.readStringList(strapi);
		
		// Convert API strings to API enum
		for(String api : strapi) {
			mAPIs.add(API.valueOf(api));
		}
		
	}
	
	public static final Parcelable.Creator<SendObject> CREATOR =
		    new Parcelable.Creator<SendObject>() {
		        public SendObject createFromParcel(Parcel in) {
		            return new SendObject(in);
		        }

		        public SendObject[] newArray(int size) {
		            return new SendObject[size];
		        }
	};
	
}
