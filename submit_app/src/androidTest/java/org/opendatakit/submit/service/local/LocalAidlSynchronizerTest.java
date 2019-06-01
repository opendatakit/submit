package org.opendatakit.submit.service.local;

import android.Manifest;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.UserInfo;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.service.AbstractAidlSynchronizerTest;
import org.opendatakit.sync.service.entity.ParcelableColumn;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;
import org.opendatakit.sync.service.entity.ParcelableTableResource;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.sync.service.logic.IAidlSynchronizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.opendatakit.submit.matcher.TableResourceMatchers.*;
import static org.opendatakit.submit.matcher.TableDefinitionResourceMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalAidlSynchronizerTest extends AbstractAidlSynchronizerTest {
  private static final String TAG = LocalAidlSynchronizerTest.class.getSimpleName();

  private static final String TEST_APP_NAME =
      "androidTest_" + LocalAidlSynchronizerTest.class.getSimpleName();
  private static final String TEST_SUBMIT_APP_NAME =
      "submit_androidTest_" + LocalAidlSynchronizerTest.class.getSimpleName();;

  @Rule
  public final ServiceTestRule serviceRule = new ServiceTestRule();

  @Rule
  public final GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  );

  private IAidlSynchronizer localAidlSynchronizer;

  @Override
  protected Iterable<String> getTestAppNames() {
    return Arrays.asList(TEST_APP_NAME, TEST_SUBMIT_APP_NAME);
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    IBinder binder = serviceRule.bindService(new Intent()
        .setClassName(
            IntentConsts.SubmitLocalSync.PACKAGE_NAME,
            IntentConsts.SubmitLocalSync.SERVICE_CLASS_NAME
        )
        .putExtra(IntentConsts.INTENT_KEY_APP_NAME, TEST_SUBMIT_APP_NAME)
    );

    localAidlSynchronizer = LocalAidlSynchronizer.Stub.asInterface(binder);
    assertNotNull(localAidlSynchronizer);

    createTables(TEST_APP_NAME);
    insertTestData(TEST_APP_NAME);
  }

  @Test
  public void verifyServerSupportsAppName() throws RemoteException {
    localAidlSynchronizer.verifyServerSupportsAppName();
  }

  @Test
  public void getUserRolesAndDefaultGroup() throws RemoteException {
    assertNull(localAidlSynchronizer.getUserRolesAndDefaultGroup());
  }

  @Test
  public void getUsers() throws RemoteException {
    assertThat(localAidlSynchronizer.getUsers(), emptyCollectionOf(UserInfo.class));
  }

  @Test
  public void getTables() throws RemoteException {
    ParcelableTableResourceList tables = localAidlSynchronizer.getTables(null);

    assertThat(
        tables.getTables(),
        allOf(
            Matchers.<TableResource>hasSize(3),
            containsInAnyOrder(
                allOf(hasTableId("table_1"), hasDefinitionUri("table_1"), hasSchemaETag("schemaETagtable_1")),
                allOf(hasTableId("table_2"), hasDefinitionUri("table_2"), hasSchemaETag("schemaETagtable_2")),
                allOf(hasTableId("table_3"), hasDefinitionUri("table_3"), hasSchemaETag("schemaETagtable_3"))
            )
        )
    );
  }

  @Test
  public void getTable() throws RemoteException {
    assertThat(
        localAidlSynchronizer.getTable("table_1"),
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
        localAidlSynchronizer.getTableDefinition("table_1"),
        allOf(
            hasNColumns(3 + SubmitColumns.SUBMIT_COLUMNS.size()),
            hasColumn(new Column("column1_1", "column1_1", "string", null)),
            hasColumn(new Column("column1_2", "column1_2", "string", null)),
            hasColumn(new Column("column1_3", "column1_3", "string", null)),
            hasSubmitColumns()
        )
    );
  }

  @Test
  public void createTable() throws RemoteException, ServicesAvailabilityException {
    ParcelableColumn c = new ParcelableColumn(new Column("col_1", "col_1", "string", null));

    localAidlSynchronizer.createTable("table_new", "", Collections.singletonList(c));

    assertThat(
        localAidlSynchronizer.getTable("table_new"),
        allOf(
            hasTableId("table_new"),
            hasDefinitionUri("table_new"),
            hasSchemaETag("schemaETagtable_new")
        )
    );

    assertThat(
        getDb()
            .getUserDefinedColumns(TEST_APP_NAME, getDbHandle(TEST_APP_NAME), "table_new")
            .getColumns(),
        allOf(
            Matchers.<Column>hasSize(1),
            containsInAnyOrder((Column) c)
        )
    );
  }

  @Test
  public void createTable_filter() throws RemoteException, ServicesAvailabilityException {
    ParcelableColumn c = new ParcelableColumn(new Column("col_1", "col_1", "string", null));
    ParcelableColumn cSubmit = new ParcelableColumn(new Column(SubmitColumns.P_STATE, "col_submit", "string", null));

    localAidlSynchronizer.createTable("table_new", "", Arrays.asList(c, cSubmit));

    assertThat(
        localAidlSynchronizer.getTable("table_new"),
        allOf(
            hasTableId("table_new"),
            hasDefinitionUri("table_new"),
            hasSchemaETag("schemaETagtable_new")
        )
    );

    assertThat(
        getDb()
            .getUserDefinedColumns(TEST_APP_NAME, getDbHandle(TEST_APP_NAME), "table_new")
            .getColumns(),
        allOf(
            Matchers.<Column>hasSize(1),
            containsInAnyOrder((Column) c)
        )
    );
  }

  @Test
  public void getUpdates() throws RemoteException {
    ParcelableTableResource table = localAidlSynchronizer.getTable("table_1");
    ParcelableRowResourceList updates = localAidlSynchronizer.getUpdates(table, null, null, 1);

    assertThat(
        updates.getRows(),
        Matchers.<RowResource>hasSize(1)
    );

    assertThat(
        updates.getRows().get(0).getValues(),
        allOf(
            Matchers.<DataKeyValue>hasSize(3 + SubmitColumns.SUBMIT_COLUMNS.size()),
            containsInAnyOrder(
                new DataKeyValue("column1_1", "value1_1"),
                new DataKeyValue("column1_2", "value1_2"),
                new DataKeyValue("column1_3", "value1_3"),
                new DataKeyValue(SubmitColumns.DEVICE_ID, ""),
                new DataKeyValue(SubmitColumns.P_ID, "row_1_1"),
                new DataKeyValue(SubmitColumns.P_STATE, SubmitSyncStates.P_MODIFIED),
                new DataKeyValue(SubmitColumns.TRANSFER_ID, "")
            )
        )
    );
  }
}