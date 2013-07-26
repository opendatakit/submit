package org.opendatakit.submit.flags;

/**
 * Types
 * Types of "things" being submitted.
 * @author mvigil
 *
 */
public enum Types {
	MESSAGE, // Messages that are not committed to memory
	SYNC; // Stored data that must be synchronized across devices and servers
}
