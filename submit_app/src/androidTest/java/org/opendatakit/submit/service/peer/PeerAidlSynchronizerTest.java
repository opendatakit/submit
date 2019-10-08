package org.opendatakit.submit.service.peer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.UserInfo;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.service.AbstractAidlSynchronizerTest;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;
import org.opendatakit.sync.service.entity.ParcelableColumn;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;
import org.opendatakit.sync.service.entity.ParcelableTableResource;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.sync.service.logic.IAidlSynchronizer;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.opendatakit.submit.matcher.TableDefinitionResourceMatchers.hasColumn;
import static org.opendatakit.submit.matcher.TableResourceMatchers.hasDefinitionUri;
import static org.opendatakit.submit.matcher.TableResourceMatchers.hasSchemaETag;
import static org.opendatakit.submit.matcher.TableResourceMatchers.hasTableId;

@LargeTest
public class PeerAidlSynchronizerTest extends AbstractAidlSynchronizerTest {
  private static final String TAG = PeerAidlSynchronizerTest.class.getSimpleName();

  private static final String TEST_SUBMIT_APP_NAME =
      "submit_androidTest_" + PeerAidlSynchronizerTest.class.getSimpleName();

  @Rule
  public final ServiceTestRule serverRule = new ServiceTestRule();

  @Rule
  public final ServiceTestRule clientRule = new ServiceTestRule();

  @Rule
  public final GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  );

  private PeerSyncServer peerSyncServer;
  private IAidlSynchronizer peerAidlSynchronizer;

  @Override
  protected Iterable<String> getTestAppNames() {
    return Collections.singleton(TEST_SUBMIT_APP_NAME);
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    IBinder serverBinder = serverRule.bindService(new Intent()
        .setClass(InstrumentationRegistry.getTargetContext(), PeerSyncServerService.class));

    peerSyncServer = ((PeerSyncServerService.PeerSyncServerBinder) serverBinder).getServer();
    startServer();

    CommonToolProperties
        .get(InstrumentationRegistry.getTargetContext(), TEST_SUBMIT_APP_NAME)
        .setProperties(Collections.singletonMap(
            CommonToolProperties.KEY_SYNC_SERVER_URL,
            IntentConsts.SubmitPeerSync.URI_SCHEME + "localhost:8080/" + TEST_SUBMIT_APP_NAME
        ));

    IBinder binder = clientRule.bindService(new Intent()
        .setClassName(
            IntentConsts.SubmitPeerSync.PACKAGE_NAME,
            IntentConsts.SubmitPeerSync.SERVICE_CLASS_NAME
        )
        .putExtra(IntentConsts.INTENT_KEY_APP_NAME, TEST_SUBMIT_APP_NAME)
    );

    peerAidlSynchronizer = PeerAidlSynchronizer.Stub.asInterface(binder);
    assertNotNull(peerAidlSynchronizer);

    createTables(TEST_SUBMIT_APP_NAME);
    insertTestData(TEST_SUBMIT_APP_NAME);
  }

  @After
  @Override
  public void tearDown() {
    super.tearDown();

    stopServer();
  }

  private void startServer() throws IOException {
    assertNotNull(peerSyncServer);

    peerSyncServer.stop();
    peerSyncServer.start();
  }

  private void stopServer() {
    peerSyncServer.stop();
  }

  @Override
  protected void createTables(String appName) throws ServicesAvailabilityException {
    super.createTables(appName);

    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_4",
        buildColumnList(4, true)
    );

    getDb().createOrOpenTableWithColumns(
        appName,
        getDbHandle(appName),
        "table_5",
        buildColumnList(4, true)
    );
  }

  @Override
  protected void insertTestData(String appName) throws ServicesAvailabilityException, ActionNotAuthorizedException {
    super.insertTestData(appName);

    for (int table : new int[]{4, 5}) {
      OrderedColumns orderedColumns = getDb().getUserDefinedColumns(appName, getDbHandle(appName), "table_" + table);

      for (int i = 1000; i < 1050; i++) {
        ContentValues cv = new ContentValues();

        cv.put(DataTableColumns.ROW_ETAG, "row_etag" + "_" + (i % 3));

        cv.put(SubmitColumns.P_ID, "p_id_" + i);
        cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_SYNCED);
        cv.put(SubmitColumns.TRANSFER_ID, "");
        cv.put(SubmitColumns.DEVICE_ID, "");

        cv.put("column" + 4 + "_1", "value" + table + "_" + i);
        cv.put("column" + 4 + "_2", "value" + table + "_" + i);
        cv.put("column" + 4 + "_3", "value" + table + "_" + i);

        getDb().insertRowWithId(
            appName,
            getDbHandle(appName),
            "table_" + table,
            orderedColumns,
            cv,
            "row_" + + table + "_" + i
        );
      }

      // repeat some rows
      for (int i = 1000; i < 1010; i++) {
        ContentValues cv = new ContentValues();

        cv.put(DataTableColumns.ROW_ETAG, "row_etag" + "_" + (i % 3 + table));

        cv.put(SubmitColumns.P_ID, "p_id_" + i);
        cv.put(SubmitColumns.P_STATE, SubmitSyncStates.P_SYNCED);
        cv.put(SubmitColumns.TRANSFER_ID, "");
        cv.put(SubmitColumns.DEVICE_ID, "");

        cv.put("column" + 4 + "_1", "value" + table + "_" + i);
        cv.put("column" + 4 + "_2", "value" + table + "_" + i);
        cv.put("column" + 4 + "_3", "value" + table + "_" + i);

        getDb().insertRowWithId(
            appName,
            getDbHandle(appName),
            "table_" + table,
            orderedColumns,
            cv,
            "row_" + + table + "_" + i + "_p"
        );
      }
    }
  }

  @Test
  public void verifyServerSupportsAppName() throws RemoteException {
    peerAidlSynchronizer.verifyServerSupportsAppName();
  }

  @Test
  public void getUserRolesAndDefaultGroup() throws RemoteException {
    assertNull(peerAidlSynchronizer.getUserRolesAndDefaultGroup());
  }

  @Test
  public void getUsers() throws RemoteException {
    assertThat(peerAidlSynchronizer.getUsers(), emptyCollectionOf(UserInfo.class));
  }

  @Test
  public void getTables() throws RemoteException {
    ParcelableTableResourceList tables = peerAidlSynchronizer.getTables(null);

    assertThat(
        tables.getTables(),
        containsInAnyOrder(
            allOf(hasTableId("table_1"), hasDefinitionUri("table_1"), hasSchemaETag("schemaETagtable_1")),
            allOf(hasTableId("table_2"), hasDefinitionUri("table_2"), hasSchemaETag("schemaETagtable_2")),
            allOf(hasTableId("table_3"), hasDefinitionUri("table_3"), hasSchemaETag("schemaETagtable_3")),
            allOf(hasTableId("table_4"), hasDefinitionUri("table_4"), hasSchemaETag("schemaETagtable_4")),
            allOf(hasTableId("table_5"), hasDefinitionUri("table_5"), hasSchemaETag("schemaETagtable_5"))
        )
    );
  }

  @Test
  public void getTable() throws RemoteException {
    assertThat(
        peerAidlSynchronizer.getTable("table_1"),
        allOf(
            hasTableId("table_1"),
            hasDefinitionUri("table_1"),
            hasSchemaETag("schemaETagtable_1")
        )
    );
  }

  @Test
  public void getTableDefinition() throws RemoteException {
    assertThat(
        peerAidlSynchronizer.getTableDefinition("table_1"),
        allOf(
            hasColumn(new Column("column1_1", "column1_1", "string", null)),
            hasColumn(new Column("column1_2", "column1_2", "string", null)),
            hasColumn(new Column("column1_3", "column1_3", "string", null))
        )
    );
  }

  @Test
  public void createTable() throws RemoteException {
    ParcelableColumn c = new ParcelableColumn(new Column("col_1", "col_1", "string", null));

    peerAidlSynchronizer.createTable("table_new", "", Collections.singletonList(c));

    assertThat(
        peerAidlSynchronizer.getTable("table_new"),
        allOf(
            hasTableId("table_new"),
            hasDefinitionUri("table_new"),
            hasSchemaETag("schemaETagtable_new")
        )
    );

    assertThat(
        peerAidlSynchronizer.getTableDefinition("table_new").getColumns(),
        allOf(
            Matchers.<Column>hasSize(1),
            containsInAnyOrder(((Column) c))
        )
    );
  }

  @Test
  public void getUpdates() throws RemoteException, JsonProcessingException {
    ParcelableTableResource table = peerAidlSynchronizer.getTable("table_4");
    ParcelableRowResourceList updates = peerAidlSynchronizer.getUpdates(table, null, null, 1);

//    Log.e(TAG, "getUpdates: " + new ObjectMapper().writeValueAsString(updates));

    for (RowResource rowResource : updates.getRows()) {
      Log.e(TAG, "getUpdates: " + rowResource);
    }

//    ParcelableTableResource table = peerAidlSynchronizer.getTable("table_1");
//    ParcelableRowResourceList updates = peerAidlSynchronizer.getUpdates(table, null, null, 1);
//
//    assertThat(
//        updates.getRows(),
//        Matchers.<RowResource>hasSize(1)
//    );
//
//    assertThat(
//        updates.getRows().get(0).getValues(),
//        allOf(
//            hasItem(new DataKeyValue("column1_1", "value1_1")),
//            hasItem(new DataKeyValue("column1_2", "value1_2")),
//            hasItem(new DataKeyValue("column1_3", "value1_3")),
//            hasItem(hasDkvWithColumn(SubmitColumns.DEVICE_ID)),
//            hasItem(hasDkvWithColumn(SubmitColumns.TRANSFER_ID))
//        )
//    );
  }
}
