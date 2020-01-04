package com.example.lehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class GameActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private String TAG = "GameActivity";

    private MqttAndroidClient mqttClient;
    private BLEScanner bleScanner;

    private String topicPub, topicSub;
    private int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        checkPermission();

        Intent i = getIntent();
        Hunt curHunt = (Hunt)i.getExtras().getSerializable("HUNT");

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        // TODO: 27.12.2019 Start thread/service for checking all devices in the environment maybe BCReceiver is enough

        // TODO: 27.12.2019 subscribe the topic of the cliendid

        /** Check Bluetooth is enabled and the adapter is not null, Start it if one is true */
         if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         }

         bleScanner = new BLEScanner();
         bleScanner.beginScanning(mqttClient);

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
            //mqttClient.subscribe()
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
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Log.d(TAG, "clear MQTT Service");
        // TODO: 03.01.2020 clear MQTT Service and Broadcast Receiver for BLE
        //stopService(mqttService);
    }

}
