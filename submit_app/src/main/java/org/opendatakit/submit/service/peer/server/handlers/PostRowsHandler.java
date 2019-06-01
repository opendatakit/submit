package org.opendatakit.submit.service.peer.server.handlers;

import android.content.ContentValues;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.PeerServerConsts;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.util.RowUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class PostRowsHandler extends PostHandler {
  private static final String TAG = PostRowsHandler.class.getSimpleName();

  private static final String P_STATE_UPDATE_CMD =
      "UPDATE " +
          "%s " +
          "SET " + SubmitColumns.P_STATE + " = ? " +
          "WHERE " +
          SubmitColumns.P_ID + " " +
          "IN (" +
          "SELECT " + SubmitColumns.P_ID + " FROM %s WHERE " + SubmitColumns.P_STATE + " = ?" +
          ")";

  @Override
  public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    UserDbInterface db = getDatabase(uriResource.initParameter(CommonApplication.class));
    String appName = getAppName(session);
    List<String> tableId = session.getParameters().get(PeerServerConsts.TABLE_ID_QUERY);

    DbHandle handle = null;
    try {
      handle = db.openDatabase(appName);

      session.parseBody(null);
      String rowsJson = session.getParameters().get(PeerServerConsts.ROWS_FORM_PARAM).get(0);
      List<Row> rows = getObjectMapper().readValue(rowsJson, new TypeReference<List<Row>>() {});

      OrderedColumns colDef = db.getUserDefinedColumns(appName, handle, tableId.get(0));
      RowOutcomeList outcomeList = new RowOutcomeList();

      for (Row row : rows) {
        if (!row.getValues().isEmpty()) {
          ContentValues cv = new ContentValues();

          for (DataKeyValue dkv : row.getValues()) {
            cv.put(dkv.column, dkv.value);
          }

          cv.put(DataTableColumns.ID, row.getRowId());
          cv.put(DataTableColumns.FORM_ID, row.getFormId());
          cv.put(DataTableColumns.SAVEPOINT_CREATOR, row.getSavepointCreator());
          cv.put(DataTableColumns.SAVEPOINT_TIMESTAMP, row.getSavepointTimestamp());
          cv.put(DataTableColumns.SAVEPOINT_TYPE, row.getSavepointType());
          cv.put(DataTableColumns.LOCALE, row.getLocale());
          cv.put(DataTableColumns.ROW_ETAG, row.getRowETag());
          cv.putAll(RowUtil.getRowFilterScope(row));

          cv.put(DataTableColumns.SYNC_STATE, SyncState.changed.toString());
          cv.putNull(DataTableColumns.CONFLICT_TYPE);

          db.privilegedInsertRowWithId(
              appName,
              handle,
              tableId.get(0),
              colDef,
              cv,
              row.getRowId(),
              false
          );
        }

        RowOutcome outcome = new RowOutcome(row);
        outcome.setOutcome(RowOutcome.OutcomeType.SUCCESS);
        outcomeList.getRows().add(outcome);
      }

      fixPState(db, handle, appName, tableId.get(0));

      return jsonResponse(new String[] {});
    } catch (ServicesAvailabilityException | IOException | NanoHTTPD.ResponseException e) {
      Log.e(TAG, "PostRowsHandler: ", e);
    } finally {
      if (handle != null) {
        try {
          db.closeDatabase(appName, handle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "PostRowsHandler: ", e);
        }
      }
    }

    return errorResponse();
  }

  private void fixPState(UserDbInterface db, DbHandle handle, String appName, String tableId) throws ServicesAvailabilityException {
    db.privilegedExecute(
        appName,
        handle,
        String.format(P_STATE_UPDATE_CMD, tableId, tableId),
        new BindArgs(new Object[] {
            SubmitSyncStates.P_CONFLICT,
            SubmitSyncStates.P_CONFLICT
        })
    );

    db.privilegedExecute(
        appName,
        handle,
        String.format(P_STATE_UPDATE_CMD, tableId, tableId),
        new BindArgs(new Object[] {
            SubmitSyncStates.P_DIVERGENT,
            SubmitSyncStates.P_DIVERGENT
        })
    );
  }
}
