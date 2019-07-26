package org.opendatakit.submit.service.peer;

import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.ColumnDefinition;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.PeerServerConsts;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.util.RowUtil;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.entity.ParcelableChangeSetList;
import org.opendatakit.sync.service.entity.ParcelableColumn;
import org.opendatakit.sync.service.entity.ParcelablePrivilegesInfo;
import org.opendatakit.sync.service.entity.ParcelableRow;
import org.opendatakit.sync.service.entity.ParcelableRowOutcome;
import org.opendatakit.sync.service.entity.ParcelableRowOutcomeList;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;
import org.opendatakit.sync.service.entity.ParcelableTableDefinitionResource;
import org.opendatakit.sync.service.entity.ParcelableTableResource;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.sync.service.entity.ParcelableUserInfoList;
import org.opendatakit.sync.service.logic.CommonFileAttachmentTerms;
import org.opendatakit.sync.service.logic.FileManifestDocument;
import org.opendatakit.sync.service.logic.IAidlSynchronizer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PeerAidlSynchronizer extends IAidlSynchronizer.Stub {
  private static final String TAG = PeerAidlSynchronizer.class.getSimpleName();

  private static final String SUFFIX = "_o";
  public static final String PEER_LOCAL_ONLY_TABLE_PREFIX = "L__PEER_";

  private final String appName;
  private final String remoteAppName;
  private final CommonApplication application;

  private final OkHttpClient httpClient;
  private final HttpUrl address;
  private final ObjectMapper objectMapper;

  private final Set<String> adminColumnSet;
  private final Set<String> otherAdminColumnSet;

  private final Map<String, Map<String, String>> pStateMapCache;
  private final Map<String, Set<String>> rowsToSend;

  private String getAppName() {
    return appName;
  }

  private String getRemoteAppName() {
    return remoteAppName;
  }

  public CommonApplication getApplication() {
    return application;
  }

  private OkHttpClient getHttpClient() {
    return httpClient;
  }

  private HttpUrl getAddress() {
    return address;
  }

  private Map<String, Map<String, String>> getPStateMapCache() {
    return pStateMapCache;
  }

  public Map<String, Set<String>> getRowsToSend() {
    return rowsToSend;
  }

  private ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public UserDbInterface getDatabase() {
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

  public PeerAidlSynchronizer(@NonNull String appName, @NonNull CommonApplication application) {
    this.appName = appName;
    this.application = application;

    String serverUrl = CommonToolProperties
        .get(application, appName)
        .getProperty(CommonToolProperties.KEY_SYNC_SERVER_URL);

    Log.d(TAG, "PeerAidlSynchronizer: raw url = " + serverUrl);

    Uri uri = Uri.parse(serverUrl).buildUpon().scheme("http").build();
    this.remoteAppName = uri.getPathSegments().get(0);

    this.httpClient = new OkHttpClient.Builder().build();
    this.address = HttpUrl
        .parse(uri.toString())
        .newBuilder()
        .removePathSegment(0) // remove the appName part
        .addQueryParameter(PeerServerConsts.APP_NAME_QUERY, getRemoteAppName())
        .build();

    this.objectMapper = new ObjectMapper();

    this.adminColumnSet = Collections.unmodifiableSet(new HashSet<>(DataTableColumns.getAdminColumns()));
    this.otherAdminColumnSet = Collections.singleton(DataTableColumns.EFFECTIVE_ACCESS);

    this.pStateMapCache = new ConcurrentHashMap<>();
    this.rowsToSend = new ConcurrentHashMap<>();

    Log.i(TAG, "PeerAidlSynchronizer: appName = " + appName + ", remoteAppName = " + remoteAppName);
    Log.i(TAG, "PeerAidlSynchronizer: address is " + address.toString());
  }

  @Override
  public void verifyServerSupportsAppName() throws RemoteException {
    Log.d(TAG, "verifyServerSupportsAppName: checking appName=" + getAppName());

    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      if (!response.isSuccessful()) {
        Log.e(TAG, "verifyServerSupportsAppName: UNSUPPORTED APPNAME");
        throw new IllegalArgumentException("Unsupported App Name");
      }
    } catch (IOException e) {
      Log.e(TAG, "verifyServerSupportsAppName: ", e);
      throw new RuntimeException(e);
    } finally {
      if (response != null) {
        response.close();
      }
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
        .appendPath(getAppName())
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
    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.GET_TABLES_PATH)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      return getObjectMapper()
          .readValue(response.body().byteStream(), ParcelableTableResourceList.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public ParcelableTableResource getTable(String tableId) throws RemoteException {
    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.GET_TABLE_PATH)
        .addQueryParameter(PeerServerConsts.TABLE_ID_QUERY, tableId)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      TableResource tableResource = getObjectMapper()
          .readValue(response.body().byteStream(), TableResource.class);

      return new ParcelableTableResource(tableResource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public ParcelableTableDefinitionResource getTableDefinition(String tableDefinitionUri) throws RemoteException {
    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.GET_TABLE_DEF_PATH)
        .addQueryParameter(PeerServerConsts.TABLE_DEF_URI_QUERY, tableDefinitionUri)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      TableDefinitionResource defRes = getObjectMapper()
          .readValue(response.body().byteStream(), TableDefinitionResource.class);

      return new ParcelableTableDefinitionResource(defRes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public ParcelableTableResource createTable(String tableId, String schemaETag, final List<ParcelableColumn> columns) throws RemoteException {
    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.CREATE_TABLE_PATH)
        .addQueryParameter(PeerServerConsts.TABLE_ID_QUERY, tableId)
        .addQueryParameter(PeerServerConsts.SCHEMA_ETAG_QUERY, schemaETag)
        .build();

    RequestBody requestBody = null;
    try {
      requestBody = new FormBody.Builder()
          .add(PeerServerConsts.COLUMNS_FORM_PARAM, getObjectMapper().writeValueAsString(columns))
          .build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      TableResource tableResource = getObjectMapper()
          .readValue(response.body().byteStream(), TableResource.class);

      return new ParcelableTableResource(tableResource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null) {
        response.close();
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
    // FIXME: REMOVE!! THIS IS FOR ME
    // I will need to fetch all records from the server (put in local table),
    // consolidate + filter
    // then send them to Services in batches

    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.GET_UPDATES_PATH)
        .addQueryParameter(PeerServerConsts.TABLE_ID_QUERY, tableResource.getTableId())
        .addQueryParameter(PeerServerConsts.DATA_ETAG_QUERY, dataETag)
        .addQueryParameter(PeerServerConsts.FETCH_LIMIT_QUERY, Integer.toString(10000))
        .build();

    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = null;
    DbHandle handle = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      ParcelableRowResourceList list =
          getObjectMapper().readValue(response.body().byteStream(), ParcelableRowResourceList.class);

      handle = getDatabase().openDatabase(getAppName());

      String localTableId = PEER_LOCAL_ONLY_TABLE_PREFIX + tableResource.getTableId();
      OrderedColumns orderedColumnsA =
              getDatabase().getUserDefinedColumns(getAppName(), handle, tableResource.getTableId());

      UserTable commonRows = findCommonRows(
              handle,
              tableResource.getTableId(),
              list.getRows(),
              localTableId,
              orderedColumnsA
      );

      ArrayList<RowResource> listCopy = new ArrayList<>();

      // TODO: optimize
      Map<String, RowResource> idToRowMap = new HashMap<>();
      for (RowResource row : list.getRows()) {
//        idToRowMap.put(Row.convertToMap(row.getValues()).get(SubmitColumns.P_ID), row);
        idToRowMap.put(row.getRowId(), row);
      }

      Map<String, String> pIdStateMap = getPStateMapCache().get(tableResource.getTableId());
      if (pIdStateMap == null) {
        pIdStateMap = new HashMap<>();
        getPStateMapCache().put(tableResource.getTableId(), pIdStateMap);
      }

      Set<String> rowsToSendSet = getRowsToSend().get(tableResource.getTableId());
      if (rowsToSendSet == null) {
        rowsToSendSet = new HashSet<>();
        getRowsToSend().put(tableResource.getTableId(), rowsToSendSet);
      }

      if (commonRows != null) {
        for (int i = 0; i < commonRows.getNumberOfRows(); i++) {
          TypedRow row = commonRows.getRowAtIndex(i);
          RowResource rowResource = idToRowMap.get(row.getRawStringByKey("_id_o"));
          listCopy.add(rowResource);

          String newState = findNewSyncState(
              row.getRawStringByKey(SubmitColumns.P_STATE),
              row.getRawStringByKey("p_state_o"),
              row.getRawStringByKey(DataTableColumns.ROW_ETAG),
              row.getRawStringByKey("_row_etag_o")
          );

          pIdStateMap.put(row.getRawStringByKey(SubmitColumns.P_ID), newState);

          // TODO: optimize
          HashMap<String, String> newVals = Row.convertToMap(rowResource.getValues());
          newVals.put(SubmitColumns.P_STATE, newState);

          rowResource.setValues(Row.convertFromMap(newVals));

          // this row needs to be treated as a new row by the receiver
          rowResource.setRowId(UUID.randomUUID().toString());

          rowsToSendSet.add(row.getRawStringByKey(DataTableColumns.ID));
        }

        String rowsToSend =
            "SELECT " + "_id" + " " +
                "FROM " + tableResource.getTableId() + " " +
                "WHERE " + SubmitColumns.P_ID + " NOT IN " +
                "(SELECT " + SubmitColumns.P_ID + " FROM " + localTableId + ")";

        BaseTable rowsToSendId = getDatabase().arbitrarySqlQuery(
            getAppName(),
            handle,
            tableResource.getTableId(),
            rowsToSend,
            null,
            null,
            null
        );

        for (org.opendatakit.database.data.Row row : rowsToSendId.getRows()) {
          rowsToSendSet.add(row.getRawStringByKey(DataTableColumns.ID));
        }
      }

      Set<String> newIdSet = new HashSet<>();

      if (commonRows != null) {
        String newRowQuery =
            "SELECT " + "r_id" + " " +
                "FROM " + localTableId + " " +
                "WHERE " + SubmitColumns.P_ID + " NOT IN " +
                "(SELECT " + SubmitColumns.P_ID + " FROM " + tableResource.getTableId() + ")";

        BaseTable newIds = getDatabase().arbitrarySqlQuery(
            getAppName(),
            handle,
            tableResource.getTableId(),
            newRowQuery,
            null,
            null,
            null
        );

        for (org.opendatakit.database.data.Row row : newIds.getRows()) {
          newIdSet.add(row.getRawStringByKey("r_id"));
        }
      }

      for (RowResource row : list.getRows()) {
        if (newIdSet.contains(row.getRowId())) {
          listCopy.add(row);
        }
      }

      list.setRows(listCopy);

      // this will probably generate too many transfer ids,
      // 1 transfer could use multiple getUpdates calls
      String transferId = UUID.randomUUID().toString();
      for (RowResource row : list.getRows()) {
        Map<String, String> dkvMap = Row.convertToMap(row.getValues());
        dkvMap.put(SubmitColumns.TRANSFER_ID, transferId);

        row.setValues(Row.convertFromMap(dkvMap));

        Log.e(TAG, "getUpdates: sending row _id=" + row.getRowId());
      }

      return list;
    } catch (IOException | ServicesAvailabilityException e) {
      throw new RuntimeException(e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(getAppName(), handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }

      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public ParcelableRowOutcomeList pushLocalRows(ParcelableTableResource tableResource, OrderedColumns orderedColumns, List<String> rowsToInsertUpdateOrDelete) throws RemoteException {
    // TODO: optimize this to send only the necessary rows

    DbHandle handle = null;
    List<Row> rowList;
    Map<String, String> rowETag = new HashMap<>();
    try {
      handle = getDatabase().openDatabase(getAppName());

      rowList = new ArrayList<>();

      for (String rowId : rowsToInsertUpdateOrDelete) {
        TypedRow typedRow = getDatabase()
            .privilegedGetRowsWithId(
                getAppName(),
                handle,
                tableResource.getTableId(),
                orderedColumns,
                rowId
            )
            .getRowAtIndex(0);

        rowETag.put(
            typedRow.getRawStringByKey(DataTableColumns.ID),
            typedRow.getRawStringByKey(DataTableColumns.ROW_ETAG)
        );

        Row row = new Row();

        ArrayList<DataKeyValue> dkvl = new ArrayList<>();

        if (getRowsToSend().get(tableResource.getTableId()).contains(rowId)) {
          for (ColumnDefinition definition : orderedColumns.getColumnDefinitions()) {
            String key = definition.getElementKey();

            if (!isAdminColumn(key)) {
              dkvl.add(new DataKeyValue(
                  key,
                  typedRow.getStringValueByKey(key)
              ));
            }
          }
        }

        row.setValues(dkvl);

        row.setRowId(typedRow.getRawStringByKey(DataTableColumns.ID));
        row.setFormId(typedRow.getRawStringByKey(DataTableColumns.FORM_ID));
        row.setRowFilterScope(RowUtil.getRowFilterScope(typedRow));
        row.setSavepointCreator(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_CREATOR));
        row.setSavepointTimestamp(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_TIMESTAMP));
        row.setSavepointType(typedRow.getRawStringByKey(DataTableColumns.SAVEPOINT_TYPE));
        row.setLocale(typedRow.getRawStringByKey(DataTableColumns.LOCALE));
        row.setRowETag(typedRow.getRawStringByKey(DataTableColumns.ROW_ETAG));

        // update p_state and assign a new _id
        if (!dkvl.isEmpty() && getPStateMapCache().get(tableResource.getTableId()).containsKey(typedRow.getRawStringByKey(SubmitColumns.P_ID))) {
          row.setRowId(UUID.randomUUID().toString());

          int pStateIndex = Collections.binarySearch(
              dkvl,
              new DataKeyValue(SubmitColumns.P_STATE, ""),
              new Comparator<DataKeyValue>() {
                @Override
                public int compare(DataKeyValue o1, DataKeyValue o2) {
                  return o1.column.compareTo(o2.column);
                }
              }
          );

          dkvl.get(pStateIndex).value =
              getPStateMapCache().get(tableResource.getTableId()).get(typedRow.getRawStringByKey(SubmitColumns.P_ID));
        }

        rowList.add(row);
      }
    } catch (ServicesAvailabilityException e) {
      Log.e(TAG, "pushLocalRows: ", e);
      return new ParcelableRowOutcomeList();
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(getAppName(), handle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "pushLocalRows: ", e);
        }
      }
    }

    HttpUrl url = getAddress()
        .newBuilder()
        .addPathSegment(PeerServerConsts.POST_ROWS_PATH)
        .addQueryParameter(PeerServerConsts.TABLE_ID_QUERY, tableResource.getTableId())
        .build();

    RequestBody requestBody;
    try {
      requestBody = new FormBody.Builder()
          .add(PeerServerConsts.ROWS_FORM_PARAM, getObjectMapper().writeValueAsString(rowList))
          .build();
    } catch (JsonProcessingException e) {
      Log.e(TAG, "pushLocalRows: ", e);
      return  new ParcelableRowOutcomeList();
    }

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
        .build();

    Response response = null;
    try {
      response = getHttpClient()
          .newCall(request)
          .execute();

      if (response.isSuccessful()) {
        ParcelableRowOutcomeList outcomeList = new ParcelableRowOutcomeList();

        for (String rowId : rowsToInsertUpdateOrDelete) {
          ParcelableRow parcelableRow = new ParcelableRow();
          parcelableRow.setRowId(rowId);
          parcelableRow.setRowETag(rowETag.get(rowId));
          parcelableRow.setRowFilterScope(RowFilterScope.EMPTY_ROW_FILTER);

          outcomeList
              .getRows()
              .add(new ParcelableRowOutcome(parcelableRow, RowOutcome.OutcomeType.SUCCESS));
        }

        return outcomeList;
      } else {
        return new ParcelableRowOutcomeList();
      }
    } catch (IOException e) {
      Log.e(TAG, "pushLocalRows: ", e);
      return new ParcelableRowOutcomeList();
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public FileManifestDocument getAppLevelFileManifest(String lastKnownLocalAppLevelManifestETag, String serverReportedAppLevelETag, boolean pushLocalFiles) throws RemoteException {
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

  private UserTable findCommonRows(DbHandle handle,
                                   String tableId,
                                   ArrayList<RowResource> remoteRows,
                                   String localTableId,
                                   OrderedColumns colDef) throws ServicesAvailabilityException {
    if (remoteRows.isEmpty()) {
      Log.e(TAG, "findCommonRows: REMOTE TABLE EMPTY");
      return null;
    }

    getDatabase().deleteLocalOnlyTable(getAppName(), handle, localTableId);
    getDatabase().createLocalOnlyTableWithColumns(
            getAppName(),
            handle,
            localTableId,
            new ColumnList(Arrays.asList(
                    new Column(SubmitColumns.P_ID, SubmitColumns.P_ID, ElementDataType.string.toString(), null),
                    new Column(SubmitColumns.P_STATE, SubmitColumns.P_STATE, ElementDataType.string.toString(), null),
                    new Column("r_id", "r_id", ElementDataType.string.toString(), null),
                    new Column("r_etag", "r_etag", ElementDataType.string.toString(), null)
            ))
    );

    String insertPeerRecord = "INSERT INTO " + localTableId + " (p_id, p_state, r_id, r_etag) VALUES ";
    StringBuilder insertPeerRecordSb = new StringBuilder(insertPeerRecord);

    for (int i = 0; i < remoteRows.size(); i++) {
      insertPeerRecordSb.append("(?, ?, ?, ?),");
    }

    // remove the extra ','
    insertPeerRecordSb.deleteCharAt(insertPeerRecordSb.length() - 1);

    Object[] bindArgs = new Object[remoteRows.size() * 4];
    for (int i = 0; i < remoteRows.size(); i++) {
      // TODO: avoid creating the map every iteration
      HashMap<String, String> valueMap = Row.convertToMap(remoteRows.get(i).getValues());
      bindArgs[i * 4] = valueMap.get(SubmitColumns.P_ID);
      bindArgs[i * 4 + 1] = valueMap.get(SubmitColumns.P_STATE);
      bindArgs[i * 4 + 2] = remoteRows.get(i).getRowId();
      bindArgs[i * 4 + 3] = remoteRows.get(i).getRowETag();
    }

    getDatabase().privilegedExecute(
            getAppName(),
            handle,
            insertPeerRecordSb.toString(),
            new BindArgs(bindArgs)
    );

    // assumes that sets of rows defined by a common rowETag
    // are never transferred partially
    String sqlFindCommon = "SELECT " +
        "l._id," +
        "o.r_id AS _id_o," +
        "l.p_id," +
        "o.p_id AS p_id_o," +
        "l._row_etag," +
        "o.r_etag AS _row_etag_o," +
        "l.p_state," +
        "o.p_state AS p_state_o " +
        "FROM " +
        "%s AS l INNER JOIN %s AS o ON l.p_id = o.p_id " +
        "WHERE l._row_etag != o.r_etag " +
        "AND o.r_etag NOT IN (SELECT _row_etag FROM %s)";

    UserTable userTable = getDatabase().arbitrarySqlQuery(
        getAppName(),
        handle,
        tableId,
        colDef,
        String.format(sqlFindCommon, tableId, localTableId, tableId),
        null,
        null,
        null
    );

    if (userTable == null) {
      Log.e(TAG, "findCommonRows: REMOTE TABLE EMPTY sql returned null");
    }

    return userTable;
  }

  /**
   * Builds a ContentValues object with all the same metadata values and user defined column
   * values from the given row. After calling this method, you simply need to update the sync
   * state for rowContent
   *
   * @param rowContent the rowContent to build
   * @param row the row object to build from
   * @param userCols the OrderedColumns of the user-defined columns
   * @param useSuffix true if we are building a common row from table B (and using our joined table)
   */
  private static void buildCV(ContentValues rowContent, TypedRow row, OrderedColumns userCols,
      boolean useSuffix) {
    String suffix = "";
    if (useSuffix) {
      suffix = SUFFIX;
    }
    // metadata original values
    rowContent.put(DataTableColumns.ID, (String)row.getDataByKey(DataTableColumns.ID + suffix));
    rowContent.put(DataTableColumns.ROW_ETAG, (String)row.getDataByKey(DataTableColumns.ROW_ETAG + suffix));
    rowContent.put(DataTableColumns.SYNC_STATE, (String)row.getDataByKey(DataTableColumns.SYNC_STATE + suffix));
    rowContent.put(DataTableColumns.FORM_ID, (String)row.getDataByKey(DataTableColumns.FORM_ID + suffix));
    rowContent.put(DataTableColumns.LOCALE, (String)row.getDataByKey(DataTableColumns.LOCALE + suffix));
    rowContent.put(DataTableColumns.SAVEPOINT_TIMESTAMP, (String)row.getDataByKey(DataTableColumns.SAVEPOINT_TIMESTAMP + suffix));
    rowContent.put(DataTableColumns.SAVEPOINT_CREATOR, (String)row.getDataByKey(DataTableColumns.SAVEPOINT_CREATOR + suffix));
    rowContent.put(DataTableColumns.SAVEPOINT_TYPE, (String)row.getDataByKey(DataTableColumns.SAVEPOINT_TYPE + suffix));
    rowContent.put(DataTableColumns.CONFLICT_TYPE, (String)row.getDataByKey(DataTableColumns.CONFLICT_TYPE + suffix));

    // todo: user permissions left out for now
    //            cvVals.put(DataTableColumns.DEFAULT_ACCESS, serverRow.getDataByKey(DataTableColumns.DEFAULT_ACCESS));
    //            cvVals.put(DataTableColumns.ROW_OWNER, serverRow.getDataByKey(DataTableColumns.ROW_OWNER));
    //            cvVals.put(DataTableColumns.GROUP_MODIFY, serverRow.getDataByKey(DataTableColumns.GROUP_MODIFY));
    //            cvVals.put(DataTableColumns.GROUP_PRIVILEGED, serverRow.getDataByKey(DataTableColumns.GROUP_PRIVILEGED));
    //            cvVals.put(DataTableColumns.GROUP_READ_ONLY, serverRow.getDataByKey(DataTableColumns.GROUP_READ_ONLY));

    // add all user defined columns to our ContentValues (including original p_state)
    // todo: for now this should propagate device id correctly while its a user column
    List<Column> userColsList = userCols.getColumns();
    for (int j = 0; j < userColsList.size(); j++) {
      String colKey = userColsList.get(j).getElementKey();
      rowContent.put(colKey, (String)row.getDataByKey(colKey + suffix));
    }
  }

  private String findNewSyncState(String stateA, String stateB, String etagA, String etagB) {
    if ((stateA.equals(SubmitSyncStates.P_SYNCED)
        && stateB.equals(SubmitSyncStates.P_SYNCED)
        && !etagA.equals(etagB))
        || (stateA.equals(SubmitSyncStates.P_DIVERGENT)
        && stateB.equals(SubmitSyncStates.P_SYNCED))
        || (stateB.equals(SubmitSyncStates.P_DIVERGENT)
        && stateA.equals(SubmitSyncStates.P_SYNCED))
        || (stateB.equals(SubmitSyncStates.P_DIVERGENT)
        && stateA.equals(SubmitSyncStates.P_DIVERGENT))) {
      return SubmitSyncStates.P_DIVERGENT;
    } else {
      return SubmitSyncStates.P_CONFLICT;
    }
  }

  private boolean isAdminColumn(String columnName) {
    return adminColumnSet.contains(columnName) ||
        otherAdminColumnSet.contains(columnName);
  }
}
