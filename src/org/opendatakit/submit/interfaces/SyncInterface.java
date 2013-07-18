package org.opendatakit.submit.interfaces;

import java.io.IOException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.SyncType;

/**
 * SyncInterface
 * Abstract interface to be implemented by all
 * layers of Submit that interact with data flow.
 * 
 * @author mvigil
 *
 */
public interface SyncInterface {
	
	/**
	 * When a new Sync object is created and needs to 
	 * be uploaded and "created" across devices.
	 * @param dest
	 * 			String representing server URI
	 * @param pathname
	 * 			String representing path/to/data
	 * @return
	 * @throws IOException
	 * @throws SyncException
	 */
	Object create( SyncType st, String dest, String pathname ) throws IOException, SyncException;
	
	/**
	 * When a new Sync object needs to be downloaded
	 * from another device or server.
	 * @param dest
	 * 			String representing server URI
	 * @param pathname
	 * 			String representing path/to/data
	 * @return
	 * @throws IOException
	 * @throws SyncException
	 */
	Object download( SyncType st, String dest, String pathname ) throws IOException, SyncException;
	
	/**
	 * When a Sync object needs to be removed from 
	 * other devices and servers.
	 * @param dest
	 * 			String representing server URI
	 * @param pathname
	 * 			String representing path/to/data
	 * @return
	 * @throws IOException
	 * @throws SyncException
	 */
	Object delete( SyncType st, String dest, String pathname ) throws IOException, SyncException;
	
	/**
	 * When a Sync object needs to be synchronized
	 * across devices and servers
	 * @param uri
	 * 			String representing server URI
	 * @param pathname
	 * 			String representing path/to/data
	 * @return
	 * @throws IOException
	 * @throws SyncException
	 */
	Object sync( SyncType st, String dest, String pathname ) throws IOException, SyncException;
	
}
