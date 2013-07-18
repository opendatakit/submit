package org.opendatakit.submit.client;

import java.io.IOException;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SubmitService extends Service implements MessageInterface, SyncInterface{

	@Override
	public Object create(String uri, String pathname) throws IOException,
			SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object download(String uri, String pathname) throws IOException,
			SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object delete(String uri, String pathname) throws IOException,
			SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object sync(String uri, String pathname) throws IOException,
			SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(String msg, String dest) throws IOException,
			MessageException {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
