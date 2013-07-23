package org.opendatakit.submit.scheduling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.opendatakit.submit.route.QueuedObject;
import org.opendatakit.submit.scheduling.ClientRemote;
import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
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

public class SubmitService extends Service implements MessageInterface, SyncInterface{

	private LinkedList<QueuedObject> mSubmitQueue = null;
	public static Radio mActiveRadio = null;
	public static Radio mActiveP2PRadio = null;
	
	private final ClientRemote.Stub mBinder = new ClientRemote.Stub() {
		
		@Override
		public String send(String uri, String pathname) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String create(SyncType st, String uri, String pathname)
				throws RemoteException {
			return this.create(st, uri, pathname);
		}

		@Override
		public String download(SyncType st, String uri, String pathname)
				throws RemoteException {
			return this.download(st, uri, pathname);
		}

		@Override
		public String sync(SyncType st, String uri, String pathname)
				throws RemoteException {
			return this.sync(st, uri, pathname);
		}

		@Override
		public String delete(SyncType st, String uri, String pathname)
				throws RemoteException {
			return this.delete(st, uri, pathname);
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
	
	@Override
	public void onCreate() {
		mSubmitQueue = new LinkedList<QueuedObject>();
		// TODO Start thread
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
    public void onDestroy() {
        // TODO
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/*
	 * Sync and Message methods
	 */
	@Override
	public String send(String dest, String msg) throws IOException,
			MessageException {
		QueuedObject submit = new QueuedObject(dest, msg);
		mSubmitQueue.add(submit);
		return submit.getUid();
	}
	
	@Override
	public Object create(SyncType st, String dest, String pathname)
			throws IOException, SyncException {
		QueuedObject submit = new QueuedObject(st, SyncDirection.CREATE, dest, pathname);
		mSubmitQueue.add(submit);
		return submit.getUid();
	}

	@Override
	public Object download(SyncType st, String dest, String pathname)
			throws IOException, SyncException {
		QueuedObject submit = new QueuedObject(st, SyncDirection.DOWNLOAD, dest, pathname);
		mSubmitQueue.add(submit);
		return submit.getUid();
	}

	@Override
	public Object delete(SyncType st, String dest, String pathname)
			throws IOException, SyncException {
		QueuedObject submit = new QueuedObject(st, SyncDirection.DELETE, dest, pathname);
		mSubmitQueue.add(submit);
		return submit.getUid();
	}

	@Override
	public Object sync(SyncType st, String dest, String pathname)
			throws IOException, SyncException {
		QueuedObject submit = new QueuedObject(st, SyncDirection.SYNC, dest, pathname);
		mSubmitQueue.add(submit);
		return submit.getUid();
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
	
	/*
	 * Runnables
	 */
	
	/**
	 * sendToManager Runnable
	 * Gets passed to the routeInBackgroundThread;
	 * based on the TYPE of the object on the top of the queue
	 * it passes off to the MessageManager or the SyncManager
	 */
	private Runnable sendToManager = new Runnable() {

		@Override
		public void run() {
			while(mSubmitQueue.size() > 0) { // TODO this is a bit brute forcish, but it will do for the moment
				try {
					QueuedObject top = mSubmitQueue.getFirst();
					switch (top.getType()) {
					case MESSAGE:
						// TODO send to MessageManager
					case SYNC:
						// TODO send to SyncManager
					default:

					}

				} catch (NullPointerException npe) {
					// TODO
				}
			}
		}
		
	};
	
	/*
	 * Routing thread
	 */
	public static Thread routeInBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					
				}
			}
		};
		t.start();
		return t;
	}
	
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
			ConnectivityManager connMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// WiFi
				mActiveRadio = Radio.WIFI;
				// TODO determine if you want to try WiFi-Direct at any point here

			}
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

				if (isConnectionFast(networkInfo.getType(),
						networkInfo.getSubtype())) {
					// "High speed" cellular connection
					mActiveRadio = Radio.CELL;
				} else {
					// Low speed cellular connection
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
