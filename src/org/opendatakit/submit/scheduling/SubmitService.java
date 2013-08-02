package org.opendatakit.submit.scheduling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendatakit.submit.route.QueuedObject;
import org.opendatakit.submit.scheduling.ClientRemote;
import org.opendatakit.submit.stubapi.SubmitAPI;
import org.opendatakit.submit.communication.MessageManager;
import org.opendatakit.submit.communication.SyncManager;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.flags.SyncDirection;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
	private LinkedList<QueuedObject> mSubmitQueue = null;
	public static Radio mActiveRadio = null;
	public static Radio mActiveP2PRadio = null;
	private static final int NTHREADS = 10;
	private static ExecutorService mExecutor = null;
	private Runnable mRunnable;
	private Thread mThread;
	private SubmitAPI mSubApi;
	
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
			if (mExecutor.isTerminated()) {
				mExecutor.submit(mThread);
			}
			return submit.getUid();
		}

		@Override
		public boolean onQueue(String uid) throws RemoteException {
			return this.onQueue(uid);
		}

		@Override
		public int queueSize() throws RemoteException {
			return this.queueSize();
		}
	};
	
	/*
	 * Service methods
	 */
	
	@Override
	public void onCreate() {
		
		Log.i(TAG, "onCreate() starting SubmitService");
		//mRunnable = new sendToManager();
		//mThread = new Thread(mRunnable);
		mSubApi = new SubmitAPI();
		// Does the SubmitQueue exist?
		if (mSubmitQueue == null) {
			mSubmitQueue = new LinkedList<QueuedObject>();
		}
		//if (mExecutor == null) {
		//	mExecutor = Executors.newFixedThreadPool(NTHREADS);
		//}
		//mExecutor.execute(mThread);
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
        mExecutor.shutdown();
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Binding to SubmitService");
		return mBinder;
	}
	
	/*
	 * private methods
	 */
	private boolean onQueue(String uid) {
		for (QueuedObject qo : mSubmitQueue) {
			if (qo.getUid().equals(uid)) {
				return true;
			}
		}
		return false;
	}
	
	private int queueSize() {
		return mSubmitQueue.size();
	}

	/* For now this is used for debugging */
	private String getStringState(CommunicationState state, String uid) {
		Intent intent = new Intent();
		intent.setAction(uid);
		if (state == null) {
			return null;
		} else {
			switch(state) {
			case SUCCESS:
				intent.putExtra("RESULT", "SUCCESS");
				sendBroadcast(intent);
				Log.i(TAG,"Sent broadcast");
				return "SUCCESS";
			case FAILURE:
				intent.putExtra("RESULT", "FAILURE");
				sendBroadcast(intent);
				Log.i(TAG,"Sent broadcast");
				return "FAILURE";
			case IN_PROGRESS:
				intent.putExtra("RESULT", "IN_PROGRESS");
				sendBroadcast(intent);
				Log.i(TAG,"Sent broadcast");
				return "IN_PROGRESS";
			case UNAVAILABLE:
				intent.putExtra("RESULT", "UNAVAILABLE");
				sendBroadcast(intent);
				Log.i(TAG,"Sent broadcast");
				return "UNAVAILABLE";
			default:
				intent.putExtra("RESULT", "OTHER");
				sendBroadcast(intent);
				Log.i(TAG,"Sent broadcast");
				return "OTHER";
			}
		}
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
			MessageManager msgmang = new MessageManager();
			SyncManager syncmang = new SyncManager();
			// While there are submission requests in the Queue, service the queue
			// with appropriate calls to executeTask() from the MessageManager or SyncManager
			while(mSubmitQueue.size() > 0) { // TODO this is a bit brute force-ish, but it will do for the moment
				Log.i(TAG, "mSubmitQueue != null");
				CommunicationState state = null;
				Intent intent = new Intent();
				try {
					Log.i(TAG, "Element in mSubmitQueue! Pop the top!");
					QueuedObject top = mSubmitQueue.getFirst();
					intent.setAction(top.getUid());
					switch (top.getType()) {
					case MESSAGE:
						// This is a Message object
						try {
							Log.i(TAG, "It's a MESSAGE!");
							// TODO see if they want to give "job offer" to peers
							state = (CommunicationState) msgmang.executeTask(top, mActiveRadio);
							break;
						} catch (CommunicationException e) {
							Log.e(TAG, e.getMessage());
							continue;
						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
							continue;
						}
					case SYNC:
						// This is a Sync object
						try {
							Log.i(TAG, "It's a SYNC!");
							// TODO see if they want to give "job offer" to peers
							state = (CommunicationState) syncmang.executeTask(top, mActiveRadio);
							break;
						} catch (CommunicationException e) {
							Log.e(TAG, e.getMessage());
							throw new NullPointerException();
						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
							continue;
						}
					default:
						Log.e(TAG, "It's a NULL! NOT GOOD!");
						throw new NullPointerException();
					} // switch(Type)

				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					continue;
				}
				
				switch(state) {
				// TODO As of now, there is no policy such a as a "3 strike" policy to help drain the 
				// queue in case of bad requests. Think about the policy we want implemented here.
				// This may involve an extra piece of member data to QueuedObject
					case SUCCESS:
						// The communication request has been
						// successfully submitted and completed
						mSubmitQueue.removeFirst();
						sendBroadcast(intent); 
						break;
					case FAILURE: 
					case IN_PROGRESS:
					case UNAVAILABLE:
					default:
						sendBroadcast(intent); 
						break;
				} // switch(state)
			} // which
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
			/*if (!mExecutor.isTerminated()) {
				mExecutor.shutdownNow();
			}
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
			mExecutor.submit(mThread);*/
			
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
