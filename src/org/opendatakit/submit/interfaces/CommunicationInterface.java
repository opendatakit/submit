package org.opendatakit.submit.interfaces;

import java.util.ArrayList;

import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.route.QueuedObject;

public interface CommunicationInterface {

	void executeTask(QueuedObject queuedobj, Radio radio) throws CommunicationException;
}
