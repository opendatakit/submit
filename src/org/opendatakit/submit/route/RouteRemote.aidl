package org.opendatakit.submit.route;

import org.opendatakit.submit.flags.MessageType;
import org.opendatakit.submit.flags.SyncType;

interface RouteRemote{

	String send( String uri, String pathname );
	String create( inout SyncType st, String uri, String pathname );
	String download( inout SyncType st, String uri, String pathname );
	String sync( inout SyncType st, String uri, String pathname );
	String delete( inout SyncType st, String uri, String pathname );
	boolean onQueue( String uid );
	int queueSize( );

}