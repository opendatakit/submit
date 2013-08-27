package org.opendatakit.submit.route;

import java.util.ArrayList;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.address.HttpsAddress;
import org.opendatakit.submit.address.SmsAddress;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.Radio;

import android.util.Log;

public class SendManager {
	private CommunicationManager mManager = null;
	private ArrayList<Thread> mThreadList = null;
	
	/**
	 * Empty constructor
	 */
	public SendManager(CommunicationManager manager) {
		mManager = manager;
	}
	
	public void updateState(SubmitObject submit, Radio radio, CommunicationState state) {
		submit.setState(state);
		if(state == CommunicationState.SEND) {
			Runnable run = new SendByModule(this, submit, radio);
			Thread thread = new Thread(run);
			mThreadList.add(thread);
			thread.start();
		}
	}
	
	/**
	 * Access to CommunicationManager for callback purposes
	 * @return
	 */
	public CommunicationManager getManager() {
		return mManager;
	}
	
	/* Private functions */
	
	private DestinationAddress getAddress(API api, ArrayList<DestinationAddress> addresses) throws InvalidAddressException {
		DestinationAddress addr = getAddressTypeForAPI(api, addresses);
		if (addr != null) {
			// TODO return a call to an SMS sending function
			return null;
		} else {
			throw new InvalidAddressException("There are no DestinationAddress formats suitable for the available APIs");
		}
	}

	private DestinationAddress getAddressTypeForAPI(API api, ArrayList<DestinationAddress> addresses) {
		for(DestinationAddress da : addresses) {
			switch(api) {
			case SMS:
				if (da.getClass() == SmsAddress.class) {
					return da;
				}
			case GCM:
				if (da.getClass() == SmsAddress.class || da.getClass() == HttpAddress.class || da.getClass() == HttpsAddress.class) {
					return da;
				}
			case ODKv2:
				if(da.getClass() == HttpAddress.class || da.getClass() == HttpsAddress.class) {
					return da;
				}
			default:
				break;
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
	private API whichAPI(SubmitObject submit, Radio radio) {
		// TODO: Add more here when modular APIs are established
		// For now, return STUB API no matter what
		return API.STUB;
	}
	
	private class SendByModule implements Runnable {
		private SubmitObject mSubmit = null;
		private Radio mRadio = null;
		private SendManager mManager = null;
		
		public SendByModule(SendManager manager, SubmitObject submit, Radio radio) {
			mManager = manager;
			mSubmit = submit;
			mRadio = radio;
		}
		
		@Override
		public void run() {
			if(mSubmit == null || mRadio == null) {
				return;
			}
			API api = whichAPI(mSubmit, mRadio);
			CommunicationState result = null;
			try {
				DestinationAddress addr = getAddress(api, mSubmit.getAddress().getAddresses());
			} catch (InvalidAddressException e) {
				Log.e(SendManager.class.getName(), e.getMessage());
			}
			switch(api) {
			case SMS:
				// TODO SMS module
				mSubmit.setState(CommunicationState.SUCCESS);
				
			case GCM:
				// TODO GCM module
				mSubmit.setState(CommunicationState.SUCCESS);
			case ODKv2:
				// TODO ODKv2 module
				mSubmit.setState(CommunicationState.SUCCESS);
			default:
				mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
			}
			// Callback to CommunicationManager that passes the SubmitObject 
			// With the modified state
			mManager.getManager().resultState(mSubmit);
			return;
		}
		
	}
}
