package org.opendatakit.submit.ui.resolve.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.R;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.ui.common.AbsBaseFragment;
import org.opendatakit.submit.ui.common.OnClickListenerHolder;
import org.opendatakit.submit.util.SubmitUtil;

import java.util.List;

public class RowDetailFragment extends AbsBaseFragment implements OnClickListenerHolder<TypedRow> {
  private static final String TAG = RowDetailFragment.class.getSimpleName();

  private static final String DELETE_ALL_BUT_CMD =
      "DELETE FROM %s " +
          "WHERE " + SubmitColumns.P_ID + " = ? " +
          "AND " + DataTableColumns.ID + " != ?";

  private static final String TABLE_ID_ARG = "tableId";
  private static final String PEER_ID_ARG = "peerId";

  private RowDetailViewModel viewModel;
  private RowDetailAdapter adapter;

  public static RowDetailFragment newInstance(@NonNull String tableId,
                                              @NonNull String peerId) {
    RowDetailFragment fragment = new RowDetailFragment();

    Bundle bundle = new Bundle();
    bundle.putString(TABLE_ID_ARG, tableId);
    bundle.putString(PEER_ID_ARG, peerId);

    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.viewModel = ViewModelProviders
        .of(this)
        .get(RowDetailViewModel.class);

    this.viewModel.setAppName(getAppName());
    this.viewModel.setTableId(getArguments().getString(TABLE_ID_ARG));
    this.viewModel.setPeerId(getArguments().getString(PEER_ID_ARG));

    this.adapter = new RowDetailAdapter(this);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_resolve_row_detail, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    configRecyclerView(ViewCompat.<RecyclerView>requireViewById(view, R.id.resolve_row_detail));

    ViewCompat
        .<TextView>requireViewById(view, R.id.resolve_row_detail_table_id)
        .setText(viewModel.getTableId());

    ViewCompat
        .<TextView>requireViewById(view, R.id.resolve_row_detail_peer_id)
        .setText(viewModel.getPeerId());

    viewModel.getRowDetail().observe(getViewLifecycleOwner(), new Observer<List<TypedRow>>() {
      @Override
      public void onChanged(@Nullable List<TypedRow> typedRows) {
        adapter.submitList(typedRows);
      }
    });

    viewModel.getRowsInConflict();
  }

  private void configRecyclerView(@NonNull RecyclerView rv) {
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(requireContext());
    DividerItemDecoration itemDecoration =
        new DividerItemDecoration(requireContext(), layoutManager.getOrientation());

    rv.setLayoutManager(layoutManager);
    rv.addItemDecoration(itemDecoration);
    rv.setAdapter(adapter);
  }

  private boolean deleteRowsExcept(String rowId) {
    // TODO: this method should be moved to another class

    String dbAppName = SubmitUtil.getSecondaryAppName(getAppName());
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      OrderedColumns colDef = getDatabase().getUserDefinedColumns(
          dbAppName,
          handle,
          viewModel.getTableId()
      );

      ContentValues newState = new ContentValues();
      newState.put(SubmitColumns.P_STATE, SubmitSyncStates.P_SYNCED);

      // TODO: handle ActionNotAuthorizedException
      getDatabase().updateRowWithId(
          dbAppName,
          handle,
          viewModel.getTableId(),
          colDef,
          newState,
          rowId
      );

      getDatabase().privilegedExecute(
          dbAppName,
          handle,
          String.format(DELETE_ALL_BUT_CMD, viewModel.getTableId()),
          new BindArgs(new Object[] {
              viewModel.getPeerId(),
              rowId
          })
      );

      return true;
    } catch (ServicesAvailabilityException | ActionNotAuthorizedException e) {
      Log.e(TAG, "deleteRowsExcept: ", e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "deleteRowsExcept: ", e);
        }
      }
    }

    return false;
  }

  @Override
  public View.OnClickListener getListener(final TypedRow item) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean succeeded = deleteRowsExcept(item.getRawStringByKey(DataTableColumns.ID));

        requireFragmentManager().popBackStack();
        Snackbar
            .make(
                getView(),
                succeeded ? R.string.resolve_conflict_selected : R.string.resolve_conflict_selected_failed,
                Snackbar.LENGTH_SHORT
            )
            .show();
      }
    };
  }
}
