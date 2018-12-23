package org.opendatakit.submit.service.peer.server.handlers;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.PeerServerConsts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;

public abstract class BaseHandler {
  private static final String TAG = BaseHandler.class.getSimpleName();

  private static final String MIME_JSON = "application/json";

  private final ObjectMapper objectMapper;
  private final Set<String> adminColumnSet;

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public BaseHandler() {
    this.objectMapper = new ObjectMapper();
    this.adminColumnSet = new HashSet<>(DataTableColumns.getAdminColumns());
    this.adminColumnSet.add(DataTableColumns.EFFECTIVE_ACCESS);
  }

  public String getAppName(NanoHTTPD.IHTTPSession session) {
    List<String> appNames = session.getParameters().get(PeerServerConsts.APP_NAME_QUERY);

    if (appNames != null && appNames.size() == 1) {
      return appNames.get(0);
    }

    return null;
  }

  public UserDbInterface getDatabase(CommonApplication application) {
    // TODO: Use Handler
    while (application.getDatabase() == null) {
      try {
        Log.e(TAG, "getDatabase: SLEEP");
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return application.getDatabase();
  }

  public TableResource getTableResFromTableDefEntry(TableDefinitionEntry entry) {
    if (entry.getSchemaETag() == null) {
      entry.setSchemaETag("schemaETag" + entry.getTableId());
    }

    TableResource tableResource = new TableResource(new TableEntry(
        entry.getTableId(),
        entry.getLastDataETag(),
        entry.getSchemaETag()
    ));
    tableResource.setDefinitionUri(entry.getTableId());

    return tableResource;
  }

  public boolean isAdminColumn(String column) {
    return adminColumnSet.contains(column);
  }

  public NanoHTTPD.Response jsonResponse(Object response) throws JsonProcessingException {
    return jsonResponse(NanoHTTPD.Response.Status.OK, response);
  }

  protected NanoHTTPD.Response jsonResponse(NanoHTTPD.Response.IStatus status, Object response)
      throws JsonProcessingException {
    Log.d(TAG, "jsonResponse: " + getObjectMapper().writeValueAsString(response));

    return NanoHTTPD.newFixedLengthResponse(
        status,
        MIME_JSON,
        getObjectMapper().writeValueAsString(response)
    );
  }

  public NanoHTTPD.Response errorResponse() {
    return errorResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, null);
  }

  public NanoHTTPD.Response errorResponse(NanoHTTPD.Response.IStatus status, String message) {
    return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_PLAINTEXT, message);
  }
}
