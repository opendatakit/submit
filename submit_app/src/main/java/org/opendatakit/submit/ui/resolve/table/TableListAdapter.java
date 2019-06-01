package org.opendatakit.submit.ui.resolve.table;

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
import org.opendatakit.submit.ui.common.OnClickListenerHolder;

public class TableListAdapter extends ListAdapter<ConflictingTable, TableListAdapter.ViewHolder> {
  private static final String TAG = TableListAdapter.class.getSimpleName();

  private final OnClickListenerHolder<ConflictingTable> onClickListener;

  public static final DiffUtil.ItemCallback<ConflictingTable> DEFAULT_DIFF_CALLBACK =
      new DiffUtil.ItemCallback<ConflictingTable>() {
        @Override
        public boolean areItemsTheSame(ConflictingTable oldItem, ConflictingTable newItem) {
          return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(ConflictingTable oldItem, ConflictingTable newItem) {
          return oldItem.equals(newItem);
        }
      };

  public TableListAdapter(@NonNull OnClickListenerHolder<ConflictingTable> onClickListener) {
    this(DEFAULT_DIFF_CALLBACK, onClickListener);
  }

  public TableListAdapter(@NonNull DiffUtil.ItemCallback<ConflictingTable> diffCallback,
                          @NonNull OnClickListenerHolder<ConflictingTable> onClickListener) {
    super(diffCallback);

    this.onClickListener = onClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resolve_table, parent, false),
        onClickListener
    );
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bindTo(getItem(position));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ViewHolder.class.getSimpleName();

    private final OnClickListenerHolder<ConflictingTable> onClickListener;

    public ViewHolder(View itemView,
                      @NonNull OnClickListenerHolder<ConflictingTable> onClickListener) {
      super(itemView);

      this.onClickListener = onClickListener;
    }

    public void bindTo(final ConflictingTable item) {
      ViewCompat
          .<TextView>requireViewById(itemView, R.id.resolve_table_id)
          .setText(item.getId());

      ViewCompat
          .<TextView>requireViewById(itemView, R.id.resolve_table_conflicts)
          .setText(itemView.getContext().getString(
              R.string.resolve_table_list_conflicts,
              item.getConflicts()
          ));

      itemView.setOnClickListener(onClickListener.getListener(item));
    }
  }
}
