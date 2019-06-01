package org.opendatakit.submit.exceptions;

/**
 * MismatchException
 * Thrown when API and Radio flags
 * do not align for proper execution.
 * @author mvigil
 *
 */
public class MismatchException extends Exception {
	/**
	 * Serial id
	 */
	private static final long serialVersionUID = -220924492228612980L;
	public MismatchException() { super(); }
	public MismatchException(String message) { super(message); }
	public MismatchException(Throwable cause) { super(cause); }
	public MismatchException(String message, Throwable cause) { super(message, cause); }
}
