package org.opendatakit.submit.scheduling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;


import org.opendatakit.submit.route.QueuedObject;
import org.opendatakit.submit.scheduling.ClientRemote;
import org.opendatakit.submit.stubapi.SubmitAPI;
import org.opendatakit.submit.communication.MessageManager;
import org.opendatakit.submit.communication.SyncManager;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.flags.SyncDirection;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.flags.Types;

import android.R;
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
	private static LinkedList<QueuedObject> mSubmitQueue = null;
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
	private MessageManager msgmang = null;
	private SyncManager syncmang = null;
	
	/*
	 * Service methods
	 */
	
	@Override
	public void onCreate() {
		
		Log.i(TAG, "onCreate() starting SubmitService");
		
		// Set up private vars
		mSubmitQueue = new LinkedList<QueuedObject>();
		syncmang = new SyncManager(getApplicationContext());
		msgmang = new MessageManager(getApplicationContext());
		mFilter = new IntentFilter();
		mSubApi = new SubmitAPI();
		
		 // Set up Queue Thread
        mRunnable = new sendToManager();
        mThread = new Thread(mRunnable);
        // Set up BroadcastReceiver
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		mMonitor = new ChannelMonitor();
		
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
	
	// TODO Consider moving this to another class and importing it...just a thought.
	private final ClientRemote.Stub mBinder = new ClientRemote.Stub() {
		
		@Override
		public String send(String dest, String payload, String uid) throws RemoteException {
			/*CommunicationState state = null;
			try {
				state = (CommunicationState) mSubApi.send(dest, payload, uid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return getStringState(state, uid);*/
			
			QueuedObject submit = new QueuedObject(dest, payload, uid);
			mSubmitQueue.add(submit);
			manageQueue();
			return submit.getUid();
		}

		@Override
		public String create(SyncType st, String uri, String pathname, String uid)
				throws RemoteException {
			/*CommunicationState state = null;
			try {
				state = (CommunicationState) mSubApi.create(st, uri, pathname, uid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return getStringState(state, uid);*/
			QueuedObject submit = new QueuedObject(st, SyncDirection.CREATE, uri, pathname, uid);
			mSubmitQueue.add(submit);
			Log.i(TAG, "create()");
			manageQueue();
			return submit.getUid();
		}

		@Override
		public String download(SyncType st, String uri, String pathname, String uid)
				throws RemoteException {
			/*CommunicationState state = null;
			try {
				state = (CommunicationState) mSubApi.download(st, uri, pathname, uid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return getStringState(state, uid);*/
			QueuedObject submit = new QueuedObject(st, SyncDirection.DOWNLOAD, uri, pathname, uid);
			mSubmitQueue.add(submit);
			Log.i(TAG, "submit()");
			manageQueue();
			return submit.getUid();
		}

		@Override
		public String sync(SyncType st, String uri, String pathname, String uid)
				throws RemoteException {
			/*CommunicationState state = null;
			try {
				state = (CommunicationState) mSubApi.sync(st, uri, pathname, uid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return getStringState(state, uid);*/
			QueuedObject submit = new QueuedObject(st, SyncDirection.SYNC, uri, pathname, uid);
			mSubmitQueue.add(submit);
			Log.i(TAG, "sync()");
			manageQueue();
			return submit.getUid();
		}

		@Override
		public String delete(SyncType st, String uri, String pathname, String uid)
				throws RemoteException {
			/*CommunicationState state = null;
			try {
				state = (CommunicationState) mSubApi.delete(st, uri, pathname, uid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return getStringState(state, uid);*/
			QueuedObject submit = new QueuedObject(st, SyncDirection.DELETE, uri, pathname, uid);
			mSubmitQueue.add(submit);
			Log.i(TAG, "delete()");
			manageQueue();
			return submit.getUid();
		}

		@Override
		public boolean onQueue(String uid) throws RemoteException {
			// TODO
			return false;
		}

		@Override
		public int queueSize() throws RemoteException {
			return mSubmitQueue.size();
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
	private void broadcastStateToApp(CommunicationState state, String uid) {
		Intent intent = new Intent();
		intent.setAction(uid);
		intent.putExtra("RESULT", (Parcelable)state);
		sendBroadcast(intent);
		Log.i(TAG,"Sent broadcast to " + uid);
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
	private class sendToManager implements Runnable {

		@Override
		public void run() {
			Log.i(TAG, "Starting to run sendToManagerThread");
			// While there are submission requests in the Queue, service the queue
			// with appropriate calls to executeTask() from the MessageManager or SyncManager
			while(mSubmitQueue.size() > 0) { // TODO this is a bit brute force-ish, but it will do for the moment
				try {
					CommunicationState result = null;
					if(mActiveRadio == null) {
						Log.i(TAG, "No active radio. Exit RoutingThread.");
						break;
					}
					QueuedObject top = mSubmitQueue.getFirst();
					
					// Pass QueuedObject off to appropriate manager
					if(top.getType() == Types.SYNC) {
						// Handle Sync data
						// TODO see if any P2P mode has been specified
						// result of communication over determined API
						result = (CommunicationState)syncmang.executeTask(top, mActiveRadio);
					} else if (top.getType() == Types.MESSAGE){
						// Handle Message data
						// result of communication over determined API
						result = (CommunicationState)msgmang.executeTask(top, mActiveRadio);
					}
					
					// Depending on the resulting CommunicationState
					// pop the top object off mSubmitQueue, keep it in for 
					// another round, or pop it and throw an exception
					switch(result) {
						case SUCCESS:
							// Pop off the top
							top = mSubmitQueue.pop();
							// broadcast result to client app
							broadcastStateToApp(result, top.getUid());
							break;
						case FAILURE:
						case IN_PROGRESS:
						case UNAVAILABLE:
							// broadcast result to client app
							broadcastStateToApp(result, top.getUid());
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
					Thread.sleep(5); // TODO temporary time
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				} catch (CommunicationException e) {
					Log.e(TAG, e.getMessage());
				}
			}
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
			Log.i(TAG, "onReceive in ChannelMonitor");

			ConnectivityManager connMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// WiFi
				Log.i(TAG, "WiFi enabled!");
				mActiveRadio = Radio.WIFI;
				// TODO determine if you want to try WiFi-Direct at any point here

			}
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

				if (isConnectionFast(networkInfo.getType(),
						networkInfo.getSubtype())) {
					// "High speed" cellular connection
					Log.i(TAG, "CELL enabled!");
					mActiveRadio = Radio.CELL;
				} else {
					// Low speed cellular connection
					Log.i(TAG, "GSM enabled!");
					mActiveRadio = Radio.GSM;
				}

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
