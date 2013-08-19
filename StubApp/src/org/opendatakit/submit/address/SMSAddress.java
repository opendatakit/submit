package org.opendatakit.submit.address;

import org.opendatakit.submit.exceptions.InvalidAddressException;

import android.telephony.PhoneNumberUtils;

public class SMSAddress extends DestinationAddress {

	public SMSAddress(String dest) throws InvalidAddressException {
		// Verify phone number
		super();
		/*
		 * Note: PhoneNumberUtils recognizes
		 * a valid phone number as one in a
		 * ###-###-#### format
		 */
		if(PhoneNumberUtils.isGlobalPhoneNumber(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid global phone number. Must be in ###-###-#### format");
		}
	}
	
	@Override
	public void setAddress(String dest) throws InvalidAddressException {
		if(PhoneNumberUtils.isGlobalPhoneNumber(dest)) {
			super.setAddress(dest);
		} else {
			throw new InvalidAddressException("Invalid global phone number. Must be in ###-###-#### format");
		}
	}

}
