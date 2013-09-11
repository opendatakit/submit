package org.opendatakit.submit.service;

import org.opendatakit.submit.data.DataPropertiesObject;
import org.opendatakit.submit.data.SendObject;

import org.opendatakit.submit.flags.MessageType;
import org.opendatakit.submit.flags.SyncType;

interface ClientRemote {
	
	String registerApplication(String app_uid);
	String register( String app_uid, inout DataPropertiesObject data );
	String submit( String app_uid, inout DataPropertiesObject data, inout SendObject send );
	void delete( String submit_uid );
	String[] getQueuedSubmissions( String app_uid );
	DataPropertiesObject getDataObjectById( String submit_uid );
	SendObject getSendObjectById( String submit_uid );
	boolean onQueue( String submit_uid );
	int queueSize( );

}