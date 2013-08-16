package org.opendatakit.submit.channels;

import org.opendatakit.submit.flags.DataSize;

public class CellLowBand extends CommunicationChannel {

	public CellLowBand(long cost) {
		super(DataSize.SMALL);
		super.setCostPerMbps(cost);
	}
	
}
