package org.opendatakit.submit.channels;

import org.opendatakit.submit.flags.DataSize;

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
