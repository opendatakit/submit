package org.opendatakit.submit.service.peer.server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.PeerServerConsts;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class GetTableDefHandler extends GetHandler {
  @Override
  public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    UserDbInterface db = getDatabase(uriResource.initParameter(CommonApplication.class));
    String appName = getAppName(session);
    List<String> tableDefUri = session.getParameters().get(PeerServerConsts.TABLE_DEF_URI_QUERY);

    DbHandle handle = null;
    try {
      handle = db.openDatabase(appName);

      TableDefinitionEntry tableDef = db.getTableDefinitionEntry(appName, handle, tableDefUri.get(0));
      OrderedColumns columns =
          db.getUserDefinedColumns(appName, handle, tableDefUri.get(0));

      if (tableDef.getSchemaETag() == null) {
        tableDef.setSchemaETag("schemaETag" + tableDef.getTableId());
      }

      return jsonResponse(new TableDefinitionResource(tableDef.getTableId(), tableDef.getSchemaETag(), columns.getColumns()));
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
