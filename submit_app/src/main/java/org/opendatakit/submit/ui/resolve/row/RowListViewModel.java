package org.opendatakit.submit.ui.resolve.row;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.Row;
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

public class RowListViewModel extends AppAwareViewModel {
  private String tableId;
  private MutableLiveData<List<ConflictingRow>> rows;

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public MutableLiveData<List<ConflictingRow>> getRows() {
    if (rows == null) {
      rows = new MutableLiveData<>();
    }

    return rows;
  }

  public RowListViewModel(@NonNull Application application) {
    super(application);
  }

  public void findRowsInConflict() {
    new FindRowsAsyncTask(
        getDatabase(),
        SubmitUtil.getSecondaryAppName(getAppName()),
        rows,
        tableId
    ).execute();
  }

  private static class FindRowsAsyncTask extends AsyncTask<Void, Void, List<ConflictingRow>> {
    private static final String TAG = FindRowsAsyncTask.class.getSimpleName();

    private static final String ROW_QUERY =
        "SELECT " +
            "DISTINCT " + SubmitColumns.P_ID + " " +
            "FROM %s WHERE " + SubmitColumns.P_STATE + " IN (?, ?)";

    @NonNull
    private final UserDbInterface database;

    @NonNull
    private final String dbAppName;

    @NonNull
    private final MutableLiveData<List<ConflictingRow>> liveData;

    @NonNull
    private final String tableId;

    private FindRowsAsyncTask(@NonNull UserDbInterface database,
                              @NonNull String dbAppName,
                              @NonNull MutableLiveData<List<ConflictingRow>> liveData,
                              @NonNull String tableId) {
      this.database = database;
      this.dbAppName = dbAppName;
      this.liveData = liveData;
      this.tableId = tableId;
    }

    @Override
    protected List<ConflictingRow> doInBackground(Void... voids) {
      DbHandle handle = null;

      try {
        handle = database.openDatabase(dbAppName);

        BaseTable table = database.arbitrarySqlQuery(
            dbAppName,
            handle,
            tableId,
            String.format(ROW_QUERY, tableId),
            new BindArgs(new Object[]{
                SubmitSyncStates.P_CONFLICT,
                SubmitSyncStates.P_DIVERGENT
            }),
            null,
            null
        );

        List<ConflictingRow> rows = new ArrayList<>();
        for (Row row : table.getRows()) {
          rows.add(new ConflictingRow(row.getRawStringByKey(SubmitColumns.P_ID)));
        }

        return rows;
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
    protected void onPostExecute(List<ConflictingRow> conflictingRows) {
      super.onPostExecute(conflictingRows);

      liveData.setValue(conflictingRows);
    }
  }
}
