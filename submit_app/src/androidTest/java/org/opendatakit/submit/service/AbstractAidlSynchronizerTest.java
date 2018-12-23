package org.opendatakit.submit.service;

import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.rule.ServiceTestRule;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.service.AidlDbInterface;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.InternalUserDbInterfaceAidlWrapperImpl;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.database.service.UserDbInterfaceImpl;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public abstract class AbstractAidlSynchronizerTest {
  private static final String TAG = AbstractAidlSynchronizerTest.class.getSimpleName();

  @Rule
  public final ServiceTestRule dbServiceRule = ServiceTestRule.withTimeout(30, TimeUnit.SECONDS);

  private UserDbInterface dbInterface;
  private Map<String, DbHandle> dbHandleMap;

  protected UserDbInterface getDb() {
    return dbInterface;
  }

  protected DbHandle getDbHandle(String appName) {
    return dbHandleMap == null ? null : dbHandleMap.get(appName);
  }

  protected abstract Iterable<String> getTestAppNames();

  @Before
  public void setUp() throws Exception {
    checkAndCreateDir();
    bindToDb();
    openAppDb();
  }

  @After
  public void tearDown() {
    try {
      closeAppDb();
    } catch (ServicesAvailabilityException e) {
      Log.e(TAG, "tearDown: failed to close databases", e);
      fail();
    } finally {
      try {
        cleanDir();
      } catch (IOException e) {
        Log.e(TAG, "tearDown: failed to delete directories", e);
        fail();
      }
    }
  }

  protected void checkAndCreateDir() {
    for (String appName : getTestAppNames()) {
      // prevent tests from running if the environment was not cleaned up properly
      assertFalse(appName + " should not exist", new File(ODKFileUtils.getAppFolder(appName)).exists());

      ODKFileUtils.assertDirectoryStructure(appName);
    }
  }

  protected void bindToDb() throws TimeoutException {
    IBinder dbBinder = dbServiceRule.bindService(new Intent()
        .setClassName(
            IntentConsts.Database.DATABASE_SERVICE_PACKAGE,
            IntentConsts.Database.DATABASE_SERVICE_CLASS
        )
    );

    dbInterface = new UserDbInterfaceImpl(
        new InternalUserDbInterfaceAidlWrapperImpl(AidlDbInterface.Stub.asInterface(dbBinder)));
    assertNotNull(dbInterface);
  }

  protected void openAppDb() throws ServicesAvailabilityException {
    dbHandleMap = new HashMap<>();

    for (String appName : getTestAppNames()) {
      DbHandle handle = getDb().openDatabase(appName);

      assertNotNull(handle);
      dbHandleMap.put(appName, handle);
    }
  }

  protected void closeAppDb() throws ServicesAvailabilityException {
    for (String appName : getTestAppNames()) {
      if (getDbHandle(appName) != null) {
        getDb().closeDatabase(appName, getDbHandle(appName));
      }
    }
  }

  protected void cleanDir() throws IOException {
    for (String appName : getTestAppNames()) {
      FileUtils.deleteDirectory(new File(ODKFileUtils.getAppFolder(appName)));
    }
  }

  protected void createTables(String appName) throws ServicesAvailabilityException {
    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_1",
        buildColumnList(1, false)
    );

    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_2",
        buildColumnList(2, false)
    );

    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_3",
        buildColumnList(3, false)
    );
  }

  protected void insertTestData(String appName) throws ServicesAvailabilityException, ActionNotAuthorizedException {
    ContentValues cv = new ContentValues();
    cv.put("column1_1", "value1_1");
    cv.put("column1_2", "value1_2");
    cv.put("column1_3", "value1_3");

    getDb().insertRowWithId(
        appName,
        getDbHandle(appName),
        "table_1",
        getDb().getUserDefinedColumns(appName, getDbHandle(appName), "table_1"),
        cv,
        "row_1_1"
    );

    getDb().insertRowWithId(
        appName,
        getDbHandle(appName),
        "table_1",
        getDb().getUserDefinedColumns(appName, getDbHandle(appName), "table_1"),
        cv,
        "row_1_2"
    );
  }

  protected ColumnList buildColumnList(int sequence, boolean isSubmitTable, Column... extraColumns) {
    List<Column> columns = new ArrayList<>(Arrays.asList(extraColumns));

    for (int i = 1; i <= 3; i++) {
      String name = "column" + sequence + "_" + i;

      columns.add(new Column(name, name, ElementDataType.string.toString(), null));
    }

    if (isSubmitTable) {
      for (String sCol : SubmitColumns.SUBMIT_COLUMNS) {
        columns.add(new Column(sCol, sCol, ElementDataType.string.toString(), null));
      }
    }

    return new ColumnList(columns);
  }
}
