package org.opendatakit.submit.ui.resolve.table;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.ui.common.AppAwareViewModel;
import org.opendatakit.submit.util.SubmitUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableListViewModel extends AppAwareViewModel {
  private static final String TAG = TableListViewModel.class.getSimpleName();

  private MutableLiveData<List<ConflictingTable>> tables;

  @NonNull
  public MutableLiveData<List<ConflictingTable>> getTables() {
    if (tables == null) {
      tables = new MutableLiveData<>();
    }

    return tables;
  }

  public TableListViewModel(@NonNull Application application) {
    super(application);
  }

  public void findTablesInConflict() {
    Log.e(TAG, "findTablesInConflict: " + hashCode());
    
    new FindTablesAsyncTask(
        getDatabase(),
        SubmitUtil.getSecondaryAppName(getAppName()),
        tables
    ).execute();
  }

  private static class FindTablesAsyncTask extends AsyncTask<Void, Void, List<ConflictingTable>> {
    private static final String TAG = FindTablesAsyncTask.class.getSimpleName();

    private static final String CONFLICT_COUNT_COL = "count";

    private static final String COUNT_CONFLICT_QUERY =
        "SELECT " +
            "COUNT(DISTINCT " + SubmitColumns.P_ID + ") AS '" + CONFLICT_COUNT_COL + "' " +
            "FROM %s WHERE " + SubmitColumns.P_STATE + " IN (?, ?)";

    @NonNull
    private final UserDbInterface database;

    @NonNull
    private final String dbAppName;

    @NonNull
    private final MutableLiveData<List<ConflictingTable>> liveData;

    private FindTablesAsyncTask(@NonNull UserDbInterface database,
                                @NonNull String dbAppName,
                                @NonNull MutableLiveData<List<ConflictingTable>> liveData) {
      this.database = database;
      this.dbAppName = dbAppName;
      this.liveData = liveData;
    }

    @Override
    protected List<ConflictingTable> doInBackground(Void... voids) {
      DbHandle handle = null;

      try {
        handle = database.openDatabase(dbAppName);

        List<String> tableIds = database.getAllTableIds(dbAppName, handle);
        List<ConflictingTable> conflictingTables = new ArrayList<>();

        for (String tableId : tableIds) {
          BaseTable baseTable = database.arbitrarySqlQuery(
              dbAppName,
              handle,
              tableId,
              String.format(COUNT_CONFLICT_QUERY, tableId),
              new BindArgs(new Object[] {
                  SubmitSyncStates.P_CONFLICT,
                  SubmitSyncStates.P_DIVERGENT
              }),
              null,
              null
          );

          int count =
              Integer.parseInt(baseTable.getRowAtIndex(0).getRawStringByKey(CONFLICT_COUNT_COL));

          if (count < 1) {
            continue;
          }

          // TODO: the _last_sync_time column isn't populated by Services
          conflictingTables.add(new ConflictingTable(tableId, count, null));
        }

        return conflictingTables;
      } catch (ServicesAvailabilityException e) {
        Log.e(TAG, "doInBackground: ", e);
      } finally {
        if (handle != null) {
          try {
            database.closeDatabase(dbAppName, handle);
          } catch (ServicesAvailabilityException e) {
            Log.e(TAG, "doInBackground: ", e);
          }
        }
      }

      return Collections.emptyList();
    }

    @Override
    protected void onPostExecute(List<ConflictingTable> conflictingTable) {
      super.onPostExecute(conflictingTable);

      liveData.setValue(conflictingTable);
    }
  }
}
