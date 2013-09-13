package org.opendatakit.submit.route;

import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.address.HttpsAddress;
import org.opendatakit.submit.address.SmsAddress;
import org.opendatakit.submit.data.DataPropertiesObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.DataSize;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.service.SubmitService;
import org.opendatakit.submit.stubapi.SubmitAPI;

import android.util.Log;

/**
 * Routes SubmitObjects based on data properties
 * and available channel properties and API compatibility.
 * @author mvigil
 *
 */
public class CommunicationManager {
	private String TAG = CommunicationManager.class.getName();
	private SubmitObject mSubmitObject = null;
	private Radio mRadio = null;
	private SendManager mSender = null;
	private SubmitService mSubmitService = null;
	
	// TODO for testing purposes
	SubmitAPI mSubmitAPI = new SubmitAPI();
	
	/**
	 * Constructor from Context
	 * @param context
	 */
	public CommunicationManager(SubmitService subserv) {
		mSender = new SendManager(this);
		mSubmitService = subserv;
	}
	
	/**
	 * Callback that directs back to SubmitService
	 * and updates SubmitObject with the newest state
	 * @param submit
	 */
	public void resultState(SubmitObject submit) {
		mSubmitService.resultState(submit);
	}
	
	/**
	 * executeTask
	 * Given a Submit and the active radio,
	 * figure out which API to send the command over
	 * and go ahead and call it.
	 * @param queuedobj
	 * @param radio
	 */
	public Object route(SubmitObject submitobj, Radio radio) {
		mRadio = radio;
		// Look at the SubmitObject's current state
		switch(submitobj.getState()) {
			case CHANNEL_UNAVAILABLE:
				if(dataFitsChannel(mRadio, submitobj.getData())) {
					// If the channel is ready to send the data
					// recursively call executeTask() on the SubmitObject
					// with the CommunicationState set to SEND
					submitobj.setState(CommunicationState.SEND);
					return CommunicationState.SEND;//route(submitobj, radio); // Bypass SubmitService
				}
			case SUCCESS:
			case FAILURE_RETRY:
				return submitobj.getState();
			case SEND:
				// Check to see if the SubmitObject is registered 
				// to send over a Submit API module or by the
				// application that submitted it
				if(submitobj.getAddress() == null) {
					// return WAITING_ON_APP_RESPONSE
					submitobj.setState(CommunicationState.WAITING_ON_APP_RESPONSE);
					return CommunicationState.WAITING_ON_APP_RESPONSE;
				} else {
					// Submit "owns" the data and is responsible for sending it.
					Log.i(TAG, "Setting state to IN_PROGRESS");
					submitobj.setState(CommunicationState.IN_PROGRESS);
					mSender.updateState(submitobj, mRadio, CommunicationState.IN_PROGRESS);
					return CommunicationState.IN_PROGRESS;
				}
			case IN_PROGRESS:
				// TODO set timer
				return CommunicationState.IN_PROGRESS;
			case WAITING_ON_APP_RESPONSE:
				// TODO set timer
				return CommunicationState.WAITING_ON_APP_RESPONSE;
			case TIMEOUT:
				return CommunicationState.FAILURE_RETRY;
			case FAILURE_NO_RETRY:
			default:
				return CommunicationState.FAILURE_NO_RETRY;
		}
	}



	/**
	 * This is the function that makes most of the "routing"
	 * decisions for the CommunicationManager.
	 * Eventually, I want to be able to read from a properties
	 * file in order to do do some more customizable rules
	 * pertaining to cost. 
	 * 
	 * @param radio
	 * @param data
	 * @return
	 */
	private boolean dataFitsChannel(Radio radio, DataPropertiesObject data) {
		// Here we do not consider order as it is dealt with
		// before being pushed to the CommunicationManager
		DataSize size = data.getDataSize();
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
	
}
