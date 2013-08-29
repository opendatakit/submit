package org.opendatakit.submit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.opendatakit.submit.data.DataObject;
import org.opendatakit.submit.data.SendObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.flags.BroadcastExtraKeys;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.receivers.ChannelMonitor;
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
	private static LinkedList<SubmitObject> mSubmitQueue = null; // Keeps track of all SubmitObjects and keeps them in a FIFO queue
	private static HashMap<String, ArrayList<String>> mSubmitMap = null; // Keeps track of all SubmitObjects that belong to a given app. Maps list of SubmitIDs to an AppID.
	private static HashMap<String, TupleElement<DataObject,SendObject>> mDataObjectMap = null; // Keeps track of DataObjects and SendObjects that belong to a SubmitObject. Maps tuple <DataObject,SendObject> to SubmitID for quick lookup.
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
	
	// TODO: REMOVE TOTAL HACK ... mBinder should be a class would solve these issues
	private static List<AppReceiver> mAppReceivers;
	private static Context mContext;
	private static SubmitService mySelf;
	
	/*
	 * Service methods
	 */
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate() starting SubmitService");
		
		mContext = this;
		mySelf = this;
		
		/* Record keeping data structures */
		// Queues all Submissions and Registrations until the time is ripe for sending
		mSubmitQueue = new LinkedList<SubmitObject>(); 
		// Maps to look up and facilitate queue management
		mSubmitMap = new HashMap<String, ArrayList<String>>();
		mDataObjectMap = new HashMap<String, TupleElement<DataObject,SendObject>>();
		mAppReceivers = new LinkedList<AppReceiver>();
		
		
		// Set up private vars
		mCommManager = new CommunicationManager(this);
		mFilter = new IntentFilter();
		mSubApi = new SubmitAPI();
        
        // Set up BroadcastReceiver
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		mMonitor = new ChannelMonitor(this);
		
		// Set up Queue Thread
        mRunnable = new SendToCommunicationManager(this);
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
		
		// unregister the receivers
		for(AppReceiver receiver : mAppReceivers) {
			receiver.destroy();
		}
		
		super.onDestroy();
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
			
			UUID uid = UUID.randomUUID();
			
			String appUid = uid.toString();
			AppReceiver listener = new AppReceiver(appUid, mySelf, mContext);
			mAppReceivers.add(listener);
			return appUid;
		}
	};
	
	/* 
	 * Broadcasts CommunicationState to the 
	 * Application listening with a BroadcastReceiver
	 * using the UID as an ID mechanism.
	 */
	public void broadcastStateToApp(SubmitObject submit, CommunicationState state) {
		Intent intent = new Intent();
		intent.setAction(submit.getAppID());
		intent.putExtra(BroadcastExtraKeys.COMMUNICATION_STATE, state.toString());
		intent.putExtra(BroadcastExtraKeys.SUBMIT_OBJECT_ID, submit.getSubmitID());
		sendBroadcast(intent);
		Log.i(TAG,"Sent broadcast to " + submit.getAppID());
	}
		
	/**
	 * A callback function
	 * to update the queue when 
	 * an update has been made to 
	 * a SubmitObject.
	 * @param submit
	 */
	public void resultState(SubmitObject submit) {
		// Update SubmitQueue
		for(SubmitObject sub : mSubmitQueue) {
			if (sub.getSubmitID().equals(submit.getSubmitID())) {
				mSubmitQueue.remove(sub);
				mSubmitQueue.add(submit);
			}
		}
	}
	
	public void updateState(String submitObjUid, CommunicationState state) {
		// TODO: fix so we don't brute force
		for(SubmitObject sub : mSubmitQueue) {
			if (sub.getSubmitID().equals(submitObjUid)) {
				// TODO: do we want logic to check that we will accept this state
				sub.setState(state);
				return;
			}
		}
		System.err.println("ERROR - Unable to find submit object ... moving on");
	}
	/* Setters */
	public void setActiveRadio(Radio radio) {
		mActiveRadio = radio;
	}
	
	/* Getters */
	public LinkedList<SubmitObject> getSubmitQueue() {
		return mSubmitQueue;
	}
	
	public Radio getActiveRadio() {
		return mActiveRadio;
	}
	
	public CommunicationManager getCommunicationManager() {
		return mCommManager;
	}
	
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
}
