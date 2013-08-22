package org.opendatakit.submit.flags;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/**
 * DataSize pertaining to what is going 
 * over the channels. These sizes are
 * relative, with the idea that developers
 * are more likely to understand the sizes
 * of data being sent over the channels. 
 *		-NORMAL sized data will typically be
 * 		considered for sending over WIFI and
 * 		HIGH_BAND_CELL radios.
 * 		-SMALL sized data will be sent over
 * 		any channel and is the only size that
 * 		will be sent over LOW_BAND_CELL. Functionally,
 * 		there is a lower-bound on the size of SMALL
 * 		data (140 characters).
 * 		-LARGE sized data is only sent over WIFI or
 * 		HIGH_BAND_CELLULAR channels if the priority is high enough
 * 		and there are sufficient financial resources.
 * @author mvigil
 *
 */
public enum DataSize implements Parcelable {
	SMALL,
	NORMAL,
	LARGE;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ordinal());
	} 
	
	public static final Creator<DataSize> CREATOR = new Creator<DataSize>() {
        @Override
        public DataSize createFromParcel(final Parcel source) {
            return DataSize.values()[source.readInt()];
        }
 
        @Override
        public DataSize[] newArray(final int size) {
            return new DataSize[size];
        }
    };
    
}
