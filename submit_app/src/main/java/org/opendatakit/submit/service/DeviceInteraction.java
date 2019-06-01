package org.opendatakit.submit.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.opendatakit.submit.activities.PeerTransferActivity;
import org.opendatakit.submit.util.PeerSyncUtil;

public class DeviceInteraction implements ConnectionInfoListener {
    public static final int DEVICE_HANDSHAKE_PORT = 8988;

    private static final String TAG = "DeviceInteraction";
    private Context context;
    private Activity mActivity;
    //private ConnectionSetupTimer connectionSetupTimer;

    public DeviceInteraction(Context context, Activity activity) {
        this.context = context;
        this.mActivity = activity;
        //this.connectionSetupTimer = connectionSetupTimer;
    }

    // If two devices are connected it will create sockets and send files based off who is sender
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        InetAddress groupOwnerAddress = info.groupOwnerAddress;
        Log.i(TAG, "onConnectionInfo");
        if (info.groupFormed && info.isGroupOwner) {
            Log.i(TAG, "group Beginning");

            final GroupOwnerServer serverSocket = new GroupOwnerServer(context, true, null, mActivity);
            //connectionSetupTimer.setEnd();
            serverSocket.execute();

        } else if (info.groupFormed) {
            //todo: this spamming gets it to work + crash
            Log.i(TAG, "Sending Beginning");
            final ClientSocket clientSocket = new ClientSocket(groupOwnerAddress, context, true, null, mActivity);
            //connectionSetupTimer.setEnd();
            clientSocket.execute();

            ((PeerTransferActivity) mActivity).startPeerSyncServer();
        }
    }

    public static class SocketAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Context context;
        protected boolean isSender;
        private byte[] bytes;
        private Activity mActivity;

        public SocketAsyncTask(Context context, boolean isSender, byte[] bytes, Activity activity) {
            this.context = context;
            this.isSender = isSender;
            this.bytes = bytes;
            this.mActivity = activity;
        }

        protected Void doInBackground(Void... params) { return null; }

        protected void send(Socket socket) throws IOException {
            OutputStream s = null;

            try {
                s = socket.getOutputStream();
                s.write(PeerSyncUtil.getAndroidId(context).getBytes("UTF-8"));
            } finally {
                if (s != null) {
                    s.close();
                }
            }

        }

        protected void receive(Socket socket) throws IOException {
            // PASS TO SERVICE
            Log.i(TAG, "receive: ");
            String senderAndroidId = IOUtils.toString(socket.getInputStream(), "UTF-8");
            ((PeerTransferActivity) mActivity).peerAddressCallback(senderAndroidId, socket.getInetAddress());
        }

    }

    // AsyncTask for device that is not group owner
    public class ClientSocket extends SocketAsyncTask {

        private InetAddress groupOwnerAddress;

        public ClientSocket(InetAddress groupOwnerAddress, Context context, boolean isSender, byte[] bytes, Activity activity) {
            super(context, isSender, bytes, activity);
            this.groupOwnerAddress = groupOwnerAddress;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "Sender: doInBackground " + groupOwnerAddress.getHostAddress());
            try {
                // display message that we're waiting in activity
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((PeerTransferActivity)mActivity).displayWaitingForPeerMessage();
                    }
                });

                Socket socket = new Socket();
                socket.bind(null);
                InetSocketAddress groupOwnerSocketAddress = new InetSocketAddress(
                        groupOwnerAddress, DEVICE_HANDSHAKE_PORT);
                socket.connect(groupOwnerSocketAddress);
                send(socket);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // AsyncTask for group owner
    public class GroupOwnerServer extends SocketAsyncTask {

        public GroupOwnerServer(Context context, boolean isSender, byte[] bytes, Activity activity) {
            super(context, isSender, bytes, activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(DEVICE_HANDSHAKE_PORT);
                Socket client = serverSocket.accept();
//                serverSocket.close();
                receive(client);
                ((PeerTransferActivity)mActivity).closeSocketsOnExit(serverSocket, client);
//                client.close();
//                if (isSender) {
//                    send(client);
//                } else {
//                    receive(client);
//                    client.close();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
      }
}