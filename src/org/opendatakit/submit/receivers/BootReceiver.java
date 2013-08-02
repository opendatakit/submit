package org.opendatakit.submit.receivers;

import org.opendatakit.submit.scheduling.SubmitService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Listens for different actions
 * and starts SubmitService
 * @author mvigil
 *
 */
public class BootReceiver extends BroadcastReceiver {
	// Start SubmitService when:
	// phone boots
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("BootReceiver", "Booted up and starting SubmitService");
		Intent submitService = new Intent(context, SubmitService.class);
		context.startService(submitService);
	}

}
