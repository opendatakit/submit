package org.opendatakit.submit.service.peer.server.handlers;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.PeerServerConsts;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class CreateTableHandler extends PostHandler {
  @Override
  public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    UserDbInterface db = getDatabase(uriResource.initParameter(CommonApplication.class));
    String appName = getAppName(session);
    List<String> tableId = session.getParameters().get(PeerServerConsts.TABLE_ID_QUERY);
    List<String> schemaETag = session.getParameters().get(PeerServerConsts.SCHEMA_ETAG_QUERY);

    DbHandle handle = null;
    try {
      handle = db.openDatabase(appName);

      session.parseBody(null);

      String columnsJson = session.getParameters().get(PeerServerConsts.COLUMNS_FORM_PARAM).get(0);
      List<Column> columnList = getObjectMapper().readValue(columnsJson, new TypeReference<List<Column>>() {});
      db.createOrOpenTableWithColumns(
          appName,
          handle,
          tableId.get(0),
          new ColumnList(columnList)
      );

      return jsonResponse(getTableResFromTableDefEntry(db.getTableDefinitionEntry(appName, handle, tableId.get(0))));
    } catch (ServicesAvailabilityException | IOException | NanoHTTPD.ResponseException e) {
      // TODO: send exception back
      Log.e("CreateTableHandler", "post: ", e);
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
