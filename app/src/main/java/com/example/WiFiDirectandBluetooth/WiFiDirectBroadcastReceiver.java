package com.example.WiFiDirectandBluetooth;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    private WifiP2pManager.PeerListListener myPeerListListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //If user has activated or deactivated wifi
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            //If Wifi P2P is enabled
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(activity, "Wi-fi is on", Toast.LENGTH_SHORT).show();
            //If Wi-Fi P2P is disabled
            } else {
                Toast.makeText(activity, "Wi-fi is off", Toast.LENGTH_SHORT).show();
            }
        //If available peer list has changed
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                //Android forces you to verify that the user granted the permission
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                //The interface myPeerListListener contains the callback and this callback is implemented in MainActivity
                manager.requestPeers(channel, myPeerListListener);
            }
        }
    }

    //This is an instance method to implement the interface in MainActivity
    public void setOnPeerListListener(WifiP2pManager.PeerListListener peerListListener) {
        myPeerListListener = peerListListener;
    }
}
