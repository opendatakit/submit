package org.opendatakit.submit.route;

import org.opendatakit.submit.flags.SyncDirection;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.flags.Types;

/**
 * QueuedObject
 * Class representing an application's submission
 * with enough metadata to use so that the CommunicationManager
 * can direct the appropriate API to handle it.
 * 
 * @author mvigil
 *
 */
public class QueuedObject {
	
	/* Unique ID */
	private String mUid = null;

	/* Metadata for APIs*/
	private Types mType = null;
	private String mDest = null;
	private String mPayload = null;
	private SyncType mSync = null;
	private SyncDirection mDir = null;
	
	
	/**
	 * Constructor for Message QueuedObjects
	 * @param dest
	 * @param payload
	 */
	public QueuedObject( String dest, String payload, String uid ) {
		
		mUid = uid;
		mDest = dest;
		mPayload = payload;
		mType = Types.MESSAGE;
		
	}
	
	/**
	 * Constructor for Sync QueuedObjects
	 * @param st
	 * @param sd
	 * @param dest
	 * @param payload
	 */
	public QueuedObject( SyncType st, SyncDirection sd, String dest, String payload, String uid ) {
		
		mUid = uid;
		mDest = dest;
		mPayload = payload;
		mSync = st;
		mDir = sd;
		mType = Types.SYNC;
		
	}
	
	/**
	 * Get UID of QueuedObject
	 * @return
	 */
	public String getUid() {
		return mUid;
	}
	
	/**
	 * Get destination of QueuedObject
	 * (phone number or server uri)
	 * @return
	 */
	public String getDest() {
		return mDest;
	}
	
	/**
	 * Get the payload of QueuedObject
	 * @return
	 */
	public String getPayload() {
		return mPayload;
	}
	
	/**
	 * Get SyncType of QueuedObject 
	 * if it is a Sync'd object
	 * @return
	 */
	public SyncType getSyncType() {
		return mSync;
	}
	
	/**
	 * Get the SyncDirection of QueuedObject
	 * if it is a Sync'd object
	 * @return
	 */
	public SyncDirection getDirection() {
		return mDir;
	}
	
	/**
	 * Get the Types if QueuedObject
	 * @return
	 */
	public Types getType() {
		switch(mType) {
		case SYNC:
			return Types.SYNC;
		default:
			break;	
		}
		return Types.MESSAGE;
	}
}
