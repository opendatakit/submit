package org.opendatakit.submit.ui.resolve.detail;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.opendatakit.database.data.TypedRow;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.submit.R;
import org.opendatakit.submit.ui.common.OnClickListenerHolder;

import java.util.ArrayList;
import java.util.List;

public class RowDetailAdapter extends ListAdapter<TypedRow, RowDetailAdapter.ViewHolder> {
  private static final String TAG = RowDetailAdapter.class.getSimpleName();

  private final OnClickListenerHolder<TypedRow> onClickListener;

  public static final DiffUtil.ItemCallback<TypedRow> DEFAULT_DIFF_CALLBACK =
      new DiffUtil.ItemCallback<TypedRow>() {
        @Override
        public boolean areItemsTheSame(TypedRow oldItem, TypedRow newItem) {
          return oldItem.getRawStringByKey(DataTableColumns.ID).equals(newItem.getRawStringByKey(DataTableColumns.ID));
        }

        @Override
        public boolean areContentsTheSame(TypedRow oldItem, TypedRow newItem) {
          // TODO: find a better way
          return areItemsTheSame(oldItem, newItem);
        }
      };

  public RowDetailAdapter(@NonNull OnClickListenerHolder<TypedRow> onClickListener) {
    this(DEFAULT_DIFF_CALLBACK, onClickListener);
  }

  public RowDetailAdapter(@NonNull DiffUtil.ItemCallback<TypedRow> diffCallback,
                          @NonNull OnClickListenerHolder<TypedRow> onClickListener) {
    super(diffCallback);

    this.onClickListener = onClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resolve_row_detail, parent, false),
        onClickListener
    );
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bindTo(getItem(position));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final RowDetailCellAdapter adapter;
    private final OnClickListenerHolder<TypedRow> onClickListener;

    public ViewHolder(View itemView, @NonNull OnClickListenerHolder<TypedRow> onClickListener) {
      super(itemView);

      this.adapter = new RowDetailCellAdapter();
      this.onClickListener = onClickListener;
    }

    public void bindTo(final TypedRow row) {
      RecyclerView rowCellRv = ViewCompat
          .requireViewById(itemView, R.id.resolve_row_detail_cell);

      configRecyclerView(rowCellRv);

      ViewCompat
          .<Button>requireViewById(itemView, R.id.resolve_row_select)
          .setOnClickListener(onClickListener.getListener(row));

      adapter.submitList(typedRowToPair(row));
    }

    private void configRecyclerView(@NonNull RecyclerView rv) {
      LinearLayoutManager layoutManager =
          new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false);
      DividerItemDecoration itemDecoration =
          new DividerItemDecoration(itemView.getContext(), layoutManager.getOrientation());

      rv.setLayoutManager(layoutManager);
      rv.addItemDecoration(itemDecoration);
      rv.setAdapter(adapter);
    }

    private List<ColumnValuePair> typedRowToPair(TypedRow row) {
      Log.e(TAG, "typedRowToPair: converting TypedRow to list of ColumnValuePair for " + row.getRawStringByKey(DataTableColumns.ID));

      List<ColumnValuePair> pairs = new ArrayList<>();

      for (String key : row.getElementKeyForIndexMap()) {
        pairs.add(new ColumnValuePair(key, row.getStringValueByKey(key)));
      }

      return pairs;
    }
  }
}
