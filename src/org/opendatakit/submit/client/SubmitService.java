package org.opendatakit.submit.client;

import java.io.IOException;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SubmitService extends Service implements MessageInterface, SyncInterface{


	@Override
	public void send(String dest, String msg) throws IOException,
			MessageException {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object create(SyncType st, String uri, String pathname)
			throws IOException, SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object download(SyncType st, String uri, String pathname)
			throws IOException, SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object delete(SyncType st, String uri, String pathname)
			throws IOException, SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object sync(SyncType st, String uri, String pathname)
			throws IOException, SyncException {
		// TODO Auto-generated method stub
		return null;
	}

}
