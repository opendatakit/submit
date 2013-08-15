package org.opendatakit.submit.data;

import java.util.Date;

/**
 * Data object in submit
 * that contains the DataObject
 * in the SubmitServer context
 * @author mvigil
 *
 */
public class SubmitObject {
	private String mAppID = null;
	private String mSubmitID = null;
	private DataObject mData = null;
	private SendObject mAddress = null;
	
	public SubmitObject(String appID, DataObject data, SendObject addr) {
		// For the SubmitID
		Date date = new Date();
		mAppID = appID;
		mData = data;
		mAddress = addr;
		mSubmitID = Integer.toString(System.identityHashCode(data)) + Long.toString(date.getTime());
	}
	
	// Getters
	public DataObject getData() {
		return mData;
	}
	
	public SendObject getAddress() {
		return mAddress;
	}
	
	public String getSubmitID() {
		return mSubmitID;
	}
	
	public String getAppID() {
		return mAppID;
	}
	
}
