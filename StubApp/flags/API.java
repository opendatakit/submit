package org.opendatakit.submit.flags;

/**
 * API
 * List of API resources available to use with Submit.
 * 
 * NOTE: If third-party developer wishes to add support
 * for another API, a corresponding value needs to 
 * be defined HERE if they wish for it to be recognized.
 * @author mvigil
 * 
 */
public enum API {
	SMS, // SMS for messages only
	GCM, // GCM for messages only
	ODKv2, // ODKv2 for stored data
	STUB // for testing
}
