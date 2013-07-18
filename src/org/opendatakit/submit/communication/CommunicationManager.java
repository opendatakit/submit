package org.opendatakit.submit.communication;

import java.io.IOException;
import java.util.List;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.MismatchException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CommunicationManager extends Service implements MessageInterface, SyncInterface {


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Object executeTask( Object obj, List<Radio> radions_on, Object type ) throws MismatchException {
		// TODO
		return null;
		
	}

	void updateRadios(List<Radio> radios) {
		
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

	@Override
	public Object send(String dest, String msg) throws IOException,
			MessageException {
		// TODO Auto-generated method stub
		return null;
	}
}
