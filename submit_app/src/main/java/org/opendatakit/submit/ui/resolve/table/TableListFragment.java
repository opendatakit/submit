package org.opendatakit.submit.ui.resolve.table;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.opendatakit.consts.IntentConsts;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.submit.R;
import org.opendatakit.submit.activities.SubmitBaseActivity;
import org.opendatakit.submit.ui.common.AbsBaseFragment;
import org.opendatakit.submit.ui.common.OnClickListenerHolder;
import org.opendatakit.submit.ui.resolve.row.RowListFragment;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.sync.service.IOdkSyncServiceInterface;
import org.opendatakit.sync.service.SyncAttachmentState;

import java.util.Collections;
import java.util.List;

public class TableListFragment extends AbsBaseFragment
    implements OnClickListenerHolder<ConflictingTable>,
    View.OnClickListener,
    ServiceConnection {
  private static final String TAG = TableListFragment.class.getSimpleName();

  private TableListViewModel viewModel;
  private TableListAdapter adapter;

  private boolean isBond;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.viewModel = ViewModelProviders
        .of(this)
        .get(TableListViewModel.class);

    this.viewModel.setAppName(getAppName());

    this.adapter = new TableListAdapter(this);

    // TODO: could cause ANR
    ((SubmitBaseActivity) requireActivity()).fixSubmitStates();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_resolve_table_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    configRecyclerView(ViewCompat.<RecyclerView>requireViewById(view, R.id.resolve_table_list));

    viewModel.getTables().observe(getViewLifecycleOwner(), new Observer<List<ConflictingTable>>() {
      @Override
      public void onChanged(@Nullable List<ConflictingTable> conflictingTables) {
        adapter.submitList(conflictingTables);

        boolean tableEmpty = conflictingTables == null || conflictingTables.isEmpty();

        int recyclerViewVisibility = tableEmpty ? View.GONE : View.VISIBLE;
        int localSyncVisibility = tableEmpty ? View.VISIBLE : View.GONE;

        ViewCompat
            .requireViewById(view, R.id.resolve_table_list)
            .setVisibility(recyclerViewVisibility);

        ViewCompat
            .requireViewById(view, R.id.resolve_table_list_empty)
            .setVisibility(localSyncVisibility);

        Button localSyncBtn = ViewCompat
            .requireViewById(view, R.id.resolve_table_local_btn);

        localSyncBtn.setVisibility(localSyncVisibility);
        localSyncBtn.setOnClickListener(TableListFragment.this);
      }
    });

    viewModel.findTablesInConflict();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (isBond) {
      requireContext().unbindService(this);
    }
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
  public View.OnClickListener getListener(final ConflictingTable item) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requireFragmentManager()
            .beginTransaction()
            .replace(R.id.main_content, RowListFragment.newInstance(item.getId()))
            .addToBackStack(null)
            .commit();
      }
    };
  }

  @Override
  public void onClick(View v) {
    if (v.getId() != R.id.resolve_table_local_btn) {
      return;
    }

    final String appName = SubmitUtil.getSecondaryAppName(getAppName());

    // TODO: move PropertiesSingleton init
    PropertiesSingleton propertiesSingleton = CommonToolProperties.get(requireActivity(), appName);
    propertiesSingleton.setProperties(Collections.singletonMap(
        CommonToolProperties.KEY_SYNC_SERVER_URL,
        IntentConsts.SubmitLocalSync.URI_SCHEME + "sync"
    ));
    propertiesSingleton.setProperties(Collections.singletonMap(
        CommonToolProperties.KEY_FIRST_LAUNCH,
        Boolean.toString(false)
    ));

    Intent intent = new Intent()
        .setClassName(IntentConsts.Sync.APPLICATION_NAME, IntentConsts.Sync.SYNC_SERVICE_CLASS);

    isBond = requireContext().bindService(
        intent,
        this,
        Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY
    );
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    Log.i(TAG, "onServiceConnected: " + name);

    IOdkSyncServiceInterface syncInterface = IOdkSyncServiceInterface.Stub.asInterface(service);

    // TODO:
//        try {
//          SyncStatus status = syncInterface.getSyncStatus(appName);
//
//          statusViewModel.setStatus(status.toString());
//        } catch (RemoteException e) {
//          e.printStackTrace();
//        }

    try {
      ((SubmitBaseActivity) requireActivity()).fixSubmitStates();

      syncInterface.synchronizeWithServer(
          SubmitUtil.getSecondaryAppName(getAppName()),
          SyncAttachmentState.NONE
      );
    } catch (RemoteException e) {
      Log.e(TAG, "onServiceConnected: ", e);
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    Log.e(TAG, "onServiceDisconnected: " + name);
  }
}
