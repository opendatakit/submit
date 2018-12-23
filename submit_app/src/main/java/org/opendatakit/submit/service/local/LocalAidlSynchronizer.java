package org.opendatakit.submit.service.local;

import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitConsts;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.util.RowUtil;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.entity.ParcelableChangeSetList;
import org.opendatakit.sync.service.entity.ParcelableColumn;
import org.opendatakit.sync.service.entity.ParcelableDataKeyValue;
import org.opendatakit.sync.service.entity.ParcelablePrivilegesInfo;
import org.opendatakit.sync.service.entity.ParcelableRow;
import org.opendatakit.sync.service.entity.ParcelableRowOutcome;
import org.opendatakit.sync.service.entity.ParcelableRowOutcomeList;
import org.opendatakit.sync.service.entity.ParcelableRowResource;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;
import org.opendatakit.sync.service.entity.ParcelableTableDefinitionResource;
import org.opendatakit.sync.service.entity.ParcelableTableResource;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.sync.service.entity.ParcelableUserInfoList;
import org.opendatakit.sync.service.logic.CommonFileAttachmentTerms;
import org.opendatakit.sync.service.logic.FileManifestDocument;
import org.opendatakit.sync.service.logic.IAidlSynchronizer;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class LocalAidlSynchronizer extends IAidlSynchronizer.Stub {
  private static final String TAG = LocalAidlSynchronizer.class.getSimpleName();

  private static final String SCHEMA_ETAG_PREFIX = "schemaETag";

  private final String submitAppName;
  private final String primaryAppName;
  private final CommonApplication application;
  private final Set<String> adminColumnSet;
  private final Set<String> submitAdminColumnSet;
  private final Set<String> otherAdminColumnSet;

  private String getPrimaryAppName() {
    return primaryAppName;
  }

  private String getSubmitAppName() {
    return submitAppName;
  }

  private CommonApplication getApplication() {
    return application;
  }

  private UserDbInterface getDatabase() {
    // TODO: Use Handler
    while (getApplication().getDatabase() == null) {
      try {
        Log.e(TAG, "getDatabase: SLEEP");
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return getApplication().getDatabase();
  }

  /**
   *
   * @param submitAppName     AppName of the application that stated the sync process (should be submit_*)
   * @param primaryAppName AppName of the user interface
   * @param application Reference to Submit
   */
  LocalAidlSynchronizer(@NonNull String submitAppName,
                               @NonNull String primaryAppName,
                               @NonNull CommonApplication application) {
    this.submitAppName = submitAppName;
    this.primaryAppName = primaryAppName;
    this.application = application;

    this.adminColumnSet = Collections.unmodifiableSet(new HashSet<>(DataTableColumns.getAdminColumns()));
    this.submitAdminColumnSet = SubmitColumns.SUBMIT_COLUMNS;
    this.otherAdminColumnSet = Collections.singleton(DataTableColumns.EFFECTIVE_ACCESS);
  }

  @Override
  public void verifyServerSupportsAppName() throws RemoteException {
    // TODO: use a better exception
    if (!getSubmitAppName().startsWith(SubmitConsts.SECONDARY_APP_NAME_PREFIX)) {
      throw new IllegalArgumentException("AppName must start with " + SubmitConsts.SECONDARY_APP_NAME_PREFIX);
    }

    String primaryAppName = getSubmitAppName().substring(SubmitConsts.SECONDARY_APP_NAME_PREFIX.length());

    File appDir = new File(ODKFileUtils.getAppFolder(primaryAppName));
    File submitAppDir = new File(ODKFileUtils.getAppFolder(getSubmitAppName()));

    // make sure both app directories exist
    if (!submitAppDir.exists() || !submitAppDir.isDirectory() ||
        !appDir.exists() || !appDir.isDirectory()) {
      throw new IllegalArgumentException("Unsupported appName");
    }
  }

  @Override
  public ParcelablePrivilegesInfo getUserRolesAndDefaultGroup() throws RemoteException {
    return null;
  }

  @Override
  public ParcelableUserInfoList getUsers() throws RemoteException {
    return new ParcelableUserInfoList();
  }

  @Override
  public Uri constructAppLevelFileManifestUri() throws RemoteException {
    return new Uri.Builder()
        .appendPath("submit")
        .appendPath(getPrimaryAppName())
        .build();
  }

  @Override
  public Uri constructTableLevelFileManifestUri(String tableId) throws RemoteException {
    return new Uri.Builder()
        .appendPath("submit")
        .appendPath(tableId)
        .build();
  }

  @Override
  public Uri constructRealizedTableIdUri(String tableId, String schemaETag) throws RemoteException {
    return new Uri.Builder()
        .appendPath("submit")
        .appendPath(tableId)
        .appendPath(schemaETag)
        .build();
  }

  @Override
  public Uri constructInstanceFileManifestUri(String serverInstanceFileUri, String rowId) throws RemoteException {
    return new Uri.Builder()
        .appendPath("submit")
        .appendPath(serverInstanceFileUri)
        .appendPath(rowId)
        .build();
  }

  @Override
  public ParcelableTableResourceList getTables(String webSafeResumeCursor) throws RemoteException {
    // TODO: paging with webSafeResumeCursor
    final String dbAppName = getPrimaryAppName();

    DbHandle handle = null;
    try {
      handle = getDatabase().openDatabase(dbAppName);

      ParcelableTableResourceList list = new ParcelableTableResourceList();

      for (String id : getDatabase().getAllTableIds(dbAppName, handle)) {
        TableDefinitionEntry tableDef =
            getDatabase().getTableDefinitionEntry(dbAppName, handle, id);

        list.getTables().add(getTableResFromTableDefEntry(tableDef));
      }

      list.setAppLevelManifestETag(getDatabase().getManifestSyncETag(dbAppName, handle, null, null));
      list.setHasMoreResults(false);

      return list;
    } catch (ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public ParcelableTableResource getTable(String tableId) throws RemoteException {
    final String dbAppName = getPrimaryAppName();
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      TableDefinitionEntry tableDef =
          getDatabase().getTableDefinitionEntry(dbAppName, handle, tableId);

      return getTableResFromTableDefEntry(tableDef);
    } catch (ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public ParcelableTableDefinitionResource getTableDefinition(String tableDefinitionUri) throws RemoteException {
    final String dbAppName = getPrimaryAppName();
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      TableDefinitionEntry tableDef =
          getDatabase().getTableDefinitionEntry(dbAppName, handle, tableDefinitionUri);

      ArrayList<Column> columns = getDatabase()
          .getUserDefinedColumns(dbAppName, handle, tableDefinitionUri)
          .getColumns();
      // pretend that the primary database has these columns
      for (String submitColumn : SubmitColumns.SUBMIT_COLUMNS) {
        columns.add(new Column(
            submitColumn,
            submitColumn,
            ElementDataType.string.toString(),
            null
        ));
      }

      if (tableDef.getSchemaETag() == null) {
        // schemaETag must be non-null
        tableDef.setSchemaETag(SCHEMA_ETAG_PREFIX + tableDef.getTableId());
      }

      return new ParcelableTableDefinitionResource(
          tableDef.getTableId(), tableDef.getSchemaETag(), columns);
    } catch (ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public ParcelableTableResource createTable(String tableId, String schemaETag, List<ParcelableColumn> columns) throws RemoteException {
    final String dbAppName = getPrimaryAppName();
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      List<Column> columnList = new ArrayList<>();
      // filter out Submit only columns
      for (ParcelableColumn column : columns) {
        if (!isMetadataColumn(column.getElementKey())) {
          columnList.add(column);
        }
      }
      getDatabase().createOrOpenTableWithColumns(dbAppName, handle, tableId, new ColumnList(columnList));

      return getTableResFromTableDefEntry(getDatabase().getTableDefinitionEntry(dbAppName, handle, tableId));
    } catch (ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void deleteTable(ParcelableTableResource table) throws RemoteException {
    // this is not used
    throw new UnsupportedOperationException();
  }

  @Override
  public ParcelableChangeSetList getChangeSets(ParcelableTableResource tableResource, String dataETag) throws RemoteException {
    // this is not used
    throw new UnsupportedOperationException();
  }

  @Override
  public ParcelableRowResourceList getChangeSet(ParcelableTableResource tableResource, String dataETag, boolean activeOnly, String websafeResumeCursor) throws RemoteException {
    // this is not used
    throw new UnsupportedOperationException();
  }

  @Override
  public ParcelableRowResourceList getUpdates(ParcelableTableResource tableResource, String dataETag, String websafeResumeCursor, int fetchLimit) throws RemoteException {
    final String dbAppName = getPrimaryAppName();
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      // TODO: adjust fetchLimit
      int adjustedFetchLimit = fetchLimit;
      Integer offset = websafeResumeCursor != null ? Integer.valueOf(websafeResumeCursor) : null;
      // TODO: cache this
      OrderedColumns orderedColumns = getDatabase()
          .getUserDefinedColumns(dbAppName, handle, tableResource.getTableId());

      UserTable table = getDatabase().privilegedSimpleQuery(
          dbAppName,
          handle,
          tableResource.getTableId(),
          orderedColumns,
          null,
          null,
          null,
          null,
          null,
          null,
          adjustedFetchLimit,
          offset
      );

      ArrayList<RowResource> rowResourceList = new ArrayList<>();
      for (int i = 0; i < table.getNumberOfRows(); i++) {
        TypedRow typedRow = table.getRowAtIndex(i);
        ParcelableRow parcelableRow = new ParcelableRow();

        ArrayList<DataKeyValue> dkvl = new ArrayList<>();
        for (String key : table.getElementKeyToIndex().keySet()) {
          if (!isMetadataColumn(key)) {
            dkvl.add(new ParcelableDataKeyValue(
                key,
                typedRow.getStringValueByKey(key)
            ));
          }
        }

        String pState = syncStateToPSyncState(typedRow.getRawStringByKey(DataTableColumns.SYNC_STATE));
        dkvl.add(new ParcelableDataKeyValue(
            SubmitColumns.P_STATE,
            pState
        ));
        dkvl.add(new ParcelableDataKeyValue(SubmitColumns.P_ID, typedRow.getRawStringByKey(DataTableColumns.ID)));

        // TODO: source these from the submit database
        dkvl.add(new ParcelableDataKeyValue(SubmitColumns.DEVICE_ID, ""));
        dkvl.add(new ParcelableDataKeyValue(SubmitColumns.TRANSFER_ID, ""));

        parcelableRow.setValues(dkvl);

        parcelableRow.setRowId(UUID.randomUUID().toString());
//        parcelableRow.setRowId(typedRow.getRawStringByKey(DataTableColumns.ID));
        parcelableRow.setFormId(typedRow.getRawStringByKey(DataTableColumns.FORM_ID));
        parcelableRow.setRowFilterScope(RowUtil.getRowFilterScope(typedRow));
        parcelableRow.setSavepointCreator(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_CREATOR));
        parcelableRow.setSavepointTimestamp(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_TIMESTAMP));
        parcelableRow.setSavepointType(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_TYPE));
        parcelableRow.setLocale(typedRow.getRawStringByKey(DataTableColumns.LOCALE));

        String rowETag = typedRow.getRawStringByKey(DataTableColumns.ROW_ETAG);
        if (rowETag == null || !pState.equals(SubmitSyncStates.P_SYNCED)) {
          parcelableRow.setRowETag(UUID.randomUUID().toString());
        } else {
          parcelableRow.setRowETag(rowETag);
        }

        ParcelableRowResource parcelableRowResource = new ParcelableRowResource(parcelableRow);
        parcelableRowResource.setSelfUri(parcelableRow.getRowId());

        rowResourceList.add(parcelableRowResource);
      }

      return new ParcelableRowResourceList(
          rowResourceList,
          null,
          tableResource.getSelfUri(),
          websafeResumeCursor,
          null,
          (offset == null ? 0 : offset) + adjustedFetchLimit + "",
          table.resumeQueryForward(adjustedFetchLimit) == null,
          false
      );
    } catch (ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public ParcelableRowOutcomeList pushLocalRows(ParcelableTableResource tableResource, OrderedColumns orderedColumns, List<String> rowsToInsertUpdateOrDelete) throws RemoteException {
    DbHandle primaryHandle = null;
    DbHandle submitHandle = null;

    try {
      primaryHandle = getDatabase().openDatabase(getPrimaryAppName());
      submitHandle = getDatabase().openDatabase(getSubmitAppName());

      OrderedColumns primaryColDef = getDatabase().getUserDefinedColumns(
          primaryAppName,
          primaryHandle,
          tableResource.getTableId()
      );

      ParcelableRowOutcomeList outcomeList = new ParcelableRowOutcomeList();

      for (String rowId : rowsToInsertUpdateOrDelete) {
        TypedRow typedRow = getDatabase()
            .privilegedGetRowsWithId(
                submitAppName,
                submitHandle,
                tableResource.getTableId(),
                orderedColumns,
                rowId
            )
            .getRowAtIndex(0);

        getDatabase().updateRowWithId(
            primaryAppName,
            primaryHandle,
            tableResource.getTableId(),
            primaryColDef,
            submitTypedRowToCv(typedRow),
            typedRow.getRawStringByKey(SubmitColumns.P_ID)
        );

        ParcelableRow parcelableRow = new ParcelableRow();
        parcelableRow.setRowId(rowId);
        parcelableRow.setRowETag(typedRow.getRawStringByKey(DataTableColumns.ROW_ETAG));
        parcelableRow.setRowFilterScope(RowUtil.getRowFilterScope(typedRow));

        outcomeList
            .getRows()
            .add(new ParcelableRowOutcome(parcelableRow, RowOutcome.OutcomeType.SUCCESS));
      }

      return outcomeList;
    } catch (ServicesAvailabilityException | ActionNotAuthorizedException e) {
      Log.e(TAG, "pushLocalRows: ", e);
      return new ParcelableRowOutcomeList();
    } finally {
      if (primaryHandle != null) {
        try {
          getDatabase().closeDatabase(primaryAppName, primaryHandle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "pushLocalRows: ", e);
        }
      }

      if (submitHandle != null) {
        try {
          getDatabase().closeDatabase(submitAppName, submitHandle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "pushLocalRows: ", e);
        }
      }
    }
  }

  @Override
  public FileManifestDocument getAppLevelFileManifest(String lastKnownLocalAppLevelManifestETag, String serverReportedAppLevelETag, boolean pushLocalFiles) throws RemoteException {
    // null to skip app level file sync
    return null;
  }

  @Override
  public FileManifestDocument getTableLevelFileManifest(String tableId, String lastKnownLocalTableLevelManifestETag, String serverReportedTableLevelETag, boolean pushLocalFiles) throws RemoteException {
    return null;
  }

  @Override
  public FileManifestDocument getRowLevelFileManifest(String serverInstanceFileUri, String tableId, String instanceId, SyncAttachmentState attachmentState, String lastKnownLocalRowLevelManifestETag) throws RemoteException {
    return null;
  }

  @Override
  public void downloadFile(Uri destFile, Uri downloadUrl) throws RemoteException {

  }

  @Override
  public void deleteConfigFile(Uri localFile) throws RemoteException {

  }

  @Override
  public void uploadConfigFile(Uri localFile) throws RemoteException {

  }

  @Override
  public void uploadInstanceFile(Uri file, Uri instanceFileUri) throws RemoteException {

  }

  @Override
  public CommonFileAttachmentTerms createCommonFileAttachmentTerms(String serverInstanceFileUri, String tableId, String instanceId, String rowpathUri) throws RemoteException {
    return null;
  }

  @Override
  public void uploadInstanceFileBatch(List<CommonFileAttachmentTerms> batch, String serverInstanceFileUri, String instanceId, String tableId) throws RemoteException {

  }

  @Override
  public void downloadInstanceFileBatch(List<CommonFileAttachmentTerms> filesToDownload, String serverInstanceFileUri, String instanceId, String tableId) throws RemoteException {

  }

  private boolean isMetadataColumn(String columnName) {
    return adminColumnSet.contains(columnName) ||
        submitAdminColumnSet.contains(columnName) ||
        otherAdminColumnSet.contains(columnName);
  }

  private ParcelableTableResource getTableResFromTableDefEntry(TableDefinitionEntry entry) {
    if (entry.getSchemaETag() == null) {
      // schemaETag must be non-null
      entry.setSchemaETag(SCHEMA_ETAG_PREFIX + entry.getTableId());
    }

    ParcelableTableResource tableResource = new ParcelableTableResource(new TableEntry(
        entry.getTableId(),
        entry.getLastDataETag(),
        entry.getSchemaETag()
    ));
    tableResource.setDefinitionUri(entry.getTableId());

    return tableResource;
  }

  private ContentValues submitTypedRowToCv(TypedRow row) {
    ContentValues cv = new ContentValues();

    // TODO: put in CV the correct type
    for (String key : row.getElementKeyForIndexMap()) {
      cv.put(key, row.getStringValueByKey(key));
    }

    cv.put(DataTableColumns.ID, cv.getAsString(SubmitColumns.P_ID));

    // these are restricted columns
    cv.remove(DataTableColumns.DEFAULT_ACCESS);
    cv.remove(DataTableColumns.ROW_OWNER);
    cv.remove(DataTableColumns.GROUP_PRIVILEGED);
    cv.remove(DataTableColumns.GROUP_MODIFY);
    cv.remove(DataTableColumns.GROUP_READ_ONLY);

    // TODO: handle this stray column better
    cv.remove(DataTableColumns.EFFECTIVE_ACCESS);

    // TODO: find a better solution for security issues

    // submit columns should not be brought back
    for (String submitColumn : SubmitColumns.SUBMIT_COLUMNS) {
      cv.remove(submitColumn);
    }

    return cv;
  }

  private String syncStateToPSyncState(@Nullable String syncState) {
    if (syncState == null || syncState.isEmpty()) {
      return SubmitSyncStates.P_SYNCED;
    }

    switch (SyncState.valueOf(syncState)) {
      case synced:
      case synced_pending_files:
        return SubmitSyncStates.P_SYNCED;
      case changed:
      case deleted:
      case new_row:
        return SubmitSyncStates.P_MODIFIED;
      case in_conflict:
        // should never happen
        return SubmitSyncStates.P_CONFLICT;
      default:
        return SubmitSyncStates.P_SYNCED;
    }
  }
}
