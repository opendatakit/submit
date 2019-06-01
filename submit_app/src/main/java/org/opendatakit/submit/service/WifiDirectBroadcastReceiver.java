package org.opendatakit.submit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.opendatakit.submit.activities.PeerTransferActivity;
import org.opendatakit.submit.util.PeerSyncUtil;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "WifiDirectBroadcastReceiver";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private PeerTransferActivity activity;



    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       PeerTransferActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // checks p2p is enabled

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.i(TAG, "p2p enabled");
            } else {
                Log.i(TAG, "p2p not enabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                @Override
                public void onPeersAvailable(WifiP2pDeviceList peerList) {
                    Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                    Log.i(TAG, "peers available");
                    Log.i(TAG, refreshedPeers.toString());

                    List<WifiP2pDevice> connectedPeers = new ArrayList<>();
                    List<WifiP2pDevice> availablePeers = new ArrayList<>();

                    Iterator<WifiP2pDevice> i = refreshedPeers.iterator();
                    while(i.hasNext()) {
                      WifiP2pDevice device = i.next();
                      if (device.status == WifiP2pDevice.CONNECTED) {
                        connectedPeers.add(device);
                      } else if (device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.INVITED) {
                        availablePeers.add(device);
                      }
                    }

                    // TODO: Can I use connected peers? 

                    if (!availablePeers.equals(activity.availablePeers)) {
                        activity.availablePeers.clear();
                        activity.availablePeers.addAll(refreshedPeers);
                        activity.availablePeerAdapter.notifyDataSetChanged();

                        // If an AdapterView is backed by this data, notify it
                        // of the change. For instance, if you have a ListView of
                        // available peers, trigger an update.
                        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                        // Perform any other updates needed based on the new list of
                        // peers connected to the Wi-Fi P2P network.
                    }

                    if (activity.availablePeers.size() == 0) {
                        Log.d(TAG, "No devices found");
                        return;
                    }
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // connects to device

            Log.i(TAG, "connection changed");

            if (manager == null) {
                Log.e(TAG, "Cannot connect because manager is null");
                return;
            }

            // TODO: these can get called too often
            WifiP2pManager.ConnectionInfoListener connectionListener =
                    new DeviceInteraction(context, activity);
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                manager.requestConnectionInfo(channel, connectionListener);
                Log.i(TAG, "onReceive: " + networkInfo);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "wifi state changed");
        }
    }
}
