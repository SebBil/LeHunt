package com.example.lehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class GameActivity extends AppCompatActivity {

    //Jaalee BEACON Default UUID
    private static final String JAALEE_BEACON_PROXIMITY_UUID = "EBEFD083-70A2-47C8-9837-E7B5634DF524";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private String TAG = "GameActivity";

    private Mqtt3AsyncClient mqttClient;
    private BeaconManager beaconManager;

    private String topicPub, topicSub;
    private int REQUEST_ENABLE_BT = 1;
    private Beacon beacon;
    private Hunt curHunt;
    private TextView tvCurrentHunt;
    private int beaconMinor;
    private HintCurrentFragment hcf;
    private HintPreviousFragment hpf;
    private FrameLayout fragContainer;

    @Override
    @TargetApi(24)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        beaconManager = new BeaconManager(this);

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }

        // Configure BeaconManager
        beaconManager.setRangingListener(LookForNewBeacons);

        // Check all Permissions
        System.out.println("checkPermissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        System.out.println("gameactivity OnCreate");

        Intent i = getIntent();
        curHunt = (Hunt) i.getExtras().getSerializable("HUNT");

        hcf = HintCurrentFragment.GetInstance();
        hpf = HintPreviousFragment.GetInstance();

        this.replaceFragment(hcf, "hcf");

        fragContainer = findViewById(R.id.framelayoutcontainer);
        fragContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Fragment frag = getSupportFragmentManager().findFragmentByTag("hcf");
                if(frag == null){
                    // hpf
                    hpf.UpdateHintList(curHunt.getHints());
                    return;
                }
                curHunt.getHints().forEach((key, value)->{
                    if(curHunt.getCurrentStation()-1 == (int)key)
                        hcf.UpdateHint((String)value);
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
                // TODO: 14.01.2020 Finish the Activity beacause of the client couldn't connect to the broker
                System.out.println("Client could not connect to MQTT Broker with address " + curHunt.getBrokerURL());
            } else {
                System.out.println("Client conncted successfully to MQTT Broker with address " + curHunt.getBrokerURL());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        tvCurrentHunt = findViewById(R.id.tvCurrentHunt);
        tvCurrentHunt.setText("Current Hunt: " + curHunt.getHuntID());

        OnMessageCallback onMessageCallback = new OnMessageCallback(curHunt, this);

        mqttClient.subscribeWith()
                .topicFilter(topicSub)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(onMessageCallback)
                .send();
        System.out.println("Client subscribed to topic " + topicSub);


        //TODO request the first hint to start the hunt but only if this is a new beginning hunt
        if (curHunt.getHints().size() == 0)
            mqttClient.publishWith()
                    .topic(topicPub)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload("{type:FirstHint,advertisement:0,station:0}".getBytes())
                    .send();
    }

    private RangingListener LookForNewBeacons = new RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Note that beacons reported here are already sorted by estimated
                    // distance between device and beacon.
                    if (list.size() > 0) {
                        beacon = (Beacon) list.get(0);
                        if (beacon.getProximityUUID().equalsIgnoreCase(JAALEE_BEACON_PROXIMITY_UUID)) {
                            // System.out.println("RSSI: " + beacon.getRssi());
                            if (beacon.getRssi() > -50) {
                                //System.out.println("Name: " + beacon.getName());
                                //System.out.println("UUID: " + beacon.getProximityUUID());
                                //System.out.println("Major: " + beacon.getMajor());
                                //System.out.println( "Minor: " + beacon.getMinor());
                                //System.out.println("KeyAlreadyPresent: " + curHunt.keyAlreadyPresent(beacon.getMinor()));
                                beaconMinor = beacon.getMinor();
                                if (!curHunt.keyAlreadyPresent(beaconMinor)) {
                                    publishMsgForNewHints(String.valueOf(beaconMinor), curHunt);
                                }
                            }
                        }
                    }
                }
            });
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("OnStart gameact");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause gameact");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume gameact");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop gameact");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        System.out.println("onDestroy gameact");

        save(curHunt);
        System.out.println("BeaconManager disconnected");
        beaconManager.disconnect();
        System.out.println("MQTT Client disconnected");
        mqttClient.disconnect();
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

    /**
     * Replace current Fragment with the destination Fragment.
     * @param frag
     */
    public void replaceFragment(Fragment frag, String tag){
        // First get FragmentManager object.
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.framelayoutcontainer, frag, tag);

        // Commit the Fragment replace action.
        fragmentTransaction.commit();
    }

    /**
     * Start Service for the Ranging and Discovering Devices
     */
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

    /**
     * Send the message to the broker that a new Beacon was discovered
     *
     * @param beaconMiner
     * @param hunt
     */
    private void publishMsgForNewHints(String beaconMiner, Hunt hunt) {

        System.out.println("publish beacon ID to topic for new Hints");
        String msg = "{type:NewBeacon,advertisement:" + beaconMiner + ",station:" + hunt.getCurrentStation() + "}";
        mqttClient.publishWith()
                .topic(topicPub)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(msg.getBytes())
                .send();

    }

    public void save(Hunt obj) {
        FileOutputStream fos = null;

        String ext = ".json";
        StringBuilder sb = new StringBuilder(obj.getHuntID()).append(ext);
        String fileName = sb.toString();
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(new Gson().toJson(obj).getBytes());

            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + fileName,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendNotification(String title){

        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(
                context.getApplicationContext(), GameActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(
                context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText("New Informations available")
                .setDefaults(
                        Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pIntent).setAutoCancel(true)
                .setSmallIcon(R.mipmap.lehunt_launcher_round).build();
        notificationManager.notify(2, notification);
    }

    public void syncCurrentHunt(Hunt hunt){
        this.curHunt = hunt;
    }

    /**
     * Class for the Callback of messeages that came in
     */
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
            System.out.println("Incomming Message: " + newHint);

            /*
             * Types that can came back:
             *   Info, Error, NewInformation, Finished
             */
            String type = "", response = "";
            try {
                JSONObject json = new JSONObject(newHint);
                type = json.getString("type");
                response = json.getString("message");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (type) {
                case "Info":
                    // System.out.println("Info: " + response);
                    makeToast("Info: " + response);
                    //Toast.makeText(context, "Info: " + response, Toast.LENGTH_SHORT).show();
                    break;
                case "Error":
                    //System.out.println("Error: " + response);
                    makeToast("Error: " + response);
                    //Toast.makeText(context, "Error: " + response, Toast.LENGTH_SHORT).show();
                    break;
                case "NewInformation":
                    curHunt.newHint(beaconMinor, response);
                    //System.out.println("Response: " + response);
                    //System.out.println("Size of Hints: " + curHunt.getHints().size());

                    if(beaconMinor == 0){
                        sendNotify("First Hint");
                    } else {
                        sendNotify(type);
                    }

                    update(response, this.curHunt);

                    break;
                case "Finished":
                    curHunt.newHint(beaconMinor, response);
                    update(response, this.curHunt);
                    System.out.println("Finished case");
                    sendNotify(type);
                    alertFin(type, response);
                    break;
                default:
                    break;

            }
        }

        public void alertFin(String title, String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder aldb = new AlertDialog.Builder(GameActivity.this);
                    //System.out.println("in finished on ui thread");
                    //set title
                    aldb.setTitle(title);
                    aldb
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mqttClient.unsubscribeWith()
                                            .topicFilter(topicSub)
                                            .send();

                                    mqttClient.disconnect();
                                    beaconManager.disconnect();

                                    ((Activity) context).finish();
                                }
                            });
                    System.out.println("after aldb sets");

                    // create alert dialog
                    AlertDialog alertDialog = aldb.create();

                    System.out.println("after create");

                    alertDialog.show();
                }
            });
        }

        public void makeToast(String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void update(String s, Hunt h){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(s);
                    syncCurrentHunt(h);
                    hcf.UpdateHint(s);
                }
            });
        }

        public void sendNotify(String title) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("in send notify");
                    sendNotification(title);
                }
            });
        }

    }
}
