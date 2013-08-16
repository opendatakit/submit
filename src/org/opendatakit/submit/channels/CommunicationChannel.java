package org.opendatakit.submit.channels;

import org.opendatakit.submit.flags.DataSize;
import org.opendatakit.submit.flags.Radio;

public class CommunicationChannel {
	long mCostPerMbps;
	DataSize mBandwidth;
	
	public CommunicationChannel(DataSize bandwidth ) {
		mBandwidth = bandwidth;
		// TODO get cost from a properties file
	}
	
	// Getters
	public long getCostPerMbps() {
		return mCostPerMbps;
	}
	
	public DataSize getBandwidth() {
		return mBandwidth;
	}
	
	// Setters
	public void setCostPerMbps(long cost) {
		mCostPerMbps = cost;
	}
}
