package org.opendatakit.submit.service.peer.server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.PeerServerConsts;
import org.opendatakit.submit.util.PeerSyncUtil;
import org.opendatakit.submit.util.RowUtil;
import org.opendatakit.sync.service.entity.ParcelableDataKeyValue;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class GetUpdatesHandler extends GetHandler {
  @Override
  public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    UserDbInterface db = getDatabase(uriResource.initParameter(CommonApplication.class));
    String appName = getAppName(session);
    List<String> tableId = session.getParameters().get(PeerServerConsts.TABLE_ID_QUERY);
    List<String> fetchLimit = session.getParameters().get(PeerServerConsts.FETCH_LIMIT_QUERY);

    DbHandle handle = null;
    try {
      handle = db.openDatabase(appName);

      OrderedColumns orderedColumns = db.getUserDefinedColumns(appName, handle, tableId.get(0));
      UserTable table = db.privilegedSimpleQuery(
          appName,
          handle,
          tableId.get(0),
          orderedColumns,
          null,
          null,
          null,
          null,
          null,
          null,
          Integer.parseInt(fetchLimit.get(0)),
          null
      );

      ArrayList<RowResource> rowResourceList = new ArrayList<>();
      for (int i = 0; i < table.getNumberOfRows(); i++) {
        TypedRow typedRow = table.getRowAtIndex(i);
        Row row = new Row();

        ArrayList<DataKeyValue> dkvl = new ArrayList<>();
        for (String key : table.getElementKeyToIndex().keySet()) {
          if (!isAdminColumn(key)) {
            dkvl.add(new ParcelableDataKeyValue(
                key,
                typedRow.getStringValueByKey(key)
            ));
          }
        }
        dkvl = PeerSyncUtil.tagDeviceId(dkvl, uriResource.initParameter(CommonApplication.class));
        row.setValues(dkvl);

        row.setRowId(typedRow.getRawStringByKey(DataTableColumns.ID));
        row.setRowETag(typedRow.getRawStringByKey(DataTableColumns.ROW_ETAG));
        row.setFormId(typedRow.getRawStringByKey(DataTableColumns.FORM_ID));
        row.setRowFilterScope(RowUtil.getRowFilterScope(typedRow));

        RowResource rowResource = new RowResource(row);
        rowResource.setSelfUri(row.getRowId());

        rowResourceList.add(rowResource);
      }

      return jsonResponse(new ParcelableRowResourceList(
          rowResourceList,
          null,
          tableId.get(0) + "self_uri",
          null,
          null,
          null,
          false,
          false
      ));
    } catch (ServicesAvailabilityException | JsonProcessingException e) {
      // TODO: send exception back
      e.printStackTrace();
    } finally {
      if (handle != null) {
        try {
          db.closeDatabase(appName, handle);
        } catch (ServicesAvailabilityException e) {
          e.printStackTrace();
        }
      }
    }

    return errorResponse();
  }
}
