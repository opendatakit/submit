package org.opendatakit.submit.service;

import androidx.annotation.NonNull;

import org.opendatakit.sync.service.IOdkSyncServiceInterface;
import org.opendatakit.sync.service.SyncStatus;

import java.util.concurrent.Callable;

public class SyncDoneCondition implements Callable<Boolean> {
  @NonNull
  private final IOdkSyncServiceInterface syncServiceInterface;

  @NonNull
  private final String appName;

  public SyncDoneCondition(@NonNull IOdkSyncServiceInterface syncServiceInterface,
                           @NonNull String appName) {
    this.syncServiceInterface = syncServiceInterface;
    this.appName = appName;
  }

  @Override
  public Boolean call() throws Exception {
    return !SyncStatus.SYNCING.equals(syncServiceInterface.getSyncStatus(appName));
  }
}
