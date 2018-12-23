package org.opendatakit.submit.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opendatakit.activities.BaseActivity;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.application.CommonApplication;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.ui.common.SharedViewModel;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.utilities.ODKFileUtils;

import java.util.List;

public abstract class SubmitBaseActivity extends BaseActivity {
  private static final String TAG = SubmitBaseActivity.class.getSimpleName();

  private static final String P_STATE_UPDATE_CMD =
      "UPDATE " +
          "%s " +
          "SET " + SubmitColumns.P_STATE + " = ? " +
          "WHERE " +
          SubmitColumns.P_ID + " " +
          "IN (" +
          "SELECT " + SubmitColumns.P_ID + " FROM %s WHERE " + SubmitColumns.P_STATE + " = ?" +
          ")";

  private static final String SYNC_STATE_UPDATE_CMD =
      "UPDATE %s SET " + DataTableColumns.SYNC_STATE + " = ?";

  protected SharedViewModel sharedViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.sharedViewModel = ViewModelProviders
        .of(this)
        .get(SharedViewModel.class);

    String appName = getIntent().getStringExtra(IntentConsts.INTENT_KEY_APP_NAME);
    if (appName == null) {
      appName = ODKFileUtils.getOdkDefaultAppName();
    }

    this.sharedViewModel.setAppName(appName);
  }

  @Override
  protected void onResume() {
    super.onResume();
    ((CommonApplication) getApplication()).establishDoNotFireDatabaseConnectionListener(this);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    ((CommonApplication) getApplication()).fireDatabaseConnectionListener();
  }

  @NonNull
  @Override
  public String getAppName() {
    return sharedViewModel.getAppName();
  }

  @Nullable
  public UserDbInterface getDatabase() {
    return ((CommonApplication) getApplication()).getDatabase();
  }

  // TODO: MOVE THIS
  /**
   * 1. Make sure that rows sharing the same p_id all have the same p_state
   * 2. Make sure that _sync_state = changed
   */
  public void fixSubmitStates() {
    String dbAppName = SubmitUtil.getSecondaryAppName(getAppName());
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      List<String> tableIds = getDatabase().getAllTableIds(dbAppName, handle);

      for (String tableId : tableIds) {
        getDatabase().privilegedExecute(
            dbAppName,
            handle,
            String.format(P_STATE_UPDATE_CMD, tableId, tableId),
            new BindArgs(new Object[] {
                SubmitSyncStates.P_CONFLICT,
                SubmitSyncStates.P_CONFLICT
            })
        );

        getDatabase().privilegedExecute(
            dbAppName,
            handle,
            String.format(P_STATE_UPDATE_CMD, tableId, tableId),
            new BindArgs(new Object[] {
                SubmitSyncStates.P_DIVERGENT,
                SubmitSyncStates.P_DIVERGENT
            })
        );

        getDatabase().privilegedExecute(
            dbAppName,
            handle,
            String.format(SYNC_STATE_UPDATE_CMD, tableId),
            new BindArgs(new Object[] {
                SyncState.changed.toString()
            })
        );
      }
    } catch (ServicesAvailabilityException e) {
      Log.e(TAG, "fixSubmitStates: ", e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "fixSubmitStates: ", e);
        }
      }
    }
  }
}
