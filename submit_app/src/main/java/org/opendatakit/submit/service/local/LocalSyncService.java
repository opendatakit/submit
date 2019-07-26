package org.opendatakit.submit.service.local;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.listener.DatabaseConnectionListener;
import org.opendatakit.submit.consts.SubmitConsts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalSyncService extends Service implements DatabaseConnectionListener {
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

  private static final String TAG = LocalSyncService.class.getSimpleName();

  // each app name needs its own instance
  private final Map<String, LocalAidlSynchronizer> synchronizers = new ConcurrentHashMap<>();

  @Override
  public void onCreate() {
    super.onCreate();
    Log.e(TAG, "onCreate: ");

    ((CommonApplication) getApplication()).onActivityResume(null);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.e(TAG, "onBind: ");

    possiblyWaitForSyncServiceDebugger();

    if (!verifyBindIntent(intent)) {
      return null;
    }

    ((CommonApplication) getApplication()).onActivityResume(null);

    String appName = intent.getStringExtra(IntentConsts.INTENT_KEY_APP_NAME);
    String primaryAppName = appName.substring(SubmitConsts.SECONDARY_APP_NAME_PREFIX.length());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // putIfAbsent is atomic
      synchronizers.putIfAbsent(appName,
          new LocalAidlSynchronizer(appName, primaryAppName, (CommonApplication) getApplication()));
    } else {
      synchronized (synchronizers) {
        if (!synchronizers.containsKey(appName)) {
          synchronizers.put(appName,
              new LocalAidlSynchronizer(appName, primaryAppName, ((CommonApplication) getApplication())));
        }
      }
    }

    return synchronizers.get(appName);
  }

  @Override
  public void databaseAvailable() {
    // TODO:
  }

  @Override
  public void databaseUnavailable() {
    // TODO:
  }

  private boolean verifyBindIntent(Intent intent) {
    // TODO:
    return intent.hasExtra(IntentConsts.INTENT_KEY_APP_NAME);
  }
}
