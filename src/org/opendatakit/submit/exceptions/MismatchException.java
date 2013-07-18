package org.opendatakit.submit.exceptions;

/**
 * MismatchException
 * Thrown when API and Radio flags
 * do not align for proper execution.
 * @author mvigil
 *
 */
public class MismatchException extends Exception {
	public MismatchException() { super(); }
	public MismatchException(String message) { super(message); }
	public MismatchException(Throwable cause) { super(cause); }
	public MismatchException(String message, Throwable cause) { super(message, cause); }
}
