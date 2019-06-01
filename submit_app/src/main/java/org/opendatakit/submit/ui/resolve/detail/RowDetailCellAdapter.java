package org.opendatakit.submit.ui.resolve.detail;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendatakit.submit.R;

public class RowDetailCellAdapter extends ListAdapter<ColumnValuePair, RowDetailCellAdapter.ViewHolder> {
  private static final String TAG = RowDetailCellAdapter.class.getSimpleName();

  public static final DiffUtil.ItemCallback<ColumnValuePair> DEFAULT_DIFF_CALLBACK =
      new DiffUtil.ItemCallback<ColumnValuePair>() {
        @Override
        public boolean areItemsTheSame(ColumnValuePair oldItem, ColumnValuePair newItem) {
          return oldItem.getColumn().equals(newItem.getColumn());
        }

        @Override
        public boolean areContentsTheSame(ColumnValuePair oldItem, ColumnValuePair newItem) {
          return oldItem.equals(newItem);
        }
      };

  public RowDetailCellAdapter() {
    this(DEFAULT_DIFF_CALLBACK);
  }

  public RowDetailCellAdapter(@NonNull DiffUtil.ItemCallback<ColumnValuePair> diffCallback) {
    super(diffCallback);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resolve_row_detail_cell, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bindTo(getItem(position));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
      super(itemView);
    }

    public void bindTo(final ColumnValuePair row) {
      ViewCompat
          .<TextView>requireViewById(itemView, R.id.resolve_row_detail_column)
          .setText(row.getColumn());

      ViewCompat
          .<TextView>requireViewById(itemView, R.id.resolve_row_detail_value)
          .setText(row.getValue());
    }
  }
}
