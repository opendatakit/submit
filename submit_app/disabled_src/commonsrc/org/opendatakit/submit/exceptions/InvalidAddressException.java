package org.opendatakit.submit.exceptions;

public class InvalidAddressException extends Exception {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = -9123396666632013349L;
	public InvalidAddressException() { super(); }
	public InvalidAddressException(String message) { super(message); }
	public InvalidAddressException(Throwable cause) { super(cause); }
	public InvalidAddressException(String message, Throwable cause) { super(message, cause); }
}
