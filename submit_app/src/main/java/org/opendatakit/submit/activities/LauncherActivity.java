package org.opendatakit.submit.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.opendatakit.activities.BaseLauncherActivity;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.utilities.ODKFileUtils;

public class LauncherActivity extends BaseLauncherActivity {
  private String appName;

  @Override
  public String getAppName() {
    return appName;
  }

  @Override
  protected void setAppSpecificPerms() { /* empty */ }

  @Override
  protected void onCreateWithPermission(Bundle savedInstanceState) {
    boolean success = configureAppName();

    if (!success) {
      finish();
    }

    // validate directories
    try {
      ODKFileUtils.verifyExternalStorageAvailability();
      ODKFileUtils.assertDirectoryStructure(getAppName());
      ODKFileUtils.assertDirectoryStructure(SubmitUtil.getSecondaryAppName(getAppName()));
    } catch (Exception e) {
      // TODO:
      // log error + show error dialog
      finish();
    }

    // launch MainActivity
    Intent i = new Intent(this, MainActivity.class);

    Intent sourceIntent = getIntent();
    Uri data = sourceIntent.getData();
    Bundle extras = sourceIntent.getExtras();

    if (data != null) {
      i.setData(data);
    }
    if (extras != null) {
      i.putExtras(extras);
    }

    startActivity(i);
    finish();
  }

  @Override
  public void databaseAvailable() {}

  @Override
  public void databaseUnavailable() {}

  /**
   * Validate and store app name
   *
   * @return true if app name is valid
   */
  private boolean configureAppName() {
    String appName = getIntent().getStringExtra(IntentConsts.INTENT_KEY_APP_NAME);

    if (appName == null) {
      appName = ODKFileUtils.getOdkDefaultAppName();
    }

    // TODO: add intent uri validation after implementing shortcuts

    this.appName = appName;

    return true;
  }
}
