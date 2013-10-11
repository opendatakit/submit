package org.opendatakit.submit.data;

import java.util.Date;

import org.opendatakit.submit.flags.CommunicationState;

/**
 * Data object in submit
 * that contains the DataPropertiesObject
 * in the SubmitServer context
 * @author mvigil
 *
 */
public class SubmitObject {
	private String mAppID = null;
	private String mSubmitID = null;
	private DataPropertiesObject mData = null;
	private SendObject mAddress = null;
	private CommunicationState mState = null;
	private int mCode = -1;
	
	public SubmitObject(String appID, DataPropertiesObject data, SendObject addr) {
		// For the SubmitID
		Date date = new Date();
		mAppID = appID;
		mData = data;
		mAddress = addr;
		mSubmitID = Integer.toString(System.identityHashCode(data)) + Long.toString(date.getTime());
		mState = CommunicationState.CHANNEL_UNAVAILABLE;
	}
	
	// Getters
	public DataPropertiesObject getData() {
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
	
	public CommunicationState getState() {
		return mState;
	}

	// Setters
	public void setState(CommunicationState state) {
		mState = state;
	}

	public void setCode(int code) {
		mCode = code;
		
	}

	public int getCode() {
		// TODO Auto-generated method stub
		return mCode;
	}
	
}
