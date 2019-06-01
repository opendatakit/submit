package org.opendatakit.submit.service.peer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.listener.DatabaseConnectionListener;

public class PeerSyncService extends Service implements DatabaseConnectionListener {
  /**
   * change to true expression if you want to debug the Sync service
   */
  public static boolean possiblyWaitForSyncServiceDebugger() {
    if (false) {
      android.os.Debug.waitForDebugger();
      int len = "for setting breakpoint".length();
      return true;
    }
    return false;
  }

  private static final String TAG = PeerSyncService.class.getSimpleName();

  private PeerAidlSynchronizer peerAidlSynchronizer;

  @Override
  public void onCreate() {
    super.onCreate();

    ((CommonApplication) getApplication()).onActivityResume(null);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.e(TAG, "onBind: ");

    possiblyWaitForSyncServiceDebugger();

    ((CommonApplication) getApplication()).onActivityResume(null);

    if (peerAidlSynchronizer == null) {
      peerAidlSynchronizer = new PeerAidlSynchronizer(
          intent.getStringExtra(IntentConsts.INTENT_KEY_APP_NAME),
          (CommonApplication) getApplication()
      );
    }

    return peerAidlSynchronizer;
  }

  @Override
  public void databaseAvailable() {
    // TODO:
  }

  @Override
  public void databaseUnavailable() {
    // TODO:
  }
}
