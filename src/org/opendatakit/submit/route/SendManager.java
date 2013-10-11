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
import org.opendatakit.submit.libs.http.ApacheHttpClient;

import android.util.Log;

/**
 * SendManager determines which SendObject data
 * to use based on available channel properties and
 * DataPropertiesObject. 
 * @author mvigil
 *
 */
public class SendManager {
	
	private final String TAG = SendManager.class.getName();
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
		if(state == CommunicationState.IN_PROGRESS) {
			Runnable run = new SendByModule(this, submit, radio);
			Thread thread = new Thread(run);
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
	
	
	
	/**
	 * Get DestinationAddress from SendObject
	 * @param api
	 * @param addresses
	 * @return
	 * @throws InvalidAddressException
	 */
	private DestinationAddress getAddress(API api, ArrayList<DestinationAddress> addresses) throws InvalidAddressException {
		DestinationAddress addr = getAddressTypeForAPI(api, addresses);
		if (addr != null) {
			return addr;
		} else {
			throw new InvalidAddressException("There are no DestinationAddress formats suitable for the available APIs");
		}
	}

	/**
	 * Get appropriate address for the selected API
	 * @param api
	 * @param addresses
	 * @return
	 */
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
			case APACHE_HTTP:
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
		if (radio == Radio.WIFI) {
			// TODO as of now we only have an HTTP client
			// as we expand, there should be more checks here
			if(hasHttp(submit.getAddress().getAddresses())) {
				return API.APACHE_HTTP;
			}
		}
		return API.STUB;
	}

	
	/**
	 * Does the list contain an HttpAddress or HttpsAddress
	 * @param addresses
	 * @return
	 */
	private boolean hasHttp(ArrayList<DestinationAddress> addresses) {
		if(addresses == null)
		{	
			Log.e(TAG, "!!!! address list is null !!!!");
			return false;
		}
		for (DestinationAddress address : addresses) {
			if(address instanceof HttpAddress) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO: This needs to be pulled out into it's own class
	 * @author mvigil
	 *
	 */
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
			Log.i(TAG, "SendByModule is in run()");
			CommunicationState commstate = null;
			if(mSubmit == null) {
				Log.i(TAG, "SubmitObject is null.");
				mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
				// Callback to CommunicationManager that passes the SubmitObject 
				// With the modified state
				mManager.getManager().resultState(mSubmit);
				return;
			}
			if(mRadio == null) {
				Log.i(TAG, "Radio is null.");
				mSubmit.setState(CommunicationState.CHANNEL_UNAVAILABLE);
				// Callback to CommunicationManager that passes the SubmitObject 
				// With the modified state
				mManager.getManager().resultState(mSubmit);
				return;
			}
			API api = mManager.whichAPI(mSubmit, mRadio);
			Log.i(TAG, "API = " + api.toString());
			
			switch(api) {
			case SMS:
				commstate = CommunicationState.FAILURE_NO_RETRY;
				mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
				break;
			case GCM:
				commstate = CommunicationState.FAILURE_NO_RETRY;
				mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
				break;
			case APACHE_HTTP:
				Log.i(TAG, "Selected API is APACHE_HTTP");
				try {
					/* Try sending with HttpClient */
					ApacheHttpClient client = new ApacheHttpClient(mSubmit, (HttpAddress)getAddress(api, mSubmit.getAddress().getAddresses()));
					int code = client.uploadData();
					CommunicationState state = client.httpCodeToCommunicationState(code);
					commstate = state;
					Log.i(TAG, "State: " + state.toString());
					mSubmit.setState(state);
					mSubmit.setCode(code);
				} catch (InvalidAddressException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
					commstate = CommunicationState.FAILURE_NO_RETRY;
					mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
					mSubmit.setCode(-1);
				}
				break;
			default:
				mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
				commstate = CommunicationState.FAILURE_NO_RETRY;
				mSubmit.setCode(-1);
				break;
			}
			// Callback to CommunicationManager that passes the SubmitObject 
			// With the modified state
			Log.i(TAG, "End of SendByModule.run(): CommsState: " + commstate);
			mManager.getManager().updateState(mSubmit.getSubmitID(), commstate);
			return;
		}
		
	}
}
