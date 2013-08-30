package org.opendatakit.submit.service;

import org.opendatakit.submit.data.DataObject;
import org.opendatakit.submit.data.SendObject;

import org.opendatakit.submit.flags.MessageType;
import org.opendatakit.submit.flags.SyncType;

interface ClientRemote {
	
	String registerApplication(String app_uid);
	String register( String app_uid, inout DataObject data );
	String submit( String app_uid, inout DataObject data, inout SendObject send );
	void delete( String submit_uid );
	String[] getQueuedSubmissions( String app_uid );
	DataObject getDataObjectById( String submit_uid );
	SendObject getSendObjectById( String submit_uid );
	boolean onQueue( String submit_uid );
	int queueSize( );

}