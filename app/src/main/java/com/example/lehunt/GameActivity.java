package com.example.lehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static final String JAALEE_BEACON_PROXIMITY_UUID = "EBEFD083-70A2-47C8-9837-E7B5634DF524";//Jaalee BEACON Default UUID
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private String TAG = "GameActivity";

    private MqttAndroidClient mqttClient;
    private BeaconManager beaconManager;

    private String topicPub, topicSub;
    private int REQUEST_ENABLE_BT = 1;
    private Beacon beacon;
    private boolean PubMessageSend = false;
    private LinearLayout hints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        checkPermission();

        Intent i = getIntent();
        Hunt curHunt = (Hunt)i.getExtras().getSerializable("HUNT");

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        hints = findViewById(R.id.ll_hints);

         // Configure BeaconManager.
         beaconManager = new BeaconManager(this);
         beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.

                        if (beacons.size() > 0){
                            beacon = (Beacon) beacons.get(0);
                            if ( beacon.getProximityUUID().equalsIgnoreCase(JAALEE_BEACON_PROXIMITY_UUID) ) {
                                if (beacon.getRssi() > -50) {
                                    // Log.i("JAALEE", "JAALEE:" + beacon.getBattLevel());
                                    if (!PubMessageSend) {
                                        try {
                                            mqttClient.publish(topicPub, new MqttMessage("new Hint".getBytes()));
                                            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            TextView tv = new TextView(getApplicationContext());
                                            tv.setLayoutParams(lparams);
                                            tv.setText("Message to mqtt send and new Hint is comming soon");
                                            hints.addView(tv);
                                        } catch (MqttException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    PubMessageSend = true;
                                }
                                Log.d("BEACON", "Name: " + beacon.getName());
                                Log.d("BEACON", "UUID: " + beacon.getProximityUUID());
                                Log.d("BEACON", "Major: " + beacon.getMajor());
                                Log.d("BEACON", "Minor: " + beacon.getMinor());
                            }
                            //Log.d("BEACON", beacon.getName() + "");
                        }
                        //List<Beacon> JaaleeBeacons = filterBeacons(beacons);


                    }
                });
            }
         });

         /*BLE device around the phone
         beaconManager.setDeviceDiscoverListener(new DeviceDiscoverListener() {
            @Override
            public void onBLEDeviceDiscovered(BLEDevice device) {
                // TODO Auto-generated method stub
                Log.i("JAALEE", "On ble device  discovery:" + device.getMacAddress());
                Log.i("JAALEE", device.toString());

            }
         });*/

         //bleScanner = new BLEScanner();
         //bleScanner.beginScanning(mqttClient);

         mqttClient = new MqttAndroidClient(getApplicationContext(), curHunt.getBrokerURL(), curHunt.getClientID());
         mqttClient.setCallback(new MqttCallbackExtended() {
             @Override
             public void connectComplete(boolean reconnect, String serverURI) {
                 if (reconnect) {
                     Log.d("MQTTCallback","Reconnected to : " + serverURI);
                     // Because Clean Session is true, we need to re-subscribe
                     //subscribeToTopic();
                 } else {
                     Log.d("MQTTCallback","Connected to : " + serverURI);
                 }
             }

             @Override
             public void connectionLost(Throwable throwable) {
                 Log.d("MQTTCallback","Connection was lost");
             }

             @Override
             public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.d("MQTTCallback", "Incomming Message: " + new String(mqttMessage.getPayload()));
             }

             @Override
             public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

             }
         });

         MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
         mqttConnectOptions.setAutomaticReconnect(true);
         mqttConnectOptions.setCleanSession(false);

         try {
             mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                 @Override
                 public void onSuccess(IMqttToken iMqttToken) {
                     DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                     disconnectedBufferOptions.setBufferEnabled(true);
                     disconnectedBufferOptions.setBufferSize(100);
                     disconnectedBufferOptions.setPersistBuffer(false);
                     disconnectedBufferOptions.setDeleteOldestMessages(false);
                     mqttClient.setBufferOpts(disconnectedBufferOptions);
                     subscribeToTopic();
                 }

                 @Override
                 public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                     Log.d("MQTTCALLBACK", "Failed to connect");
                 }
             });
         } catch (MqttException e) {
            e.printStackTrace();
         }

    }

     @Override
     protected void onStart(){
        super.onStart();

         // Check if device supports Bluetooth Low Energy.
         if (!beaconManager.hasBluetooth()) {
             Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
             return;
         }

         // If Bluetooth is not enabled, let user enable it.
         if (!beaconManager.isBluetoothEnabled()) {
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         } else {
             connectToService();
         }
     }

    private void connectToService() {
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    private List<Beacon> filterBeacons(List<Beacon> beacons) {
        List<Beacon> filteredBeacons = new ArrayList<Beacon>(beacons.size());
        for (Beacon beacon : beacons)
        {
//    	only detect the Beacon of Jaalee
    	if ( beacon.getProximityUUID().equalsIgnoreCase(JAALEE_BEACON_PROXIMITY_UUID) )
    	if (beacon.getRssi() > -50)
            {
                Log.i("JAALEE", "JAALEE:"+beacon.getBattLevel());
                filteredBeacons.add(beacon);
            }
        }
        return filteredBeacons;
    }

    private void subscribeToTopic(){
        try{
            mqttClient.subscribe(topicSub, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d("SUBSCRIPTION", "Subscribed successfull to topic: " + topicSub);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d("SUBSCRIPTION", "Subscribed failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission(){
        Log.d(TAG, "checkPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],  @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

    private void pubishMsgForNewHints(String beaconID, int qos){
        try {
            Log.d(TAG, "publish beacon ID to topic for new Hints");
            //huntMqttClient.publishMessage(mqttClient, beaconID, qos, topicPub);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy");

        Log.d("BM", "BeaconManager disconnected");
        beaconManager.disconnect();
        Log.d(TAG, "clear MQTT Service");
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        super.onDestroy();
        // TODO: 03.01.2020 clear MQTT Service and Broadcast Receiver for BLE
        //stopService(mqttService);
    }

}
