package org.opendatakit.submit.ui.common;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.opendatakit.application.CommonApplication;
import org.opendatakit.database.service.UserDbInterface;

public class AppAwareViewModel extends AndroidViewModel {
  private String appName;

  public String getAppName() {
    return appName;
  }

  public void setAppName(@NonNull String appName) {
    this.appName = appName;
  }

  @Nullable
  public UserDbInterface getDatabase() {
    return this.<CommonApplication>getApplication().getDatabase();
  }

  public AppAwareViewModel(@NonNull Application application) {
    super(application);
  }
}
