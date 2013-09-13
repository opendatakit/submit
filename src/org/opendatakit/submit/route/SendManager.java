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
	 * Given an HTTP code, return a corresponding CommunicationState
	 * @return
	 */
	private CommunicationState httpCodeToCommunicationState(int code) {
		if (200 <= code && code < 300) {
			Log.i(TAG, "HTTP Response Code: Successful "+ Integer.toString(code));
			return CommunicationState.SUCCESS;
		} else if (300 <= code && code < 400) {
			Log.i(TAG, "HTTP Response Code: Redirection "+ Integer.toString(code));
			return CommunicationState.FAILURE_RETRY;
		} else if (400 <= code && code < 500) {
			Log.i(TAG, "HTTP Response Code: Client Error "+ Integer.toString(code));
			return CommunicationState.FAILURE_RETRY;
		} else if (500 <= code && code < 600) {
			Log.i(TAG, "HTTP Response Code: Server Error "+ Integer.toString(code));
			return CommunicationState.FAILURE_NO_RETRY;
		}
		Log.i(TAG, "!!!! No recognizable HTTP Response Code !!!! "+ Integer.toString(code));
		return CommunicationState.FAILURE_NO_RETRY;
	}
	
	private DestinationAddress getAddress(API api, ArrayList<DestinationAddress> addresses) throws InvalidAddressException {
		DestinationAddress addr = getAddressTypeForAPI(api, addresses);
		if (addr != null) {
			return addr;
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
				// TODO SMS module
				mSubmit.setState(CommunicationState.SUCCESS);
				
			case GCM:
				// TODO GCM module
				mSubmit.setState(CommunicationState.SUCCESS);
			case APACHE_HTTP:
				Log.i(TAG, "Selected API is APACHE_HTTP");
				try {
					/* Try sending with HttpClient */
					ApacheHttpClient client = new ApacheHttpClient(mSubmit, (HttpAddress)getAddress(api, mSubmit.getAddress().getAddresses()));
					int httpcode = client.uploadData();
					Log.i(TAG, "HTTP code: " + httpcode);
					mSubmit.setState(httpCodeToCommunicationState(httpcode));
				} catch (InvalidAddressException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
					mSubmit.setState(CommunicationState.FAILURE_NO_RETRY);
				}
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
