package org.opendatakit.submit.communication;

import java.io.IOException;
import java.util.ArrayList;

import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.interfaces.CommunicationInterface;
import org.opendatakit.submit.route.QueuedObject;

import org.opendatakit.submit.stubapi.SubmitAPI;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MessageManager implements CommunicationInterface {

	private RadioAPIMap mRAMap = null;
	private ArrayList<API> mAPIList = null;
	private SubmitAPI mSubmitAPI = null;
	
	public MessageManager(Context context) {
		mRAMap = new RadioAPIMap(context);
		mSubmitAPI = new SubmitAPI();
		
	}

	@Override
	public Object executeTask(QueuedObject queuedobj, Radio radio)
			throws CommunicationException {
		if (mRAMap.keyExists(radio)) {
			// If the activated radio is valid per our schema
			// get the list of associated APIswell
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
			case STUB:
			case SMS:
			case GCM:
			default:
				try {
					return mSubmitAPI.send(queuedobj.getDest(), queuedobj.getPayload(), queuedobj.getUid());
				} catch(MessageException me) {
					Log.e("MessageManager", me.getMessage());
				} catch(IOException ioe) {
					Log.e("MessageManager", ioe.getMessage());
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
