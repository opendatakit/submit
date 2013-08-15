package org.opendatakit.submit.route;

import org.opendatakit.submit.data.QueuedObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.interfaces.CommunicationInterface;

public class CommunicationManager implements CommunicationInterface {
	SubmitObject mSubmitObject = null;
	Radio mRadio = null;
	
	public CommunicationManager(SubmitObject submitobj) {
		
	}
	@Override
	public Object executeTask(QueuedObject queuedobj, Radio radio)
			throws CommunicationException {
		// TODO Auto-generated method stub
		return null;
	}

}
