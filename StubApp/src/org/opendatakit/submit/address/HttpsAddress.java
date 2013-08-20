package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;

/**
 * TODO needs to be fleshed out. For now just use HttpAddress
 * @author mvigil
 *
 */
public class HttpsAddress extends HttpAddress {

	
	public HttpsAddress(String dest) throws InvalidAddressException {
		super(dest);
	}

}
