package com.example.lehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static android.os.Build.HOST;

public class GameActivity extends AppCompatActivity {

    private static final String JAALEE_BEACON_PROXIMITY_UUID = "EBEFD083-70A2-47C8-9837-E7B5634DF524";//Jaalee BEACON Default UUID
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private String TAG = "GameActivity";

    private Mqtt3AsyncClient mqttClient;
    private BeaconManager beaconManager;

    private String topicPub, topicSub;
    private int REQUEST_ENABLE_BT = 1;
    private Beacon beacon;
    private boolean PubMessageSend = false;
    private LinearLayout hints;
    private int indexOfLastChild = -1;
    private int beaconMinor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent i = getIntent();
        Hunt curHunt = (Hunt) i.getExtras().getSerializable("HUNT");

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        hints = findViewById(R.id.ll_hints);

        // Configure BeaconManager
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
                        if (beacons.size() > 0) {
                            beacon = (Beacon) beacons.get(0);
                            if (beacon.getProximityUUID().equalsIgnoreCase(JAALEE_BEACON_PROXIMITY_UUID)) {
                                Log.d(TAG, "RSSI: " + beacon.getRssi());
                                if (beacon.getRssi() > -50) {
                                    Log.d(TAG, "Name: " + beacon.getName());
                                    Log.d(TAG, "UUID: " + beacon.getProximityUUID());
                                    Log.d(TAG, "Major: " + beacon.getMajor());
                                    Log.d(TAG, "Minor: " + beacon.getMinor());
                                    Log.d(TAG, "PubMsgSend: " + PubMessageSend);
                                    Log.d(TAG, "KeyAlreadyPresent: " + curHunt.keyAlreadyPresent(beacon.getMinor()));
                                    if (!PubMessageSend && !curHunt.keyAlreadyPresent(beacon.getMinor())) {
                                        publishMsgForNewHints(String.valueOf(beacon.getMajor()), String.valueOf(beacon.getMinor()));
                                        PubMessageSend = true;
                                        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        TextView tv = new TextView(getApplicationContext());
                                        tv.setLayoutParams(lparams);
                                        tv.setText("Message to mqtt send and new Hint is comming ...");
                                        hints.addView(tv);
                                        indexOfLastChild = hints.indexOfChild(tv);
                                        beaconMinor = beacon.getMinor();
                                    } else if (!PubMessageSend && curHunt.keyAlreadyPresent(beacon.getMinor())) {
                                        Toast.makeText(getApplicationContext(), "This station is already found, keep going on.", Toast.LENGTH_SHORT).show();
                                    } else if (PubMessageSend && curHunt.keyAlreadyPresent(beacon.getMinor())) {

                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        // Mqtt Client create
        mqttClient = Mqtt3Client.builder()
                .identifier(curHunt.getClientID())
                .serverHost(curHunt.getBrokerURL())
                .buildAsync();

        try {
            Mqtt3ConnAck mqtt3ConnAck = mqttClient.connect().get();
            if (mqtt3ConnAck.getReturnCode().isError()) {
                Log.d(TAG, "Client could not connect to MQTT Broker with address " + curHunt.getBrokerURL());
            } else {
                Log.d(TAG, "Client conncted successfully to MQTT Broker with address " + curHunt.getBrokerURL());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        OnMessageCallback onMessageCallback = new OnMessageCallback(curHunt, this);

        mqttClient.subscribeWith()
                .topicFilter(topicSub)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(onMessageCallback)
                .send();
        Log.d(TAG, "Client subscribed to topic " + topicSub);

        mqttClient.publishWith()
                .topic("test")
                .payload("testnachricht".getBytes())
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }

    @TargetApi(24)
    private class OnMessageCallback implements Consumer<Mqtt3Publish> {

        Hunt curHunt;
        Context context;

        OnMessageCallback(Hunt hunt, Context context) {
            this.context = context;
            this.curHunt = hunt;
        }

        @Override
        public void accept(Mqtt3Publish mqtt3Publish) {
            String newHint = new String(mqtt3Publish.getPayloadAsBytes());
            Log.d(TAG, "Incomming Message: " + newHint);

            String hint = "";
            try {
                JSONObject json = new JSONObject(newHint);
                hint = json.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            curHunt.newHint(hint, beaconMinor);
            Log.d(TAG, hint);
            Log.d(TAG, curHunt.toString());
            TextView tv = (TextView) hints.getChildAt(indexOfLastChild);
            Log.d(TAG, "get tv is ok");

            // TODO: 12.01.2020 find a solution for setting the hint from another class
            tv.setText(hint);

            // clear up beaconMinor and PubSend
            PubMessageSend = false;
            beaconMinor = -1;
            indexOfLastChild = -1;
        }
    }

    @Override
    protected void onStart() {
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

        // Check all Permissions
        Log.d(TAG, "checkPermissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    private void connectToService() {
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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


    private void publishMsgForNewHints(String beaconMajor, String beaconMiner) {
        Log.d(TAG, "publish beacon ID to topic for new Hints");
        String msg = new String("{type:NewBeacon,advertisment:" + beaconMajor + "-" + beaconMiner + "}");
        mqttClient.publishWith()
                .topic(topicPub)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(msg.getBytes())
                .send();

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
        // TODO: 11.01.2020 Safe/Update Current Hunt to Harddisk
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        Log.d("BM", "BeaconManager disconnected");
        beaconManager.disconnect();
        Log.d(TAG, "MQTT Client disconnected");
        mqttClient.disconnect();

        super.onDestroy();
    }
}
