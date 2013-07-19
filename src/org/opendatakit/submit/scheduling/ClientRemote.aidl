package org.opendatakit.submit.scheduling;

import org.opendatakit.submit.flags.MessageType;
import org.opendatakit.submit.flags.SyncType;

interface ClientRemote {

	String send( String uri, String pathname );
	String create( inout SyncType st, String uri, String pathname );
	String download( inout SyncType st, String uri, String pathname );
	String sync( inout SyncType st, String uri, String pathname );
	String delete( inout SyncType st, String uri, String pathname );
	boolean onQueue( String uid );
	int queueSize( );

}