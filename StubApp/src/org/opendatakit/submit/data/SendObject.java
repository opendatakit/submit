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
	}
	
	public SendObject(Parcel in) {
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
		DestinationAddress[] addrarray = new DestinationAddress[mAddresses.size()];
		int position = 0;
		
		// Go through all addresses and write them
		// into a Parcelable form
		for(DestinationAddress addr : mAddresses) {
			addrarray[position] = addr;
			position++;
		}
		dest.writeParcelableArray(addrarray, flags);
		
		// Go through all APIs and write them
		// into a Parcelable form
		API[] apiarray = new API[mAPIs.size()];
		position = 0;
		for(API api : mAPIs) {
			apiarray[position] = api;
			position++;
		}
		dest.writeParcelableArray(apiarray, flags);
	}
	
	public void readFromParcel(Parcel in) {
		DestinationAddress[] addrarray = null;
		API[] apiarray = null;
		
		addrarray = (DestinationAddress[])in.readParcelableArray(null);
		apiarray = (API[])in.readParcelableArray(null);
		
		for(DestinationAddress addr : addrarray) {
			mAddresses.add(addr);
		}
		
		for(API api : apiarray) {
			mAPIs.add(api);
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
