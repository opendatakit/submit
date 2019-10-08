package org.opendatakit.submit.ui.finalize;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendatakit.consts.IntentConsts;
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.submit.R;
import org.opendatakit.submit.activities.SubmitBaseActivity;
import org.opendatakit.submit.consts.SubmitColumns;
import org.opendatakit.submit.consts.SubmitSyncStates;
import org.opendatakit.submit.service.peer.PeerSyncServerService;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;
import org.opendatakit.submit.ui.common.AbsBaseFragment;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.sync.service.IOdkSyncServiceInterface;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.SyncStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FinalizeFragment extends AbsBaseFragment {
  private static final String TAG = FinalizeFragment.class.getSimpleName();

  private static final String CONFLICT_COUNT_COL = "count";

  private static final String COUNT_CONFLICT_QUERY =
      "SELECT " +
          "COUNT(DISTINCT " + SubmitColumns.P_ID + ") AS '" + CONFLICT_COUNT_COL + "' " +
          "FROM %s WHERE " + SubmitColumns.P_STATE + " IN (?, ?)";

  private FinalizeViewModel statusViewModel;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    statusViewModel = ViewModelProviders
        .of(this)
        .get(FinalizeViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_finalize, container, false);
  }

  @Override
  public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    statusViewModel.getStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        ((TextView) view.findViewById(R.id.textView3)).setText(s);
      }
    });

    view.findViewById(R.id.green_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SubmitBaseActivity activity = (SubmitBaseActivity) requireActivity();
        final String appName = SubmitUtil.getSecondaryAppName(getAppName());

        // TODO: move PropertiesSingleton init
        PropertiesSingleton propertiesSingleton = CommonToolProperties.get(activity, appName);
        propertiesSingleton.setProperties(Collections.singletonMap(
            CommonToolProperties.KEY_SYNC_SERVER_URL,
            "submit://test_url"
        ));
        propertiesSingleton.setProperties(Collections.singletonMap(
            CommonToolProperties.KEY_FIRST_LAUNCH,
            Boolean.toString(false)
        ));

        Intent intent = new Intent()
            .setClassName(IntentConsts.Sync.APPLICATION_NAME, IntentConsts.Sync.SYNC_SERVICE_CLASS);

        requireContext().bindService(intent, new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            IOdkSyncServiceInterface syncInterface = IOdkSyncServiceInterface.Stub.asInterface(service);

            try {
              SyncStatus status = syncInterface.getSyncStatus(appName);

              statusViewModel.setStatus(status.toString());
            } catch (RemoteException e) {
              e.printStackTrace();
            }

            try {
//              fixSubmitStates();
              syncInterface.synchronizeWithServer(appName, SyncAttachmentState.NONE);
            } catch (RemoteException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            // TODO:
          }
        }, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
      }
    });

//    view.findViewById(R.id.init_green_btn).setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        SubmitBaseActivity activity = (SubmitBaseActivity) requireActivity();
//        String appName = SubmitUtil.getSecondaryAppName(getAppName());
//
//        DbHandle handle = null;
//        try {
//          handle = getDatabase().openDatabase(appName);
//
//          List<String> tableIds = getDatabase().getAllTableIds(appName, handle);
//          for (String tableId : tableIds) {
//            // TODO: create a table with each of these ids
//          }
//        } catch (ServicesAvailabilityException e) {
//          throw new RuntimeException(e);
//        } finally {
//          if (handle != null) {
//            try {
//              getDatabase().closeDatabase(appName, handle);
//            } catch (ServicesAvailabilityException e) {
//              e.printStackTrace();
//            }
//          }
//        }
//      }
//    });

    view.findViewById(R.id.red_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent peerServiceIntent = new Intent()
            .setClass(requireContext(), PeerSyncServerService.class);

        requireContext().bindService(peerServiceIntent, new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: connected to PeerSyncServer");

            // TODO: manage server lifecycle better

            PeerSyncServer server =
                ((PeerSyncServerService.PeerSyncServerBinder) service).getServer();

            server.stop();
            try {
              server.start();
            } catch (IOException e) {
              Log.e(TAG, "onServiceConnected: ", e);
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {

          }
        }, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
      }
    });

    view.findViewById(R.id.init_red_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String appName = SubmitUtil.getSecondaryAppName(getAppName());

        Intent intent = new Intent()
            .setClassName(IntentConsts.Sync.APPLICATION_NAME, IntentConsts.Sync.SYNC_SERVICE_CLASS);

        requireContext().bindService(intent, new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            IOdkSyncServiceInterface syncInterface = IOdkSyncServiceInterface.Stub.asInterface(service);

            try {
//              fixSubmitStates();
              Log.e(TAG, "onServiceConnected: start sync with " + appName);
              syncInterface.synchronizeWithServer(appName, SyncAttachmentState.NONE);
            } catch (RemoteException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            // TODO:
          }
        }, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
      }
    });
  }

  private boolean hasSubmitConflicts() {
    String dbAppName = SubmitUtil.getSecondaryAppName(getAppName());
    DbHandle handle = null;

    try {
      handle = getDatabase().openDatabase(dbAppName);

      List<String> tableIds = getDatabase().getAllTableIds(dbAppName, handle);

      for (String tableId : tableIds) {
        BaseTable baseTable = getDatabase().arbitrarySqlQuery(
            dbAppName,
            handle,
            tableId,
            String.format(COUNT_CONFLICT_QUERY, tableId),
            new BindArgs(new Object[] {
                SubmitSyncStates.P_CONFLICT,
                SubmitSyncStates.P_DIVERGENT
            }),
            null,
            null
        );

        int count =
            Integer.parseInt(baseTable.getRowAtIndex(0).getRawStringByKey(CONFLICT_COUNT_COL));

        if (count > 0) {
          return true;
        }
      }

      return false;
    } catch (ServicesAvailabilityException e) {
      Log.e(TAG, "hasSubmitConflicts: ", e);
    } finally {
      if (handle != null) {
        try {
          getDatabase().closeDatabase(dbAppName, handle);
        } catch (ServicesAvailabilityException e) {
          Log.e(TAG, "hasSubmitConflicts: ", e);
        }
      }
    }

    // TODO: differentiate error and has conflict
    return true;
  }
}
