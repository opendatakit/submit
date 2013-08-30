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
import android.os.Binder;
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
	private SubmitQueue mSubmitQueue = null;
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
	private IBinder mBinder = null;
	private Context mContext = null;
	
	// TODO: REMOVE TOTAL HACK ... mBinder should be a class would solve these issues
	private static List<AppReceiver> mAppReceivers;
	
	/*
	 * Service methods
	 */
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate() starting SubmitService");
		

		
		/* Record keeping data structures */
		// Queues all Submissions and Registrations until the time is ripe for sending
		mSubmitQueue = new SubmitQueue();
		mAppReceivers = new LinkedList<AppReceiver>();
		
		
		// Set up private vars
		mCommManager = new CommunicationManager(this);
		mFilter = new IntentFilter();
		mSubApi = new SubmitAPI();
		mContext = this;
        
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
		// Set up IBinder
		mBinder = new SubmitServiceInterface(this, mContext);
		return (IBinder)mBinder;
	}
	
	//private final ClientRemote.Stub mBinder = new ClientRemote.Stub()	
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
		if (submit != null) {
			mSubmitQueue.updateSubmitQueue(submit);
		}
	}
	
	public void updateState(String submitObjUid, CommunicationState state) {
		SubmitObject submit = mSubmitQueue.getSubmitObjectBySubmitId(submitObjUid);
		submit.setState(state);
		mSubmitQueue.updateSubmitQueue(submit);
	}
	
	/* Setters */
	public void setActiveRadio(Radio radio) {
		mActiveRadio = radio;
	}
	
	public void addAppReceiver(AppReceiver apprecv) {
		mAppReceivers.add(apprecv);
	}
	
	public void addSubmitObject(SubmitObject submit) {
		if (submit != null) {
			mSubmitQueue.addSubmitObjectToQueue(submit);
		}
	}
	
	public void removeSubmitObjectFromQueue(SubmitObject submit) {
		mSubmitQueue.removeSubmitObjectFromQueue(submit);
	}
	
	public void removeSubmitObjectFromQueue(String app_id, String submit_id) {
		mSubmitQueue.removeSubmitObjectFromQueue(app_id, submit_id);
	}
	
	public void addLastToSubmitQueue(SubmitObject submit) {
		mSubmitQueue.addSubmitObjectLast(submit);
	}
	
	public SubmitObject popFromSubmitQueue() {
		return mSubmitQueue.popTopSubmitObject();
	}
	
	/* Getters */
	public boolean onSubmitQueue(String submit_id) {
		return mSubmitQueue.onSubmitQueue(submit_id);
	}
	
	public int getSubmitQueueSize() {
		return mSubmitQueue.getSubmitQueueSize();
	}
	
	public SubmitQueue getSubmitQueue() {
		return mSubmitQueue;
	}
	
	public Radio getActiveRadio() {
		return mActiveRadio;
	}
	
	public CommunicationManager getCommunicationManager() {
		return mCommManager;
	}
	
	public SubmitObject getSubmitObjectFromSubmitQueue(String submit_id) {
		return mSubmitQueue.getSubmitObjectBySubmitId(submit_id);
	}
	
	public ArrayList<String> getSubmitIdsFromAppId(String app_id) {
		return mSubmitQueue.getSubmitIdsByAppId(app_id);
	}
	
	/* Call this to start managing the queue */
	public void manageQueue() {
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
