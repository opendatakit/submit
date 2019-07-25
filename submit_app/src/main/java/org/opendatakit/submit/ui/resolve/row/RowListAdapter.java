package org.opendatakit.submit.ui.resolve.row;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendatakit.submit.R;
import org.opendatakit.submit.ui.common.OnClickListenerHolder;

public class RowListAdapter extends ListAdapter<ConflictingRow, RowListAdapter.ViewHolder> {
  private static final String TAG = RowListAdapter.class.getSimpleName();

  private final OnClickListenerHolder<ConflictingRow> onClickListener;

  public static final DiffUtil.ItemCallback<ConflictingRow> DEFAULT_DIFF_CALLBACK =
      new DiffUtil.ItemCallback<ConflictingRow>() {
        @Override
        public boolean areItemsTheSame(ConflictingRow oldItem, ConflictingRow newItem) {
          return oldItem.getPeerId().equals(newItem.getPeerId());
        }

        @Override
        public boolean areContentsTheSame(ConflictingRow oldItem, ConflictingRow newItem) {
          return oldItem.equals(newItem);
        }
      };

  public RowListAdapter(@NonNull OnClickListenerHolder<ConflictingRow> onClickListener) {
    this(DEFAULT_DIFF_CALLBACK, onClickListener);
  }

  public RowListAdapter(@NonNull DiffUtil.ItemCallback<ConflictingRow> diffCallback,
                        @NonNull OnClickListenerHolder<ConflictingRow> onClickListener) {
    super(diffCallback);

    this.onClickListener = onClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resolve_row, parent, false),
        onClickListener
    );
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bindTo(getItem(position));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final OnClickListenerHolder<ConflictingRow> onClickListener;

    public ViewHolder(View itemView, @NonNull OnClickListenerHolder<ConflictingRow> onClickListener) {
      super(itemView);

      this.onClickListener = onClickListener;
    }

    public void bindTo(final ConflictingRow row) {
      ViewCompat
          .<TextView>requireViewById(itemView, R.id.resolve_row_id)
          .setText(row.getPeerId());

      itemView.setOnClickListener(onClickListener.getListener(row));
    }
  }
}
