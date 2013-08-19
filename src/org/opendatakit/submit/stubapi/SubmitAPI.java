package org.opendatakit.submit.stubapi;

import java.io.IOException;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

public class SubmitAPI {


	public Object get() {
		return CommunicationState.SUCCESS;
	}


	public Object put() {
		return CommunicationState.SUCCESS;
	}

	// TODO 


	public Object delete() {
		return CommunicationState.SUCCESS;
	}

}
