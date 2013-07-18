package org.opendatakit.submit.exceptions;

/**
 * SyncException
 * Thrown when something goes awry during
 * the synchronization of stored data:
 * create, download, sync, delete
 * @author mvigil
 *
 */
public class SyncException extends Exception {
	public SyncException() { super(); }
	public SyncException(String message) { super(message); }
	public SyncException(Throwable cause) { super(cause); }
	public SyncException(String message, Throwable cause) { super(message, cause); }
}
