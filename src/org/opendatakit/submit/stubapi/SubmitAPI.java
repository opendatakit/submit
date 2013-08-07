package org.opendatakit.submit.stubapi;

import java.io.IOException;

import org.opendatakit.submit.exceptions.MessageException;
import org.opendatakit.submit.exceptions.SyncException;
import org.opendatakit.submit.flags.CommunicationState;
import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.interfaces.MessageInterface;
import org.opendatakit.submit.interfaces.SyncInterface;

public class SubmitAPI implements SyncInterface, MessageInterface {

	@Override
	public Object send(String dest, String msg, String uid) throws IOException,
			MessageException {
		return CommunicationState.SUCCESS;
	}

	@Override
	public Object create(SyncType st, String dest, String pathname, String uid)
			throws IOException, SyncException {
		return CommunicationState.SUCCESS;
	}

	@Override
	public Object download(SyncType st, String dest, String pathname, String uid)
			throws IOException, SyncException {
		return CommunicationState.SUCCESS;
	}

	@Override
	public Object delete(SyncType st, String dest, String pathname, String uid)
			throws IOException, SyncException {
		return CommunicationState.SUCCESS;
	}

	@Override
	public Object sync(SyncType st, String dest, String pathname, String uid)
			throws IOException, SyncException {
		return CommunicationState.SUCCESS;
	}

}
