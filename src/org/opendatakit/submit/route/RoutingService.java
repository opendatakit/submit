package org.opendatakit.submit.route;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RoutingService extends Service implements MessageInterface, SyncInterface, Queue {

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

	/*
	 * Service methods
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Queue methods
	 */
	@Override
	public boolean addAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray(Object[] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Object e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean offer(Object e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object peek() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove() {
		// TODO Auto-generated method stub
		return null;
	}

}
