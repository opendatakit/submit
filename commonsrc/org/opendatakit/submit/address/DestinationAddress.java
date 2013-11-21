package org.opendatakit.submit.address;


import org.opendatakit.submit.exceptions.InvalidAddressException;

public abstract class DestinationAddress {

	private String mAddress;
	private static final String TAG = "DestinationAddress";

	/**
	 * DestinationAddress constructor
	 */
	public DestinationAddress() {
		// Empty constructor
	}



	// Getters
	/**
	 * Get the URI or telephone number
	 * @return mAddress
	 */
	public String getAddress() {
		return mAddress;
	}

	// Setters
	/**
	 * Set the URI or telephone number to dest
	 * @param dest
	 * @throws InvalidAddressException
	 */
	public void setAddress(String dest) throws InvalidAddressException {
		mAddress = dest;
	}

}
