package org.opendatakit.submit.service;

import org.opendatakit.submit.flags.BroadcastExtraKeys;
import org.opendatakit.submit.flags.CommunicationState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AppReceiver extends BroadcastReceiver {
	
	private final Context context;
	
	private final SubmitService serv;

	public AppReceiver(String appUuid, SubmitService service, Context ctxt) {
		super();
		context = ctxt;
		serv = service;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(appUuid);
		context.registerReceiver(this, filter);
	}

	public void destroy() {
		context.unregisterReceiver(this);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(!intent.hasExtra(BroadcastExtraKeys.SUBMIT_OBJECT_ID) ||
				!intent.hasExtra(BroadcastExtraKeys.COMMUNICATION_STATE)) {
			System.err.println("Problem missing broadcast info");
			return;
		}
		
		// get the ID of the data object & 
		String sendUid = intent.getStringExtra(BroadcastExtraKeys.SUBMIT_OBJECT_ID);
		CommunicationState result = (CommunicationState) intent.getParcelableExtra(BroadcastExtraKeys.COMMUNICATION_STATE);

		// TODO put in legal checks
		serv.updateState(sendUid, result);	
	}

}
