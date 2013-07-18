package org.opendatakit.submit.interfaces;

import java.io.IOException;

import org.opendatakit.submit.exceptions.MessageException;

/**
 * MessageInterface
 * Abstract interface to be implemented by all
 * layers of Submit that interact with data flow.
 * @author mvigil
 *
 */
public interface MessageInterface {

	/**
	 * Send a message.
	 * @param msg
	 * 			String representing message to be sent
	 * @param dest
	 * 			String representing phone number or server URI to send to
	 * @throws IOException
	 * @throws MessageException
	 */
	Object send( String dest, String msg ) throws IOException, MessageException;
	
}
