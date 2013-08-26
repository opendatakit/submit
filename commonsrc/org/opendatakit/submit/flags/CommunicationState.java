package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * State of the communication based on the
 * result from the dynamically made API call
 * over the available radio. This is ultimately
 * communicated back to the applications connected
 * to SubmitService.
 * 
 * @author mvigil
 *
 */
public enum CommunicationState implements Parcelable {
	SUCCESS,
	FAILURE,
	SEND,
	CHANNEL_UNAVAILABLE,
	IN_PROGRESS,
	WAITING_ON_APP_RESPONSE,
	TIMEOUT;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(ordinal());
	} 
	
	public static final Creator<CommunicationState> CREATOR = new Creator<CommunicationState>() {
        @Override
        public CommunicationState createFromParcel(final Parcel source) {
            return CommunicationState.values()[source.readInt()];
        }
 
        @Override
        public CommunicationState[] newArray(final int size) {
            return new CommunicationState[size];
        }
    };
}
