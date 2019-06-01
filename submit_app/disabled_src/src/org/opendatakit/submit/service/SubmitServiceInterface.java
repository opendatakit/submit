package org.opendatakit.submit.service;

import java.util.ArrayList;
import java.util.UUID;

import org.opendatakit.submit.data.DataPropertiesObject;
import org.opendatakit.submit.data.SendObject;
import org.opendatakit.submit.data.SubmitObject;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

/**
 * Implementation of ClientRemote Stub for 
 * Applications to interface with SubmitService
 * @author mvigil
 *
 */
public class SubmitServiceInterface extends ClientRemote.Stub {
	
	private final String TAG = SubmitServiceInterface.class.getName();
	
	/* Fields that correspond with SubmitService */
	private SubmitService mService = null;
	private Context mContext = null;
	
	public SubmitServiceInterface(SubmitService service, Context context) {
		mService = service;
		mContext = context;
		Log.i(TAG, "Constructed SubmitServiceInterface");
	}
	
	@Override
	public String submit(String app_uid, DataPropertiesObject data, SendObject send)
			throws RemoteException {
		
		Log.i(TAG, "In submit()");
		SubmitObject submit = new SubmitObject(app_uid, data, send);
		if (submit == null) {
			Log.e(TAG, "!!!!!!!!SubmitObject is null!!!!!!!!");
		}
		mService.addSubmitObject(submit);
		//mService.manageQueue();
		return submit.getSubmitID();
	}
	
	@Override
	public String register(String app_uid, DataPropertiesObject data)
			throws RemoteException {
		Log.i(TAG, "In register()");
				
		SubmitObject submit = new SubmitObject(app_uid, data, null);		
		if (submit == null) {
			Log.e(TAG, "!!!!!!!!SubmitObject is null!!!!!!!!");
		}
		/* Map submission ID to DataPropertiesObject */
		// Here, we assume that the SubmitID
		// is unique, so we do not check before
		// putting it into the map

		mService.addSubmitObject(submit);
		//mService.manageQueue();
		return submit.getSubmitID();
	}
	
	@Override
	public int queueSize() throws RemoteException {
		Log.i(TAG, "In queueSize()");
		return mService.getSubmitQueueSize();
	}
	
	@Override
	public boolean onQueue(String submit_uid) throws RemoteException {
		Log.i(TAG, "In onQueue()");
		return mService.onSubmitQueue(submit_uid);	
	}
	
	@Override
	public SendObject getSendObjectById(String submit_uid) throws RemoteException {
		return mService.getSubmitObjectFromSubmitQueue(submit_uid).getAddress();
	}
	
	@Override
	public String[] getQueuedSubmissions(String app_uid) throws RemoteException {
		String[] ids = null;
		ArrayList<String> idlist = mService.getSubmitIdsFromAppId(app_uid);
		
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
	public DataPropertiesObject getDataObjectById(String submit_uid) throws RemoteException {
		return mService.getSubmitObjectFromSubmitQueue(submit_uid).getData();
	}
	
	@Override
	public void delete(String submit_uid )
			throws RemoteException {
		Log.i(TAG, "In delete()");
		
	}

	@Override
	public String registerApplication(String app_uid)
			throws RemoteException {
		
		UUID uid = UUID.randomUUID();
		
		String appUid = uid.toString();
		AppReceiver listener = new AppReceiver(appUid, mService, mContext);
		mService.addAppReceiver(listener);
		return appUid;
	}
}
