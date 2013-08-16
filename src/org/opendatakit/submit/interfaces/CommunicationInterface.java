package org.opendatakit.submit.interfaces;

import java.util.ArrayList;

import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.data.QueuedObject;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.CommunicationException;

public interface CommunicationInterface {

	Object executeTask(SubmitObject submit, Radio radio) throws CommunicationException;
}
