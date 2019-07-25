package org.opendatakit.submit.service;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opendatakit.submit.R;
import org.opendatakit.submit.activities.PeerTransferActivity;

import java.util.Map;

public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.ViewHolder> {
    private static final String TAG = "PeerAdapter";

    private Map<String, String> androidIdToIp;
    private PeerTransferActivity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public RelativeLayout mRelativeLayout;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.peer_name);
            mRelativeLayout = v.findViewById(R.id.recycler_relative_layout);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PeerAdapter(Map<String, String> androidIdToIp, PeerTransferActivity activity) {
        this.androidIdToIp = androidIdToIp;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PeerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_peer_list, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.i(TAG, "bind");
        final String device = androidIdToIp.keySet().toArray(new String[0])[position];
        holder.mTextView.setText(device);
        //holder.mTextView.setText("hey look!!!");

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (activity.databaseAvailable) {
                    activity.bindToSyncService(device);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return androidIdToIp.keySet().size();
    }
}