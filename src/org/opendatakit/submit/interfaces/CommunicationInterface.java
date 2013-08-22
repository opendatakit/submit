package org.opendatakit.submit.interfaces;

import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.CommunicationException;
import org.opendatakit.submit.flags.Radio;

public interface CommunicationInterface {

	Object executeTask(SubmitObject submit, Radio radio) throws CommunicationException;
}
