package org.opendatakit.submit.route;

import org.opendatakit.submit.flags.SyncDirection;
import org.opendatakit.submit.flags.SyncType;

public class QueuedObject {
	
	/* Unique ID */
	private String mUid = null;

	/* Metadata for APIs*/
	private String mDest = null;
	private String mPayload = null;
	private SyncType mSync = null;
	private SyncDirection mDir = null;
	
	/**
	 * Empty constructor
	 */
	public QueuedObject(){};
	
	/**
	 * Constructor for Message QueuedObjects
	 * @param dest
	 * @param payload
	 */
	public QueuedObject( String dest, String payload ) {
		
		mUid = makeUid(dest, payload);
		mDest = dest;
		mPayload = payload;
		
	}
	
	/**
	 * Constructor for Sync QueuedObjects
	 * @param st
	 * @param sd
	 * @param dest
	 * @param payload
	 */
	public QueuedObject( SyncType st, SyncDirection sd, String dest, String payload ) {
		
		mUid = makeUid(dest, payload);
		mDest = dest;
		mPayload = payload;
		mSync = st;
		mDir = sd;
		
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
	
	/*
	 * TODO
	 * This will need to be less hackish one day
	 */
	private String makeUid(String a, String b) {
		long time = System.currentTimeMillis();
		return a+b+Long.toString(time);
	}
}
