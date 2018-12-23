package org.opendatakit.submit.receivers;

import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.service.SubmitService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

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
	

	private final String TAG = ChannelMonitor.class.getName();
	private SubmitService mService = null;
	private Radio mActiveRadio = null;
	
	public ChannelMonitor(SubmitService service) {
		mService = service;
	}
	
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
			// Update the active Radio in 
			mService.setActiveRadio(mActiveRadio);
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

