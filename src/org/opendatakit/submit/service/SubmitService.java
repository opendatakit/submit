package org.opendatakit.submit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.opendatakit.submit.data.DataObject;
import org.opendatakit.submit.data.SendObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.route.CommunicationManager;
import org.opendatakit.submit.stubapi.SubmitAPI;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Main coordination class in Submit.
 * @author mvigil
 *
 */
public class SubmitService extends Service {

	private static final String TAG = "SubmitService";
	private static LinkedList<SubmitObject> mSubmitQueue = null; // Record keeping
	private static HashMap<String, ArrayList<String>> mSubmitMap = null; // Record keeping
	private static HashMap<String, TupleElement<DataObject,SendObject>> mDataObjectMap = null;
	public static Radio mActiveRadio = null;
	public static Radio mActiveP2PRadio = null;
	private SubmitAPI mSubApi = null;
	private ChannelMonitor mMonitor = null;
	private IntentFilter mFilter = null;
	private static final int QUEUE_THRESHOLD = 3;
	protected static Runnable mRunnable = null;
	protected static Thread mThread = null;
	private SharedPreferences mPrefs = null;
	private Resources mResources = null;
	private CommunicationManager mCommManager = null;
	
	/*
	 * Service methods
	 */
	
	@Override
	public void onCreate() {
		
		Log.i(TAG, "onCreate() starting SubmitService");
		
		/* Record keeping data structures */
		// Queues all Submissions and Registrations until the time is ripe for sending
		mSubmitQueue = new LinkedList<SubmitObject>(); 
		// Maps to look up and facilitate queue management
		mSubmitMap = new HashMap<String, ArrayList<String>>();
		mDataObjectMap = new HashMap<String, TupleElement<DataObject,SendObject>>();
		
		// Set up private vars
		mCommManager = new CommunicationManager(getApplicationContext());
		mFilter = new IntentFilter();
		mSubApi = new SubmitAPI();
        
        // Set up BroadcastReceiver
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		mMonitor = new ChannelMonitor();
		
		// Set up Queue Thread
        mRunnable = new SendToManager();
        mThread = new Thread(mRunnable);
        mThread.start();
		
		this.getApplicationContext().registerReceiver(mMonitor, mFilter);
		
		
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
    public void onDestroy() {
		Log.i(TAG, "Destroying SubmitService instance");
		this.getApplicationContext().unregisterReceiver(mMonitor);

    }
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Binding to SubmitService");
		return mBinder;
	}
	
	private final ClientRemote.Stub mBinder = new ClientRemote.Stub() {
		
		@Override
		public String submit(String app_uid, DataObject data, SendObject send)
				throws RemoteException {
			
			Log.i(TAG, "In submit()");
			SubmitObject submit = new SubmitObject(app_uid, data, send);

			/* Map submission ID to DataObject */
			// Here, we assume that the SubmitID
			// is unique, so we do not check before
			// putting it into the map
			TupleElement<DataObject,SendObject> metadata = new TupleElement<DataObject,SendObject>(submit.getData(),send);
			
			addSubmitObjToSubmitMap(app_uid, submit, metadata);
						
			manageQueue();
			return submit.getSubmitID();
		}
		
		@Override
		public String register(String app_uid, DataObject data)
				throws RemoteException {
			Log.i(TAG, "In register()");
					
			SubmitObject submit = new SubmitObject(app_uid, data, null);		
			
			/* Map submission ID to DataObject */
			// Here, we assume that the SubmitID
			// is unique, so we do not check before
			// putting it into the map
			TupleElement<DataObject,SendObject> metadata = new TupleElement<DataObject,SendObject>(submit.getData(),null);

			addSubmitObjToSubmitMap(app_uid, submit, metadata);
			
			manageQueue();
			return submit.getSubmitID();
		}

		private void addSubmitObjToSubmitMap(String app_uid, SubmitObject submit, TupleElement<DataObject,SendObject> metadata) {
			ArrayList<String> submitids;
			/* Map application to submission ID */
			// Check if application already has 
			// submissions on the queue
			if(mSubmitMap.containsKey(app_uid)) {
				// If it does, add the SubmitID to the list of SubmitID's
				submitids = mSubmitMap.get(app_uid);
			} else {
				submitids = new ArrayList<String>();
			}
			submitids.add(submit.getSubmitID());
			mSubmitMap.put(app_uid, submitids);
			
			mDataObjectMap.put(submit.getSubmitID(), metadata);
			
			/* Put submission on queue */
			mSubmitQueue.add(submit);
		}
		
		@Override
		public int queueSize() throws RemoteException {
			Log.i(TAG, "In queueSize()");
			return mSubmitQueue.size();
		}
		
		@Override
		public boolean onQueue(String submit_uid) throws RemoteException {
			Log.i(TAG, "In onQueue()");
			return mDataObjectMap.containsKey(submit_uid);		
		}
		
		@Override
		public SendObject getSendObjectById(String submit_uid) throws RemoteException {
			return (SendObject)mDataObjectMap.get(submit_uid).get(1);
		}
		
		@Override
		public String[] getQueuedSubmissions(String app_uid) throws RemoteException {
			String[] ids = null;
			ArrayList<String> idlist = mSubmitMap.get(app_uid);
			
			// If there are no SubmitObjects belonging to
			// the AppID, then return null values
			if(idlist.size() < 1) {
				return null;
			}
			
			// Transfer listed SubmitIDs to 
			// String[] ids. We do the transfer
			// for the purpose of serialization
			ids = new String[idlist.size()];
			int position = 0;
			for(String id : idlist) {
				ids[position] = id;
				position++;
			}
			return ids;
		}
		
		@Override
		public DataObject getDataObjectById(String submit_uid) throws RemoteException {
			return (DataObject)mDataObjectMap.get(submit_uid).get(0);
		}
		
		@Override
		public void delete(String submit_uid )
				throws RemoteException {
			Log.i(TAG, "In delete()");
			// Remove from mSubmitQueue and mSubmitMap
			for(SubmitObject submit : mSubmitQueue ) {
				if(submit.getSubmitID().equals(submit_uid)) {
					
					// Remove from mSubmitMap
					String appid = submit.getAppID();
					ArrayList<String> submitids = mSubmitMap.get(appid);
					submitids.remove(submit_uid);
					mSubmitMap.put(appid, submitids);
					
					// Remove from mSumitQueue
					mSubmitQueue.remove(submit);
					
					break;
				}
			} 
			// Remove from mDataObjectMap
			mDataObjectMap.remove(submit_uid);
		}

		@Override
		public String registerApplication(String app_uid)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
	};
		
	
	/*
	 * private methods
	 */
	
	/* Call this to start managing the queue */
	protected void manageQueue() {
		Log.i(TAG, "manageQueue()");
		try{
			if(!mThread.isAlive()) {
				mThread.start();
			}
		} catch(NullPointerException npe) {
			Log.e(TAG, npe.getMessage());
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	

	/* 
	 * Broadcasts CommunicationState to the 
	 * Application listening with a BroadcastReceiver
	 * using the UID as an ID mechanism.
	 */
	private void broadcastStateToApp(CommunicationState state, SubmitObject submit) {
		Intent intent = new Intent();
		intent.setAction(submit.getAppID());
		intent.putExtra("SUBMIT_OBJECT_ID", submit.getSubmitID());
		intent.putExtra("RESULT", (Parcelable)state);
		sendBroadcast(intent);
		Log.i(TAG,"Sent broadcast to " + submit);
	}

	/*
	 * Runnables
	 */
	
	/**
	 * sendToManager Runnable
	 * Gets passed to the routeInBackgroundThread;
	 * based on the TYPE of the object on the top of the queue
	 * it passes off to the MessageManager or the SyncManager
	 */
	private class SendToManager implements Runnable {

		@Override
		public void run() {
			Log.i(TAG, "Starting to run sendToManagerThread");
			// While there are submission requests in the Queue, service the queue
			// with appropriate calls to executeTask() from the MessageManager or SyncManager
			while(!Thread.currentThread().isInterrupted()) { // TODO this is a bit brute force-ish, but it will do for the moment
				try {
					if (mSubmitQueue.size() < 1) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Log.e(TAG, e.getMessage());
						}
						continue;
					}
					CommunicationState result = null;
					SubmitObject top = mSubmitQueue.getFirst();
					// Check if there is an ordered element before
					// passing it off to the CommunicationManager
					
					result = (CommunicationState)mCommManager.executeTask(top, mActiveRadio);
					
					
					// Depending on the resulting CommunicationState
					// pop the top object off mSubmitQueue, keep it in for 
					// another round, or pop it and throw an exception
					switch(result) {
						case WAITING_ON_APP_RESPONSE:
							Log.i(TAG, "Result was PASS_TO_APP");
							// This assumes that we should treat App-owned
							// SubmitObjects the same as Submit-owned
							// where we just pop off the top 
							// If App encounters an issue while communicating
							// they can figure out why and resubmit a
							// submission request using submit() or register()
							// broadcast result to client app
							broadcastStateToApp(result, top);
							// Delete
							mBinder.delete(top.getSubmitID());
							break;
						case SUCCESS:
							Log.i(TAG, "Result was SUCCESS");
							// Delete
							mBinder.delete(top.getSubmitID());
							// broadcast result to client app
							broadcastStateToApp(result, top);
							Log.i(TAG, "Thread has finished run()");
							break;
						case FAILURE:
							Log.i(TAG, "Result was FAILURE");
							// Delete
							mBinder.delete(top.getSubmitID());
							// broadcast result to client app
							broadcastStateToApp(result, top);
							break;
						case SEND:
						case CHANNEL_UNAVAILABLE:
							// broadcast result to client app
							broadcastStateToApp(result, top);
							break;
						default:
							/*
							 * TODO Consider adding a mechanism here, where if 
							 * a QueuedObject has just been sitting on the queue
							 * for more than X rounds through the Queue, we dump
							 * it as a particular failure case. 
							 */
							break;
					}
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					e.printStackTrace();
					return;
				} 
				// Add downtime
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
					break;
				} // TODO temporary time
				continue;
			}
			Log.i(TAG, "Thread has finished run()");
		}
		
	};
	

	
	/**
	 * ChannelMonitor listens to see when Radio
	 * interfaces are activated.
	 * 
	 * This class extends the BroadcastReceiver interface
	 * and updates the current Radio that is active.
	 * 
	 * @author mvigil
	 *
	 */
	public class ChannelMonitor extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try{
				Log.i(TAG, "onReceive in ChannelMonitor");

				ConnectivityManager connMgr = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

				if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					// WiFi
					Log.i(TAG, "WiFi enabled!");
					mActiveRadio = Radio.WIFI;
					// TODO determine if you want to try WiFi-Direct at any
					// point here

				}
				if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

					if (isConnectionFast(networkInfo.getType(),
							networkInfo.getSubtype())) {
						// "High speed" cellular connection
						Log.i(TAG, "CELL enabled!");
						mActiveRadio = Radio.HIGH_BAND_CELL;
					} else {
						// Low speed cellular connection
						Log.i(TAG, "GSM enabled!");
						mActiveRadio = Radio.LOW_BAND_CELL;
					}

				}
			} catch (Exception e) {
				String err = (e.getMessage() == null)?"Exception":e.getMessage();
				Log.e(TAG, err);
				e.printStackTrace();
			}
			
		}

		/**
		 * Check if the connection is fast From Emil @ stackoverflow
		 * 
		 * @param type
		 * @param subType
		 * @return
		 */
		public boolean isConnectionFast(int type, int subType) {
			if (type == ConnectivityManager.TYPE_WIFI) {
				return true;
			} else if (type == ConnectivityManager.TYPE_MOBILE) {
				switch (subType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return false; // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return true; // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return true; // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return false; // ~ 100 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return true; // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return true; // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return true; // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return true; // ~ 400-7000 kbps
					/*
					 * Above API level 7, make sure to set android:targetSdkVersion
					 * to appropriate level to use these
					 */
				case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
					return true; // ~ 1-2 Mbps
				case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
					return true; // ~ 5 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
					return true; // ~ 10-20 Mbps
				case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
					return false; // ~25 kbps
				case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
					return true; // ~ 10+ Mbps
					// Unknown
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					return false;
				}
			} else {
				return false;
			}
		}
	}

}
