package org.opendatakit.submit.route;

import java.io.IOException;
import java.util.LinkedList;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.SyncDirection;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class RoutingService extends Service implements MessageInterface, SyncInterface {

	private LinkedList<QueuedObject> mSubmitQueue = null;
	
	private final RouteRemote.Stub mBinder = new RouteRemote.Stub() {

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

	/*
	 * Service methods
	 */
	@Override
	public IBinder onBind(Intent intent) {
		
		return mBinder;
	}
	
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
	 * Private methods
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

}
