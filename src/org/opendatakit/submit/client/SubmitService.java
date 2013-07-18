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
import android.os.RemoteException;

public class SubmitService extends Service implements MessageInterface, SyncInterface{

	private final ClientRemote.Stub mBinder = new ClientRemote.Stub() {
		
		@Override
		public String sync(SyncType st, String uri, String pathname)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String send(String uri, String pathname) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String download(SyncType st, String uri, String pathname)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String delete(SyncType st, String uri, String pathname)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String create(SyncType st, String uri, String pathname)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean onQueue(String uid) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int queueSize() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public Object send(String dest, String msg) throws IOException,
			MessageException {
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
