package org.opendatakit.submit.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opendatakit.consts.IntentConsts;
import org.opendatakit.fragment.AlertNProgessMsgFragmentMger;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.submit.R;
import org.opendatakit.submit.service.AvailablePeerAdapter;
import org.opendatakit.submit.service.PeerAdapter;
import org.opendatakit.submit.service.WifiDirectBroadcastReceiver;

import org.opendatakit.submit.service.actions.SyncActions;
import org.opendatakit.submit.service.peer.PeerSyncServerService;
import org.opendatakit.submit.service.peer.server.PeerSyncServer;
import org.opendatakit.submit.util.PeerSyncUtil;
import org.opendatakit.submit.util.SubmitUtil;
import org.opendatakit.sync.service.IOdkSyncServiceInterface;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.SyncProgressEvent;
import org.opendatakit.sync.service.SyncProgressState;
import org.opendatakit.sync.service.SyncStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PeerTransferActivity extends SubmitBaseActivity {
  public static final int W_DIRECT_UNSUPPORTED = 10;

    private List<Socket> clientSocketsToClose;
    private List<ServerSocket> serverSocketsToClose;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private Map<String, String> androidIdToIp;
    public AvailablePeerAdapter availablePeerAdapter;
    private PeerAdapter connectedPeerAdapter;

    private PeerSyncServer server;

    public List<WifiP2pDevice> availablePeers = new ArrayList<>();
    private final Handler handler = new Handler();
    private AlertNProgessMsgFragmentMger msgManager;
    private SyncActions syncAction = SyncActions.IDLE;
    private Snackbar dbUnavailableSnackbar;
    public boolean databaseAvailable = false;

  // this is used for joining table stuff
    private static final String SUFFIX = "_o";
    // tag for logging
    private static final String TAG = "PeerTransferActivity";
    private String alertDialogTag = TAG + "AlertDialog";
    private String progressDialogTag = TAG + "ProgressDialog";

    IOdkSyncServiceInterface syncInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // basic setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_transfer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final PeerTransferActivity activity = this;

        dbUnavailableSnackbar = Snackbar.make(
            findViewById(R.id.peer_transfer_content),
            R.string.database_unavailable,
            Snackbar.LENGTH_INDEFINITE
        );

        androidIdToIp = new TreeMap<>();
        clientSocketsToClose = new ArrayList<>();
        serverSocketsToClose = new ArrayList<>();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        if (mManager == null) {
          Log.e(TAG, "onCreate: Wi-Fi Direct unsupported");
          setResult(W_DIRECT_UNSUPPORTED);
          finish();
          return;
        }

        mChannel = mManager.initialize(activity, getMainLooper(), null);

        connectedPeerAdapter = new PeerAdapter(androidIdToIp, activity);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, activity);
        registerReceiver(mReceiver, mIntentFilter);

        // setup button
        final Button findPeersButton = findViewById(R.id.find_peers);
        findPeersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                LocalBroadcastManager.getInstance(PeerTransferActivity.this).registerReceiver(mReceiver, mIntentFilter);

              mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, activity);
              registerReceiver(mReceiver, mIntentFilter);

                if (mManager != null) {
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // request available peers from the wifi p2p manager. This is an
                            // asynchronous call and the calling activity is notified with a
                            // callback on PeerListListener.onPeersAvailable()
                            Log.i(TAG, "discover peers success");
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Log.i(TAG, "discover peers failure - reasonCode: " + reasonCode);
                        }
                    });
                }

            }
        });

        final Button syncCompleteButton = findViewById(R.id.sync_complete);
        syncCompleteButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            Intent i = new Intent(activity, MainActivity.class);
            i.putExtra(MainActivity.FRAGMENT_TO_NAV_LABEL, MainActivity.TABLE_LIST_FRAGMENT_TO_NAV);
            i.putExtra(MainActivity.FINAL_COPY_LABEL, true);
            i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        }});

        // setup recycler view
        availablePeerAdapter = new AvailablePeerAdapter(availablePeers, activity);
        RecyclerView availablePeerListRv = findViewById(R.id.available_peer_list);
        availablePeerListRv.setLayoutManager(new LinearLayoutManager(activity));
        availablePeerListRv.setAdapter(availablePeerAdapter);
        availablePeerAdapter.notifyDataSetChanged();

        RecyclerView connectedPeerListRv = findViewById(R.id.connected_peer_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        connectedPeerListRv.setLayoutManager(layoutManager);
        connectedPeerListRv.setAdapter(connectedPeerAdapter);
        connectedPeerAdapter.notifyDataSetChanged();

        bindToPeerSyncServerService();

        // display device id
        TextView yourId = findViewById(R.id.your_id);
        yourId.setText(PeerSyncUtil.getAndroidId(activity));

        if (savedInstanceState != null) {
            msgManager = AlertNProgessMsgFragmentMger
                    .restoreInitMessaging(SubmitUtil.getSecondaryAppName(getAppName()), alertDialogTag, progressDialogTag,
                            savedInstanceState);
        }

        // if message manager was not created from saved state, create fresh
        if (msgManager == null) {
            msgManager = new AlertNProgessMsgFragmentMger(SubmitUtil.getSecondaryAppName(getAppName()), alertDialogTag,
                    progressDialogTag, false, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (msgManager != null) {
            msgManager.addStateToSaveStateBundle(outState);
        }
    }

    public void bindToSyncService(String androidId) {
        Log.i(TAG, "bindToSyncService: " + androidId);

        if (androidId == null) {
            return;
        }

        final String secondaryAppName = SubmitUtil.getSecondaryAppName(getAppName());
        PropertiesSingleton propertiesSingleton = CommonToolProperties.get(this, secondaryAppName);
        propertiesSingleton.setProperties(Collections.singletonMap(
                CommonToolProperties.KEY_SYNC_SERVER_URL,
                IntentConsts.SubmitPeerSync.URI_SCHEME + androidIdToIp.get(androidId) + ":8080/" + secondaryAppName
        ));
        propertiesSingleton.setProperties(Collections.singletonMap(
                CommonToolProperties.KEY_FIRST_LAUNCH,
                Boolean.toString(false)
        ));

        Intent intent = new Intent()
                .setClassName(IntentConsts.Sync.APPLICATION_NAME, IntentConsts.Sync.SYNC_SERVICE_CLASS);
        syncAction = SyncActions.START_SYNC;

        this.bindService(intent, new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            syncInterface = IOdkSyncServiceInterface.Stub.asInterface(service);
            try {
              fixSubmitStates();
              syncInterface.synchronizeWithServer(secondaryAppName, SyncAttachmentState.NONE);
              syncAction = SyncActions.START_SYNC;

              handler.post(new Runnable() {
                @Override
                public void run() {
                  // TODO: Put strings in xml
                  msgManager.createProgressDialog("Transferring", "Initiating data transfer", getSupportFragmentManager());
                }
              });

              monitorProgress();
            } catch (RemoteException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "services disconnected");
          }
        }, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    public void bindToPeerSyncServerService() {
      Intent peerServiceIntent = new Intent()
          .setClass(this, PeerSyncServerService.class);

      bindService(peerServiceIntent, new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          Log.e(TAG, "onServiceConnected: connected to PeerSyncServer");

          server = ((PeerSyncServerService.PeerSyncServerBinder) service).getServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          server = null;
        }
      }, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    public void startPeerSyncServer() {
      if (server == null) {
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            startPeerSyncServer();
          }
        }, 500);

        return;
      }

      if (server.isAlive()) {
        return;
      }

      server.stop();
      try {
        server.start();
      } catch (IOException e) {
        Log.e(TAG, "startPeerSyncServer: ", e);

        Snackbar
            .make(
                findViewById(R.id.peer_transfer_content),
                "Unable to start service",
                Snackbar.LENGTH_LONG
            )
            .show();
      }
    }

    /**
     * Lets the user know we are connected to a peer and waiting for
     * them to initiate and complete sync
     */
    public void displayWaitingForPeerMessage() {
        TextView isWaiting = findViewById(R.id.is_waiting);
        isWaiting.setText("Connected to peer .. waiting");
    }

    /**
     * Updates list of peers with given androidId and peerAddress
     *
     * @param androidId
     * @param peerAddress
     */
    public void peerAddressCallback(String androidId, InetAddress peerAddress) {

        Log.i(TAG, "peer androidId: " + androidId);
        Log.i(TAG, "peer Address: " + peerAddress.getHostAddress());

        androidIdToIp.put(androidId, peerAddress.getHostAddress());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedPeerAdapter.notifyDataSetChanged();
            }
        });

    }

  private void monitorProgress() {
    final Activity activity = this;

    if (!msgManager.hasDialogBeenCreated()) {
      // We are in transition. Wait and try again
      handler.postDelayed(new Runnable() {@Override public void run() { monitorProgress(); }}, 100);
      return;
    }

    if (syncInterface == null || syncAction == SyncActions.IDLE) {
      resetDialogAndStateMachine();
      return;
    }

    final SyncStatus status;
    final SyncProgressEvent event;
    try {
      status = syncInterface.getSyncStatus(SubmitUtil.getSecondaryAppName(getAppName()));
      event = syncInterface.getSyncProgressEvent(SubmitUtil.getSecondaryAppName(getAppName()));
    } catch (RemoteException e) {
      // TODO: How can we handle this error? Can we recover?
      WebLogger.getLogger(SubmitUtil.getSecondaryAppName(getAppName())).d(TAG, "[monitorProgress] Remote exception");
      e.printStackTrace();
      resetDialogAndStateMachine();
      return;
    }

    if (syncAction == SyncActions.START_SYNC && status == SyncStatus.NONE) {
      handler.postDelayed(new Runnable() {@Override public void run() { monitorProgress(); }}, 100);
      return;
    } else if (status == SyncStatus.SYNCING) {
      syncAction = SyncActions.MONITOR_SYNCING;

      SyncProgressState progress = (event.progressState != null) ? event.progressState : SyncProgressState.INACTIVE;
      String message;
      switch (progress) {
        case APP_FILES:
          message = "Syncing app files";
          break;
        case TABLE_FILES:
          message = "Syncing table files";
          break;
        case ROWS:
          message = "Syncing row data";
          break;
        default:
          message = "Syncing files";
      }

      FragmentManager fm =  getSupportFragmentManager();
      msgManager.createProgressDialog("Syncing", message, fm);
      fm.executePendingTransactions();
      msgManager.updateProgressDialogMessage(message, event.curProgressBar, event.maxProgressBar, fm);

      handler.postDelayed(new Runnable() {@Override public void run() { monitorProgress(); }}, 100);
    } else if (syncAction == SyncActions.MONITOR_SYNCING && (status == SyncStatus.SYNC_COMPLETE || status == SyncStatus.NONE)) {
      FragmentManager fm =  getSupportFragmentManager();
      msgManager.clearDialogsAndRetainCurrentState(fm);
      fm.executePendingTransactions();

      try {
        syncInterface.clearAppSynchronizer(SubmitUtil.getSecondaryAppName(getAppName()));
      } catch (RemoteException e) {
        // TODO: How can we handle this error? Can we recover?
        WebLogger.getLogger(SubmitUtil.getSecondaryAppName(getAppName())).d(TAG, "[monitorProgress] Remote exception");
        e.printStackTrace();
        resetDialogAndStateMachine();
        new AlertDialog.Builder(activity)
            .setTitle("Sync Error")
            .setMessage("An error occurred with the sync")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) { /* Do nothing */ }
            });
        return;
      }
      syncAction = SyncActions.IDLE;

      new AlertDialog.Builder(activity)
          .setTitle("Sync Complete")
          .setMessage("The files have been successfully synced")
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Intent i = new Intent(activity, MainActivity.class);
              i.putExtra(MainActivity.FRAGMENT_TO_NAV_LABEL, MainActivity.TABLE_LIST_FRAGMENT_TO_NAV);
              i.putExtra(MainActivity.FINAL_COPY_LABEL, true);
              i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
              startActivity(i);
            }});

    } else if (syncAction == SyncActions.MONITOR_SYNCING && (status == SyncStatus.AUTHENTICATION_ERROR || status == SyncStatus.DEVICE_ERROR ||
        status == SyncStatus.APPNAME_NOT_SUPPORTED_BY_SERVER || status == SyncStatus.NETWORK_TRANSPORT_ERROR
        || status == SyncStatus.REQUEST_OR_PROTOCOL_ERROR || status == SyncStatus.RESYNC_BECAUSE_CONFIG_HAS_BEEN_RESET_ERROR
        || status == SyncStatus.SERVER_INTERNAL_ERROR || status == SyncStatus.SERVER_IS_NOT_ODK_SERVER ||
        status == SyncStatus.SERVER_MISSING_CONFIG_FILES )) {

      FragmentManager fm =  getSupportFragmentManager();
      msgManager.clearDialogsAndRetainCurrentState(fm);
      fm.executePendingTransactions();

      try {
        syncInterface.clearAppSynchronizer(SubmitUtil.getSecondaryAppName(getAppName()));
      } catch (RemoteException e) {
        // TODO: How can we handle this error? Can we recover?
        WebLogger.getLogger(SubmitUtil.getSecondaryAppName(getAppName())).d(TAG, "[monitorProgress] Remote exception");
        e.printStackTrace();
        resetDialogAndStateMachine();
        new AlertDialog.Builder(activity)
            .setTitle("Sync Error")
            .setMessage("An error occurred with the sync")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) { /* Do nothing */ }
            });
        return;
      }
      syncAction = SyncActions.IDLE;
    }

  }

  private void resetDialogAndStateMachine() {
    msgManager.dismissAlertDialog(getSupportFragmentManager());
    syncAction = SyncActions.IDLE;
  }

  /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        msgManager.clearDialogsAndRetainCurrentState(getSupportFragmentManager());
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            // ignore
            // somehow not registered
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        WebLogger.getLogger(SubmitUtil.getSecondaryAppName(getAppName())).i(TAG, "[onResume]");

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                monitorProgress();
            }
        }, 100);
    }

  @Override
  protected void onPostResume() {
    super.onPostResume();

    if (getDatabase() == null) {
      databaseAvailable = false;
      dbUnavailableSnackbar.show();
    }
  }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        WebLogger.getLogger(SubmitUtil.getSecondaryAppName(getAppName())).i(TAG, "[onDestroy]");
    }

    /**
     * register a client and server socket to close when the activity stops
     *
     * @param serverSocket
     * @param clientSocket
     */
    public void closeSocketsOnExit(ServerSocket serverSocket, Socket clientSocket) {
        clientSocketsToClose.add(clientSocket);
        serverSocketsToClose.add(serverSocket);
    }

    /**
     * when activity stops, close all sockets
     */
    public void onStop() {
        for (Socket socket : clientSocketsToClose) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (ServerSocket socket : serverSocketsToClose) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
      super.onStop();
    }

    public void connectDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "connected device success");
                //success logic
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "connected device failure");
                //failure logic
            }
        });
    }

  @Override
  public void databaseAvailable() {
    databaseAvailable = true;
    if (dbUnavailableSnackbar != null) {
      dbUnavailableSnackbar.dismiss();
    }
  }

  @Override
  public void databaseUnavailable() {
    databaseAvailable = false;
    if (dbUnavailableSnackbar != null) {
      dbUnavailableSnackbar.show();
    }
  }
}