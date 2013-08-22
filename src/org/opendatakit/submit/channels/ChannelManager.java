package org.opendatakit.submit.channels;

import org.opendatakit.submit.flags.DataSize;
import org.opendatakit.submit.flags.Radio;

public class ChannelManager {
	private long mCostPerMbps;
	private double mBandwidth;
	private boolean mEnabled = false;
	
	public ChannelManager(double bandwidth ) {
		mBandwidth = bandwidth;
		// TODO get cost from a properties file
	}
	
	// Getters
	public long getCostPerMbps() {
		return mCostPerMbps;
	}
	
	public double getBandwidth() {
		return mBandwidth;
	}
	
	public void enable() {
		mEnabled = true;
	}
	
	public void disable() {
		mEnabled = false;
	}
	
	// Setters
	public void setCostPerMbps(long cost) {
		mCostPerMbps = cost;
	}
}
