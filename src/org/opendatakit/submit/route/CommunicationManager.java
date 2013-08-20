package org.opendatakit.submit.route;

import java.io.IOException;
import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.data.DataObject;
import org.opendatakit.submit.data.QueuedObject;
import org.opendatakit.submit.data.SendObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.DataSize;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.interfaces.CommunicationInterface;
import org.opendatakit.submit.stubapi.SubmitAPI;

import android.content.Context;
import android.util.Log;

public class CommunicationManager implements CommunicationInterface {
	SubmitObject mSubmitObject = null;
	Radio mRadio = null;
	
	// TODO for testing purposes
	SubmitAPI mSubmitAPI = new SubmitAPI();
	
	public CommunicationManager(Context context) {
		
	}
	
	/**
	 * executeTask
	 * Given a Submit and the active radio,
	 * figure out which API to send the command over
	 * and go ahead and call it.
	 * @param queuedobj
	 * @param radio
	 */
	@Override
	public Object executeTask(SubmitObject submitobj, Radio radio)
			throws CommunicationException {
		SendObject send = submitobj.getAddress();
		API api = whichAPI(submitobj);
		// If the channel is appropriate for the data
		if (dataFitsChannel(radio, submitobj.getData())) {
			// If the application intends to handle the
			// sending itself
			if (send == null) {
				return CommunicationState.PASS_TO_APP;
			}
			switch(api) {
			case STUB:
				return CommunicationState.SUCCESS;
			case SMS:
			case GCM:
			default:
				// TODO other calls to real APIs would go here.
				return CommunicationState.UNAVAILABLE;
			}
		} // The channel is innapropriate for the data
		return CommunicationState.UNAVAILABLE;
	}

	private String getAddress(API api, ArrayList<DestinationAddress> addresses) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean dataFitsChannel(Radio radio, DataObject data) {
		// Here we do not consider order as it is dealt with
		// before being pushed to the CommunicationManager
		DataSize size = data.getDataSize();
		DataSize response = data.getResponseSize();
		boolean reliable = data.isReliable();
		boolean resp_required = data.isResponseRequired();
		if (radio != null) {
			switch (radio) {
			case LOW_BAND_CELL:
				if (size != DataSize.SMALL) {
					return false;
				}
				// TODO add more here if necessary
				return true;
			case HIGH_BAND_CELL:
				// TODO this is where factoring
				// in price will be critical as well
				if (size == DataSize.LARGE) {
					return false;
				}
				if (response == DataSize.LARGE) {
					return false;
				}
			case WIFI:
				return true;
			case P2P_WIFI:
				// TODO for now
				return false;
			case NFC:
				return false;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Select the best API given the current conditions
	 * This is going to be updated based on certain flags
	 * that get added, such as data urgency/priority.
	 * @param apis
	 * @param queuedobj
	 * @return
	 */
	private API whichAPI(SubmitObject submit) {
		// TODO: Add more here when modular APIs are established
		// For now, return STUB API no matter what
		if(submit.getAddress() == null) {
			return API.APP;
		}
		return API.STUB;
	}
	
}
