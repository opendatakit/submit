package org.opendatakit.submit.communication;


import java.io.IOException;
import java.util.ArrayList;

import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.interfaces.CommunicationInterface;
import org.opendatakit.submit.route.QueuedObject;
import org.opendatakit.submit.stubapi.SubmitAPI;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class SyncManager implements CommunicationInterface {

	private RadioAPIMap mRAMap = null;
	private ArrayList<API> mAPIList = null;
	private SubmitAPI mSubmitAPI = null;
	
	public SyncManager(SharedPreferences sp) {
		mRAMap = new RadioAPIMap();
		mSubmitAPI = new SubmitAPI();
	}
	
	/**
	 * executeTask
	 * Given a QueuedObject and the active radio,
	 * figure out which API to send the command over
	 * and go ahead and call it.
	 * @param queuedobj
	 * @param radio
	 */
	@Override
	public Object executeTask(QueuedObject queuedobj, Radio radio)
			throws CommunicationException {
		if (mRAMap.keyExists(radio)) {
			// If the activated radio is valid per our schema
			// get the list of associated APIs
			mAPIList = mRAMap.getValue(radio);
		} else {
			// else, null list of APIs
			mAPIList = null;
		}
		
		// Now, we have an active radio and a list of 
		// APIs as well as a QueuedObject...
		// what API should we use??
		if (mAPIList != null) {
			API api = whichAPI(mAPIList, queuedobj);
			switch(api) {
			case ODKv2:
				switch(queuedobj.getDirection()) {
				
				case CREATE:
				case DOWNLOAD:
				case SYNC:
				case DELETE:
				default:
					// TODO
					break;
				}
			case STUB:
			default:
				switch (queuedobj.getDirection()) {
				case CREATE:
					try {
						return mSubmitAPI.create(queuedobj.getSyncType(),
								queuedobj.getDest(), queuedobj.getPayload(), queuedobj.getUid());
					} catch (SyncException se) {
						Log.e("SyncManager", se.getMessage());
					} catch (IOException ioe) {
						Log.e("SyncManager", ioe.getMessage());
					}

				case DOWNLOAD:
					try {
						return mSubmitAPI.download(queuedobj.getSyncType(),
								queuedobj.getDest(), queuedobj.getPayload(), queuedobj.getUid());
					} catch (SyncException se) {
						Log.e("SyncManager", se.getMessage());
					} catch (IOException ioe) {
						Log.e("SyncManager", ioe.getMessage());
					}
				case SYNC:
					try {
						return mSubmitAPI.sync(queuedobj.getSyncType(),
								queuedobj.getDest(), queuedobj.getPayload(), queuedobj.getUid());
					} catch (SyncException se) {
						Log.e("SyncManager", se.getMessage());
					} catch (IOException ioe) {
						Log.e("SyncManager", ioe.getMessage());
					}
				case DELETE:
					try {
						return mSubmitAPI.delete(queuedobj.getSyncType(),
								queuedobj.getDest(), queuedobj.getPayload(), queuedobj.getUid());
					} catch (SyncException se) {
						Log.e("SyncManager", se.getMessage());
					} catch (IOException ioe) {
						Log.e("SyncManager", ioe.getMessage());
					}
				default:
					break;

				}
			}
		}
		return null;
	}
	
	/**
	 * Select the best API given the current conditions
	 * This is going to be updated based on certain flags
	 * that get added, such as data urgency/priority.
	 * @param apis
	 * @param queuedobj
	 * @return
	 */
	private API whichAPI(ArrayList<API> apis, QueuedObject queuedobj) {
		// TODO: Add more here when modular APIs are established
		// For now, return STUB API no matter what
		return API.STUB;
	}
}
