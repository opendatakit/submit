package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MessageType
 * Type MESSAGE can come in a variety of types.
 * @author mvigil
 *
 */
public enum MessageType implements Parcelable {
	SMS, // Short Message Service
	GCM; // Google Cloud Message

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
	
	public static final Creator<MessageType> CREATOR = new Creator<MessageType>() {
        @Override
        public MessageType createFromParcel(final Parcel source) {
            return MessageType.values()[source.readInt()];
        }
 
        @Override
        public MessageType[] newArray(final int size) {
            return new MessageType[size];
        }
    };
    
    public void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
	}
}
