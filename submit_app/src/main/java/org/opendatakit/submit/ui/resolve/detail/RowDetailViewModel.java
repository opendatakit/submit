package org.opendatakit.submit.ui.resolve.detail;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.ui.common.AppAwareViewModel;
import org.opendatakit.submit.util.SubmitUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RowDetailViewModel extends AppAwareViewModel {
  private String tableId;
  private String peerId;

  private MutableLiveData<List<TypedRow>> rowDetail;

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getPeerId() {
    return peerId;
  }

  public void setPeerId(String peerId) {
    this.peerId = peerId;
  }

  public MutableLiveData<List<TypedRow>> getRowDetail() {
    if (rowDetail == null) {
      rowDetail = new MutableLiveData<>();
    }

    return rowDetail;
  }

  public RowDetailViewModel(@NonNull Application application) {
    super(application);
  }

  public void getRowsInConflict() {
    new GetRowsAsyncTask(
        getDatabase(),
        SubmitUtil.getSecondaryAppName(getAppName()),
        rowDetail,
        tableId,
        peerId
    ).execute();
  }

  private static class GetRowsAsyncTask extends AsyncTask<Void, Void, List<TypedRow>> {
    private static final String TAG = GetRowsAsyncTask.class.getSimpleName();

    @NonNull
    private final UserDbInterface database;

    @NonNull
    private final String dbAppName;

    @NonNull
    private final MutableLiveData<List<TypedRow>> liveData;

    @NonNull
    private final String tableId;

    @NonNull
    private final String peerId;

    private GetRowsAsyncTask(@NonNull UserDbInterface database,
                             @NonNull String dbAppName,
                             @NonNull MutableLiveData<List<TypedRow>> liveData,
                             @NonNull String tableId,
                             @NonNull String peerId) {
      this.database = database;
      this.dbAppName = dbAppName;
      this.liveData = liveData;
      this.tableId = tableId;
      this.peerId = peerId;
    }

    @Override
    protected List<TypedRow> doInBackground(Void... voids) {
      DbHandle handle = null;

      try {
        handle = database.openDatabase(dbAppName);

        OrderedColumns orderedCol =
            database.getUserDefinedColumns(dbAppName, handle, tableId);

        UserTable table = database.simpleQuery(
            dbAppName,
            handle,
            tableId,
            orderedCol,
            SubmitColumns.P_ID + " = ?",
            new BindArgs(new Object[]{
                peerId
            }),
            null,
            null,
            null,
            null,
            null,
            null
        );

        List<TypedRow> rows = new ArrayList<>();
        for (int i = 0; i < table.getNumberOfRows(); i++) {
          rows.add(table.getRowAtIndex(i));
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
    protected void onPostExecute(List<TypedRow> typedRows) {
      super.onPostExecute(typedRows);

      liveData.setValue(typedRows);
    }
  }
}
