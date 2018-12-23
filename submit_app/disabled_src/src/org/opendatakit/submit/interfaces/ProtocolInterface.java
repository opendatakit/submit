package org.opendatakit.submit.interfaces;

import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.CommunicationState;

public interface ProtocolInterface {
	public CommunicationState uploadData() throws InvalidAddressException;
}
