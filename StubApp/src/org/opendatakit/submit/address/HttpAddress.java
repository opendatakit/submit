package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.webkit.URLUtil;

public class HttpAddress extends DestinationAddress {

	// Constructor
	public HttpAddress(String dest) throws InvalidAddressException {
		super();
		if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid URL.");
		}
	}
	
	@Override
	public void setAddress(String dest) throws InvalidAddressException {
		if(URLUtil.isValidUrl(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid URL.");
		}
	}
}
