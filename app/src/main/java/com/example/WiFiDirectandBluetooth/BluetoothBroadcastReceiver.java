package com.example.WiFiDirectandBluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private BluetoothAdapter adapter;
    private MainActivity activity;

    PeerListener peerListener;

    public BluetoothBroadcastReceiver(BluetoothAdapter adapter, MainActivity activity) {
        this.adapter = adapter;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //If user has activated or deactivated bluetooth
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            //If Bluetooth is enabled
            if (state == BluetoothAdapter.STATE_ON) {
                Toast.makeText(activity, "Bluetooth is on", Toast.LENGTH_SHORT).show();
            //If Bluetooth is disabled
            } else {
                Toast.makeText(activity, "Bluetooth is off", Toast.LENGTH_SHORT).show();
            }
        //If a new device is found
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //onDeviceFound is a callback that is implemented in MainActivity
            peerListener.onDeviceFound(device);
        //If bluetooth search has finished
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Toast.makeText(activity, "Search for bluetooth devices is off", Toast.LENGTH_LONG).show();
        }
    }

    public void setOnPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    public interface PeerListener {
        void onDeviceFound(BluetoothDevice bluetoothDevice);
    }
}