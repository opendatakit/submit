package org.opendatakit.submit.exceptions;

/**
 * CommunicationException
 * Thrown when CommunicationManagers are unable 
 * to talk to the APIs.
 * @author mvigil
 *
 */
public class CommunicationException extends Exception {
	public CommunicationException() { super(); }
	public CommunicationException(String message) { super(message); }
	public CommunicationException(Throwable cause) { super(cause); }
	public CommunicationException(String message, Throwable cause) { super(message, cause); }
}