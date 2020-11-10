package com.example.WiFiDirectandBluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> uniqueWifiP2pDevices = new ArrayList<String>();
    ArrayList<String> uniqueBluetoothDevices = new ArrayList<String>();

    ArrayList<String> uniqueBluetoothDevicesNow = new ArrayList<String>();

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    WiFiDirectBroadcastReceiver wifiReceiver;

    BluetoothAdapter bluetoothAdapter;
    BluetoothBroadcastReceiver bluetoothReceiver;

    IntentFilter wifiFilter;
    IntentFilter bluetoothFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*----------------------------------------------Start WiFiDirect----------------------------------------------------*/

        wifiFilter = new IntentFilter();
        //Register intent action to know when Wi-Fi p2p has changed(from enabled to disabled or vice versa)
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //Register intent action to know when available peer list has changed.
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        Button detectPeersWifi = findViewById(R.id.detectPeersWifi);

        //When user clicks the button "Detect Peers"
        detectPeersWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Android forces you to verify that the user granted the permission
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Please grant all permissions manually, go to Settings->Apps & notifications->Wi-fi Direct->Permissions->Denied->Allow only while using the app", Toast.LENGTH_LONG).show();
                    return;
                }

                //Create a WifiManager object to know if user has the wifi enabled
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                //If user has wifi enabled
                if (wifiManager.isWifiEnabled()) {
                    //Initiate peers discovery
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                        //If discovery initiation was success
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_LONG).show();
                        }

                        //If discovery initiation was failure
                        @Override
                        public void onFailure(int i) {
                            String errorType = null;
                            /*
                            These error messages are the same that are in:
                            https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.ActionListener#onFailure(int)
                             */
                            if (i == 0) {
                                errorType = "The operation failed due to an internal error.";
                            } else if (i == 1) {
                                errorType = "The operation failed because p2p is unsupported on the device.";
                            } else if (i == 2) {
                                errorType = "The operation failed because the framework is busy and unable to service the request";
                            }
                            Toast.makeText(getApplicationContext(), errorType, Toast.LENGTH_LONG).show();
                        }
                    });
                //If user has wifi disabled
                } else {
                    Toast.makeText(getApplicationContext(), "Enable wi-fi", Toast.LENGTH_LONG).show();
                }
            }
        });

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        wifiReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        //This callback is executed when wifi has detected devices in the WifiDirectBroadcastReceiver object.
        wifiReceiver.setOnPeerListListener(new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                TextView deviceAddressesNowWifi = findViewById(R.id.deviceAddressesNowWifi);
                TextView numberDevicesNowWifi = findViewById(R.id.numberDevicesNowWifi);
                TextView totalNumberDevicesWifi = findViewById(R.id.totalNumberDevicesWifi);

                deviceAddressesNowWifi.setText("");
                ArrayList<WifiP2pDevice> devicesFoundedNow = new ArrayList<WifiP2pDevice>(wifiP2pDeviceList.getDeviceList());
                Iterator iterator = devicesFoundedNow.iterator();
                int devicesNow = 0;
                while (iterator.hasNext()) {
                    WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
                    //Primary device type tell us what kind of device is (computer, printer, scanner or cellphone)
                    String primaryDeviceType = wifiP2pDevice.primaryDeviceType;
                    //The 10 before the hypen refers to a telephone in the regex pattern.
                    Pattern pattern = Pattern.compile("10-?0050F204");
                    Matcher matcher = pattern.matcher(primaryDeviceType);
                    boolean isTelephone = matcher.lookingAt();

                    //If my list of unique wifi devices doesn't contain the wifi address and is a wifi address of a telephone
                    if (!uniqueWifiP2pDevices.contains(wifiP2pDevice.deviceAddress)
                            && isTelephone) {
                        uniqueWifiP2pDevices.add(wifiP2pDevice.deviceAddress);
                    }

                    //If this wifi address in right now is a telephone
                    if (isTelephone) {
                        deviceAddressesNowWifi.append(wifiP2pDevice.deviceName + "\n");
                        devicesNow++;
                    }
                }

                numberDevicesNowWifi.setText(devicesNow + "");
                totalNumberDevicesWifi.setText(uniqueWifiP2pDevices.size() + "");
            }
        });

        /*----------------------------------------------Start bluetooth----------------------------------------------------*/

        final TextView deviceAddressesNowBluetooth = findViewById(R.id.deviceAddressesNowBluetooth);

        bluetoothFilter = new IntentFilter();
        //Register intent action to know when bluetooth has changed(from enabled to disabled or vice versa)
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //Register intent action to know when there is a new available device.
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //Register intent action to know when search for bluetooth devices has finished(after 12 seconds).
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        Button detectPeersBluetooth = findViewById(R.id.detectPeersBluetooth);

        //When user clicks the button "Detect Peers"
        detectPeersBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Android forces you to verify that the user granted the permission
                if (ActivityCompat.checkSelfPermission(view.getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Please grant all permissions manually, go to Settings->Apps & notifications->Wi-fi Direct->Permissions->Denied->Allow only while using the app", Toast.LENGTH_LONG).show();
                    return;
                }

                //If cellphone does not have bluetooth
                if (bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "The device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
                //If user has bluetooth enabled
                } else if (bluetoothAdapter.isEnabled()) {
                    boolean successfulDetection = bluetoothAdapter.startDiscovery();
                    //If discovery initiation was success
                    if (successfulDetection) {
                        Toast.makeText(getApplicationContext(), "Searching for 12 seconds...", Toast.LENGTH_LONG).show();
                        deviceAddressesNowBluetooth.setText("");
                        uniqueBluetoothDevicesNow.clear();
                    //If discovery initiation was failure
                    } else {
                        Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
                    }
                //If user has bluetooth disabled
                } else {
                    Toast.makeText(getApplicationContext(), "Enable bluetooth", Toast.LENGTH_LONG).show();
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothReceiver = new BluetoothBroadcastReceiver(bluetoothAdapter, this);

        //This callback is executed when bluetooth has detected a new device in the BluetoothBroadcastReceiver object.
        bluetoothReceiver.setOnPeerListener(new BluetoothBroadcastReceiver.PeerListener() {
            @Override
            public void onDeviceFound(BluetoothDevice bluetoothDevice) {
                TextView numberDevicesNowBluetooth = findViewById(R.id.numberDevicesNowBluetooth);
                TextView totalNumberDevicesBluetooth = findViewById(R.id.totalNumberDevicesBluetooth);

                // Get bluetooth address
                String deviceHardwareAddress = bluetoothDevice.getAddress();

                //If my list of unique bluetooth devices of the last 12 seconds doesn't contain the bluetooth address
                if (!uniqueBluetoothDevicesNow.contains(deviceHardwareAddress)) {
                    uniqueBluetoothDevicesNow.add(deviceHardwareAddress);
                    deviceAddressesNowBluetooth.append(deviceHardwareAddress + "\n");
                }

                //If my list of unique bluetooth devices doesn't contain the bluetooth address
                if (!uniqueBluetoothDevices.contains(deviceHardwareAddress)) {
                    uniqueBluetoothDevices.add(deviceHardwareAddress);
                }

                numberDevicesNowBluetooth.setText(uniqueBluetoothDevicesNow.size() + "");
                totalNumberDevicesBluetooth.setText(uniqueBluetoothDevices.size() + "");
                }
            }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiReceiver, wifiFilter);
        registerReceiver(bluetoothReceiver, bluetoothFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(bluetoothReceiver);
    }
}
