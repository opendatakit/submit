package org.opendatakit.submit.ui.resolve.row;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendatakit.submit.R;
import org.opendatakit.submit.ui.common.AbsBaseFragment;
import org.opendatakit.submit.ui.common.OnClickListenerHolder;
import org.opendatakit.submit.ui.resolve.detail.RowDetailFragment;

import java.util.List;

public class RowListFragment extends AbsBaseFragment implements OnClickListenerHolder<ConflictingRow> {
  private static final String TAG = RowListFragment.class.getSimpleName();

  private static final String TABLE_ID_ARG = "tableId";

  private RowListViewModel viewModel;
  private RowListAdapter adapter;

  public static RowListFragment newInstance(@NonNull String tableId) {
    RowListFragment fragment = new RowListFragment();

    Bundle args = new Bundle();
    args.putString(TABLE_ID_ARG, tableId);

    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.viewModel = ViewModelProviders
        .of(this)
        .get(RowListViewModel.class);

    this.viewModel.setAppName(getAppName());
    this.viewModel.setTableId(getArguments().getString(TABLE_ID_ARG));

    this.adapter = new RowListAdapter(this);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_resolve_row_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    configRecyclerView(ViewCompat.<RecyclerView>requireViewById(view, R.id.resolve_row_list));

    ViewCompat
        .<TextView>requireViewById(view, R.id.resolve_row_table_id)
        .setText(viewModel.getTableId());

    viewModel.getRows().observe(getViewLifecycleOwner(), new Observer<List<ConflictingRow>>() {
      @Override
      public void onChanged(@Nullable List<ConflictingRow> conflictingRows) {
        adapter.submitList(conflictingRows);

        // no more conflicts in this table
        if (conflictingRows == null || conflictingRows.isEmpty()) {
          requireFragmentManager().popBackStack();
        }
      }
    });

    viewModel.findRowsInConflict();
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

  @Override
  public View.OnClickListener getListener(final ConflictingRow item) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requireFragmentManager()
            .beginTransaction()
            .replace(
                R.id.main_content,
                RowDetailFragment.newInstance(viewModel.getTableId(), item.getPeerId())
            )
            .addToBackStack(null)
            .commit();
      }
    };
  }
}
