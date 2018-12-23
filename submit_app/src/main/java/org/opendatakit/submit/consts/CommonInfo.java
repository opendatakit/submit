package org.opendatakit.submit.consts;

import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterfaceImpl;

public class CommonInfo {

    public UserDbInterfaceImpl dbInterface;
    public DbHandle db;
    public String appName;

    public CommonInfo(UserDbInterfaceImpl dbInterface, DbHandle db, String appName) {
        this.dbInterface = dbInterface;
        this.db = db;
        this.appName = appName;
    }
}
