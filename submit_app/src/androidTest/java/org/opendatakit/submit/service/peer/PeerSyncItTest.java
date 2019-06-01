package org.opendatakit.submit.service.peer;

import android.Manifest;
import android.content.ContentValues;
import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.service.AbstractSyncItTest;
import org.opendatakit.submit.service.SyncDoneCondition;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.SyncStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PeerSyncItTest extends AbstractSyncItTest {
  private static final String TAG = PeerSyncItTest.class.getSimpleName();

//  private static final String PRIMARY_APP_NAME = "android_test_" + PeerSyncItTest.class.getSimpleName();
  private static final String PRIMARY_APP_NAME = "default";
//  private static final String SYNC_INIT_APP_NAME = "submit_android_test_" + PeerSyncItTest.class.getSimpleName() + "_init";
  private static final String SYNC_INIT_APP_NAME = SubmitUtil.getSecondaryAppName(PRIMARY_APP_NAME);
  private static final String PEER_PRIMARY_APP_NAME = "default" + PeerSyncItTest.class.getSimpleName() + "_peer";
  private static final String SYNC_PEER_APP_NAME = SubmitUtil.getSecondaryAppName(PEER_PRIMARY_APP_NAME);

  @Rule
  public final GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  );

  @Override
  protected Iterable<String> getTestAppNames() {
    return Arrays.asList(PRIMARY_APP_NAME, SYNC_INIT_APP_NAME, PEER_PRIMARY_APP_NAME, SYNC_PEER_APP_NAME);
  }

  @Override
  protected void createTables(String appName) throws ServicesAvailabilityException {
    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_1",
        buildColumnList(1, true)
    );

    getDb().privilegedUpdateTableETags(
        appName,
        getDbHandle(appName),
        "table_1",
        "schemaETagtable_1",
        null
    );
  }

  @Override
  protected void insertTestData(String appName) throws ServicesAvailabilityException, ActionNotAuthorizedException {
    OrderedColumns orderedColumns = getDb().getUserDefinedColumns(appName, getDbHandle(appName), "table_1");

    for (int i = 1000; i < 1010; i++) {
      ContentValues cv = new ContentValues();

      cv.put(DataTableColumns.ROW_ETAG, i * 2);
      cv.put(DataTableColumns.SYNC_STATE, SyncState.synced.toString());

//      cv.put(SubmitColumns.P_ID, "row_1_" + i);
//      cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_SYNCED);
//      cv.put(SubmitColumns.TRANSFER_ID, "");
//      cv.put(SubmitColumns.DEVICE_ID, "");

      cv.put("column1_1", "value1_1_" + i);
      cv.put("column1_2", "value1_2_" + i);
      cv.put("column1_3", "value1_3_" + i);

      String rowId = "row_1_" + i;

      if (appName.equals(PRIMARY_APP_NAME)) {
//        rowId = String.valueOf(i);

        if (i == 1000) {
          cv.put(DataTableColumns.ROW_ETAG, i * 3);
//          cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_MODIFIED);
          cv.put(DataTableColumns.SYNC_STATE, SyncState.changed.toString());
          cv.put("column1_3", "value_changed");
        }
      }

      getDb().insertRowWithId(
          appName,
          getDbHandle(appName),
          "table_1",
          orderedColumns,
          cv,
          rowId
      );
    }
  }

  @Test
  public void emptyLocal() throws ServicesAvailabilityException, ActionNotAuthorizedException, RemoteException, InterruptedException, IOException {
    createTables(PRIMARY_APP_NAME);
    createTables(SYNC_INIT_APP_NAME);
    createTables(SYNC_PEER_APP_NAME);
    createTables(PEER_PRIMARY_APP_NAME);

//    insertTestData(PRIMARY_APP_NAME);
    insertTestData(PEER_PRIMARY_APP_NAME);

//    OrderedColumns orderedColumns = getDb().getUserDefinedColumns(SYNC_INIT_APP_NAME, getDbHandle(SYNC_INIT_APP_NAME), "table_1");
//
//    for (int i = 1005; i < 1015; i++) {
//      ContentValues cv = new ContentValues();
//
//      cv.put(DataTableColumns.ROW_ETAG, i * 3);
////      cv.put(DataTableColumns.ROW_ETAG, i * 2);
//      cv.put(DataTableColumns.SYNC_STATE, SyncState.changed.toString());
//
//      cv.put(SubmitColumns.P_ID, "row_1_" + i);
//      cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_SYNCED);
//      cv.put(SubmitColumns.TRANSFER_ID, "");
//      cv.put(SubmitColumns.DEVICE_ID, "");
//
//      cv.put("column1_1", "value1_10_" + i);
//      cv.put("column1_2", "value1_20_" + i);
//      cv.put("column1_3", "value1_30_" + i);
//
//      getDb().insertRowWithId(
//          SYNC_INIT_APP_NAME,
//          getDbHandle(SYNC_INIT_APP_NAME),
//          "table_1",
//          orderedColumns,
//          cv,
//          "row_1_" + i
//      );
//    }

//    setSyncUrl(SYNC_INIT_APP_NAME, "peer://localhost:8080/" + SYNC_PEER_APP_NAME);
//    localSync(SYNC_INIT_APP_NAME);
    localSync(SYNC_PEER_APP_NAME);

    getPeerSyncServer().start();
    peerSync(SYNC_INIT_APP_NAME, SYNC_PEER_APP_NAME);

//    localSync(SYNC_INIT_APP_NAME);
//    localSync(SYNC_PEER_APP_NAME);
  }

  @Test
  public void manyConflicts() throws ServicesAvailabilityException, ActionNotAuthorizedException {
    createTables(SYNC_INIT_APP_NAME);

    OrderedColumns orderedColumns = getDb().getUserDefinedColumns(SYNC_INIT_APP_NAME, getDbHandle(SYNC_INIT_APP_NAME), "table_1");

    for (int i = 0; i < 100; i++) {
      ContentValues cv = new ContentValues();

      cv.put(DataTableColumns.ROW_ETAG, i * 3);
      cv.put(DataTableColumns.SYNC_STATE, SyncState.synced.toString());

      cv.put(SubmitColumns.P_ID, "row_1_1");
      cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_DIVERGENT);
      cv.put(SubmitColumns.TRANSFER_ID, "");
      cv.put(SubmitColumns.DEVICE_ID, "");

      cv.put("column1_1", "value1_10_" + i);
      cv.put("column1_2", "value1_20_" + i);
      cv.put("column1_3", "value1_30_" + i);

      getDb().insertRowWithId(
          SYNC_INIT_APP_NAME,
          getDbHandle(SYNC_INIT_APP_NAME),
          "table_1",
          orderedColumns,
          cv,
          "row_1_" + i
      );
    }
  }

  private void localSync(String appName) throws RemoteException {
    setSyncUrl(appName, "submit://sync");

    getSyncService().clearAppSynchronizer(appName);
    assertEquals(SyncStatus.NONE, getSyncService().getSyncStatus(appName));
    getSyncService().synchronizeWithServer(appName, SyncAttachmentState.NONE);

    await().until(new SyncDoneCondition(getSyncService(), appName));

    assertEquals(SyncStatus.SYNC_COMPLETE, getSyncService().getSyncStatus(appName));
  }

  private void peerSync(String localAppName, String remoteAppName) throws RemoteException {
    setSyncUrl(localAppName, "peer://localhost:8080/" + remoteAppName);

    getSyncService().clearAppSynchronizer(localAppName);
    assertEquals(SyncStatus.NONE, getSyncService().getSyncStatus(localAppName));
    getSyncService().synchronizeWithServer(localAppName, SyncAttachmentState.NONE);

    await().until(new SyncDoneCondition(getSyncService(), localAppName));

    assertEquals(SyncStatus.SYNC_COMPLETE, getSyncService().getSyncStatus(localAppName));
  }

//  private void setAllToChanged(String appName, String tableId) {
//    DbHandle handle = null;
//    try {
//      handle = getDb()
//    }
//  }
}
