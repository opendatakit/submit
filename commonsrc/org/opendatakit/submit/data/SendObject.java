package org.opendatakit.submit.data;

import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.address.HttpsAddress;
import org.opendatakit.submit.address.SmsAddress;
import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.os.Parcel;
import android.os.Parcelable;

public class SendObject implements Parcelable {
	private String mDataPath = null;
	
	/* This is so idiotic, but the way Android handles the IPC
	 * protocol, I have to do this. If someone wants to reference
	 * option 2 in the email thread, they are welcome to try to implement
	 * it, they are welcome to. For the sake of time, I am hacking via 
	 * option 1.
	 * 
	 * In short, IPC does not do run time type inspection when recreating
	 * objects from a parcel on the "other side." So we must use a concrete 
	 * class type to jump over the interface. So frustrating.
	 * https://groups.google.com/forum/#!topic/android-developers/zLURzdhSqWE
	 */
	private ArrayList<DestinationAddress> mAddresses = null;
	private ArrayList<HttpAddress> mHttpAddresses = null;
	private ArrayList<HttpsAddress> mHttpsAddresses = null;
	private ArrayList<String> mFileLocations = null;

	// constructors
	/**
	 * If you choose to use the empty constructor
	 * you must add file location paths so that
	 * Submit has a way to access them.
	 */
	public SendObject() {
		mHttpAddresses = new ArrayList<HttpAddress>();
		mHttpsAddresses = new ArrayList<HttpsAddress>();
		mAddresses = new ArrayList<DestinationAddress>();
		mFileLocations = new ArrayList<String>();
	}
	
	public SendObject(String datapath) {
		// Empty constructor
		mHttpAddresses = new ArrayList<HttpAddress>();
		mHttpsAddresses = new ArrayList<HttpsAddress>();
		mAddresses = new ArrayList<DestinationAddress>();
		mFileLocations = new ArrayList<String>();
		mFileLocations.add(datapath);
	}
	


	public SendObject(Parcel in) {
		this();
		readFromParcel(in);
		
		// Set up addresses as if they always existed as one list
		mAddresses = new ArrayList<DestinationAddress>();
		mAddresses.addAll(mHttpAddresses);
		mAddresses.addAll(mHttpsAddresses);
	}

	// Getters

	public String getDataPath() {
		return mDataPath;
	}
	
	public ArrayList<DestinationAddress> getAddresses() {
		mAddresses = new ArrayList<DestinationAddress>();
		mAddresses.addAll(mHttpAddresses);
		mAddresses.addAll(mHttpsAddresses);
		//mAddresses.addAll(mSmsAddresses);
		return mAddresses;
	}

	public ArrayList<String> getFilePointers() {
		return mFileLocations;
	}

	// Setters

	public void setDataPath(String path) {
		mFileLocations.add(path);
	}
	

	public void addFilePointers(ArrayList<String> filePointers) {
		mFileLocations.addAll(filePointers);
	}

	public void addAddress(DestinationAddress destaddr) throws InvalidAddressException {
		if (destaddr instanceof HttpAddress) {
			mHttpAddresses.add((HttpAddress)destaddr);
		} else if (destaddr instanceof HttpsAddress) {
			mHttpsAddresses.add((HttpsAddress)destaddr);
		} else {
			throw new InvalidAddressException("This address needs to be of type HttpAddress or HttpsAddress for now. Future versions will update.");
		}
	}

	public void addFilePointer(String filePointer) {
		mFileLocations.add(filePointer);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		// Write ArrayList<HttpAddresses>
		dest.writeTypedList(mHttpAddresses);
		// Write ArrayList<HttpsAddresses>
		dest.writeTypedList(mHttpsAddresses);
		// Write ArrayList<String> of mFileLocations
		dest.writeStringList(mFileLocations);
	}

	public void readFromParcel(Parcel in) {
		
				
		// Read ArrayList<HttpAddress>
		in.readTypedList(mHttpAddresses, HttpAddress.CREATOR);
		// Read ArrayList<HttpsAddress>
		in.readTypedList(mHttpsAddresses, HttpsAddress.CREATOR);
		// Read ArrayList<String> of mFileLocations
		in.readStringList(mFileLocations);

	}

	public static final Parcelable.Creator<SendObject> CREATOR = new Parcelable.Creator<SendObject>() {
		public SendObject createFromParcel(Parcel in) {
			return new SendObject(in);
		}

		public SendObject[] newArray(int size) {
			return new SendObject[size];
		}
	};

}
