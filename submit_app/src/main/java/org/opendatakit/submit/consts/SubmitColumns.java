package org.opendatakit.submit.consts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubmitColumns {
    public static final String P_STATE = "p_state";
    public static final String P_ID = "p_id";
    public static final String TRANSFER_ID = "p_transfer_id";
    public static final String DEVICE_ID = "p_device_id";

    public static final Set<String> SUBMIT_COLUMNS;
    static {
        Set<String> columns = new HashSet<>();

        columns.add(P_STATE);
        columns.add(P_ID);
        columns.add(TRANSFER_ID);
        columns.add(DEVICE_ID);

        SUBMIT_COLUMNS = Collections.unmodifiableSet(columns);
    }
}
