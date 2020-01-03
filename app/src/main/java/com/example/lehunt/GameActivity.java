package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

    private MqttAndroidClient client;
    private HuntMqttClient huntMqttClient;

    private Button btnPubTestingPurpose;
    private Intent mqttService;
    private Intent bluetoothService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        btnPubTestingPurpose = findViewById(R.id.btnPubTestingPurpose);

        Intent i = getIntent();
        Hunt curHunt = (Hunt)i.getExtras().getSerializable("HUNT");

        String topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        String topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        huntMqttClient = new HuntMqttClient();
        client = huntMqttClient.getMqttClient(getApplicationContext(), curHunt.getBrokerURL(), curHunt.getClientID());


        // TODO: 22.11.2019 if ble is disabled finish this activity and make a toast for enable bluetooth
        // CheckBluetooth();

        // TODO: 27.12.2019 Start thread/service for checking all devices in the environment



        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", curHunt);
        mqttService = new Intent(GameActivity.this, MqttMessageService.class);
        mqttService.putExtras(bundle);
        bindService(mqttService,)
        startService(mqttService);

        // TODO: 27.12.2019 subscribe the topic of the cliendid


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




    /**
     * Check the Bluetoothmanager
     */
    private void CheckBluetooth() {
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
    }

    /*
    private Mqtt5AsyncClient connectToMQTTBroker(Hunt hunt){

        String clientid = UUID.randomUUID().toString();
        hunt.setClientID(clientid);

        Mqtt5AsyncClient client = MqttClient.builder()
                .useMqttVersion5()
                .identifier(clientid)
                .serverHost(hunt.getBrokerURL())
                .serverPort(1883)
                .buildAsync();

        client.connectWith().send();
        Toast.makeText(this, "Client connected to Broker", Toast.LENGTH_SHORT).show();
                /*.whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        System.out.println("failure to connect");
                        // Handle connection failure
                    } else {
                        String topic = hunt.getHuntID() + "/" + clientid + "/down";


                        client.subscribeWith()
                            .topicFilter(topic)
                            .callback(publish -> {
                                System.out.println(publish.getPayloadAsBytes().toString());
                                // Process the received message
                            })
                            .send()
                            .whenComplete((subAck, throwable2) -> {
                                if (throwable2 != null) {
                                    // Handle failure to subscribe
                                } else {
                                    System.out.println("sub success");
                                    Toast.makeText(this, "Client subscribe to topic: ", Toast.LENGTH_SHORT).show();

                                    // Handle successful subscription, e.g. logging or incrementing a metric
                                }
                            });

                    }
                });

        return client;
    }
 */

}
