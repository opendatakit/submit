package org.opendatakit.submit.service.peer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.listener.DatabaseConnectionListener;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;

public class PeerSyncServerService extends Service implements DatabaseConnectionListener {
  /**
   * change to true expression if you want to debug the Sync service
   */
  public static boolean possiblyWaitForSyncServiceDebugger() {
    if ( false ) {
      android.os.Debug.waitForDebugger();
      int len = "for setting breakpoint".length();
      return true;
    }
    return false;
  }

  private static final String TAG = PeerSyncServerService.class.getSimpleName();

  private PeerSyncServer server;
  private PeerSyncServerBinder serverBinder;

  public PeerSyncServer getServer() {
    return server;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    ((CommonApplication) getApplication()).onActivityResume(null);

    server = new PeerSyncServer("0.0.0.0", 8080, (CommonApplication) getApplication());
    serverBinder = new PeerSyncServerBinder();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.i(TAG, "onBind: ");
    possiblyWaitForSyncServiceDebugger();

    ((CommonApplication) getApplication()).onActivityResume(null);

    return serverBinder;
  }

  @Override
  public void databaseAvailable() {

  }

  @Override
  public void databaseUnavailable() {

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (getServer() != null) {
      getServer().stop();
    }
  }

  public class PeerSyncServerBinder extends Binder {
    public PeerSyncServer getServer() {
      return PeerSyncServerService.this.getServer();
    }
  }
}
