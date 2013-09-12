package org.opendatakit.submit.data;

import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;

import android.os.Parcel;
import android.os.Parcelable;

public class SendObject implements Parcelable {
	private String mDataPath = null;
	private ArrayList<DestinationAddress> mAddresses = null;
	private ArrayList<String> mFileLocations = null;

	// constructors
	public SendObject(String datapath) {
		// Empty constructor
		mDataPath = datapath;
		mAddresses = new ArrayList<DestinationAddress>();
		mFileLocations = new ArrayList<String>();
	}

	public SendObject(Parcel in) {
		this(in.readString());
		readFromParcel(in);
	}

	// Getters

	public String getDataPath() {
		return mDataPath;
	}
	
	public ArrayList<DestinationAddress> getAddresses() {
		return mAddresses;
	}

	public ArrayList<String> getFilePointers() {
		return mFileLocations;
	}

	// Setters

	public void setDataPath(String path) {
		mDataPath = path;
	}
	
	public void addAddresses(ArrayList<DestinationAddress> destaddrs) {
		mAddresses.addAll(destaddrs);
	}

	public void addFilePointers(ArrayList<String> filePointers) {
		mFileLocations.addAll(filePointers);
	}

	public void addAddress(DestinationAddress destaddr) {
		mAddresses.add(destaddr);
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
		// Write mDataPath
		dest.writeString(mDataPath);
		// Write ArrayList<DestinationAddress>
		dest.writeTypedList(mAddresses);
		// Write ArrayList<String> of mFileLocations
		dest.writeStringList(mFileLocations);
	}

	public void readFromParcel(Parcel in) {
		mAddresses = new ArrayList<DestinationAddress>();
		mFileLocations = new ArrayList<String>();
		// Read mDataPath
		mDataPath = in.readString();
				
		// Read ArrayList<DestinationAddress>
		in.readTypedList(mAddresses, DestinationAddress.CREATOR);
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
