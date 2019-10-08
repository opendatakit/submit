package org.opendatakit.submit.service.peer.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.submit.consts.PeerServerConsts;
import org.opendatakit.submit.service.peer.server.handlers.CreateTableHandler;
import org.opendatakit.submit.service.peer.server.handlers.GetTableDefHandler;
import org.opendatakit.submit.service.peer.server.handlers.GetTableHandler;
import org.opendatakit.submit.service.peer.server.handlers.GetTablesHandler;
import org.opendatakit.submit.service.peer.server.handlers.GetUpdatesHandler;
import org.opendatakit.submit.service.peer.server.handlers.PostRowsHandler;
import org.opendatakit.submit.service.peer.server.handlers.VerifyAppNameHandler;

import java.io.IOException;

import fi.iki.elonen.router.RouterNanoHTTPD;

public class PeerSyncServer extends RouterNanoHTTPD {
  private static final String TAG = PeerSyncServer.class.getSimpleName();

  private final CommonApplication application;

  private CommonApplication getApplication() {
    return application;
  }

  public PeerSyncServer(@Nullable String hostname, int port, @NonNull CommonApplication application) {
    super(hostname, port);

    this.application = application;

    addMappings();
  }

  @Override
  public void start() throws IOException {
    super.start();

    Log.d(TAG, "start: STARTED on " + getHostname() + " port " + getListeningPort());
  }

  @Override
  public void addMappings() {
    super.addMappings();

    removeRoute("/");
    removeRoute("/index.html");

    addRoute("/" + PeerServerConsts.VERIFY_SERVER_APP_NAME_PATH, VerifyAppNameHandler.class);
    addRoute("/" + PeerServerConsts.GET_TABLES_PATH, GetTablesHandler.class, getApplication());
    addRoute("/" + PeerServerConsts.GET_TABLE_PATH, GetTableHandler.class, getApplication());
    addRoute("/" + PeerServerConsts.GET_TABLE_DEF_PATH, GetTableDefHandler.class, getApplication());
    addRoute("/" + PeerServerConsts.CREATE_TABLE_PATH, CreateTableHandler.class, getApplication());
    addRoute("/" + PeerServerConsts.GET_UPDATES_PATH, GetUpdatesHandler.class, getApplication());
    addRoute("/" + PeerServerConsts.POST_ROWS_PATH, PostRowsHandler.class, getApplication());
  }
}
