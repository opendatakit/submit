package org.opendatakit.submit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AppReceiver extends BroadcastReceiver {
	
	private final Context context;

	public AppReceiver(String appUuid, Context ctxt) {
		super();
		context = ctxt;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(appUuid);
		context.registerReceiver(this, filter);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

	}

}
