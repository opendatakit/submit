package org.opendatakit.submit.application;

import com.squareup.leakcanary.LeakCanary;

import org.opendatakit.application.CommonApplication;

import org.opendatakit.submit.R;

public class Submit extends CommonApplication {
  @Override
  public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      return;
    }

    LeakCanary.install(this);
  }

  @Override
  public int getConfigZipResourceId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSystemZipResourceId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getApkDisplayNameResourceId() {
    return R.string.app_name;
  }
}
