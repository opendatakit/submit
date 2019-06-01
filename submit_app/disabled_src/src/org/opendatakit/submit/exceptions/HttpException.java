package org.opendatakit.submit.exceptions;

public class HttpException extends Exception {
	
	public HttpException() { super(); }
	public HttpException(String message) { super(message); }
	public HttpException(Throwable cause) { super(cause); }
	public HttpException(String message, Throwable cause) { super(message, cause); }
}
