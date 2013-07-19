package org.opendatakit.submit.interfaces;

import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.route.QueuedObject;

public interface CommunicationInterface {

	void executeTask(QueuedObject queuedobj) throws CommunicationException;
}
