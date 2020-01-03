package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;

public class GameActivity extends AppCompatActivity {

    private int REQUEST_ENABLE_BT = 1;
    private String TAG = "GameActivity";
    BluetoothManager bluetoothManager;
    BluetoothAdapter BTAdapter;

    private MqttAndroidClient client;
    private HuntMqttClient huntMqttClient;

    private Button btnPubTestingPurpose;
    private Intent mqttService;
    private Intent bluetoothService;
    private String topicPub, topicSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /** For Testing */
        btnPubTestingPurpose = findViewById(R.id.btnPubTestingPurpose);

        Intent i = getIntent();
        Hunt curHunt = (Hunt)i.getExtras().getSerializable("HUNT");

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        huntMqttClient = new HuntMqttClient();
        client = huntMqttClient.getMqttClient(getApplicationContext(), curHunt.getBrokerURL(), curHunt.getClientID());

        /** Check Bluetooth is enabled and the adapter is not null, Start it if one is true */
        if (BTAdapter == null || !BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // TODO: 27.12.2019 Start thread/service for checking all devices in the environment maybe BCReceiver is enough

        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", curHunt);
        mqttService = new Intent(GameActivity.this, MqttMessageService.class);
        mqttService.putExtras(bundle);
        // bindService(mqttService)
        startService(mqttService);
        // TODO: 27.12.2019 subscribe the topic of the cliendid



        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(bReciever, filter);
        BTAdapter.startDiscovery();

        /** For Testing */
        btnPubTestingPurpose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String beaconID = "";
                    Log.d(TAG, "publish test to topic: " + topicPub);
                    huntMqttClient.publishMessage(client,"test", 1, topicPub);
                } catch (MqttException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        try {
            String beaconID = "";
            Log.d(TAG, "publish test to topic: " + topicPub);
            huntMqttClient.subscribe(client,topicSub, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, device.getName());
                // pubishMsgForNewHints(device.getName(), 1);
            }
        }
    };

    private void pubishMsgForNewHints(String beaconID, int qos){
        try {
            Log.d(TAG, "publish beacon ID to topic for new Hints");
            huntMqttClient.publishMessage(client, beaconID, qos, topicPub);
        } catch (MqttException | UnsupportedEncodingException e){
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
        Log.d(TAG, "clear MQTT Service and Broadcast Receiver for BLE");
        // TODO: 03.01.2020 clear MQTT Service and Broadcast Receiver for BLE
        getBaseContext().unregisterReceiver(bReciever);
        BTAdapter.cancelDiscovery();
        stopService(mqttService);
    }

}
