package org.opendatakit.submit.service.peer.server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class GetTablesHandler extends GetHandler {
  @Override
  public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
    UserDbInterface db = getDatabase(uriResource.initParameter(CommonApplication.class));
    String appName = getAppName(session);

    DbHandle handle = null;
    try {
      handle = db.openDatabase(appName);

      TableResourceList list = new TableResourceList();

      for (String id : db.getAllTableIds(appName, handle)) {
        TableDefinitionEntry tableDef =
            db.getTableDefinitionEntry(appName, handle, id);

        list.getTables().add(getTableResFromTableDefEntry(tableDef));
      }

      list.setAppLevelManifestETag(db.getManifestSyncETag(appName, handle, null, null));
      list.setHasMoreResults(false);

      return jsonResponse(list);
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
