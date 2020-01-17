package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class GameActivity extends AppCompatActivity {

    //Jaalee BEACON Default UUID
    private static final String JAALEE_BEACON_PROXIMITY_UUID = "EBEFD083-70A2-47C8-9837-E7B5634DF524";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private Mqtt3AsyncClient mqttClient;
    private BeaconManager beaconManager;

    private String topicPub, topicSub;
    private Beacon beacon;
    private Hunt curHunt;
    private int beaconMinor;
    private HintCurrentFragment hcf;
    private HintPreviousFragment hpf;

    @Override
    @TargetApi(24)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        beaconManager = new BeaconManager(this);

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }

        // Configure BeaconManager
        beaconManager.setRangingListener(LookForNewBeacons);


        System.out.println("gameactivity OnCreate");

        Intent i = getIntent();
        curHunt = (Hunt) i.getExtras().getSerializable("HUNT");

        hcf = HintCurrentFragment.GetInstance();
        hpf = HintPreviousFragment.GetInstance();

        this.replaceFragment(hcf, "hcf");

        FrameLayout fragContainer = findViewById(R.id.framelayoutcontainer);
        fragContainer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
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

        });

        // Mqtt Client create
        mqttClient = Mqtt3Client.builder()
                .identifier(curHunt.getClientID())
                .serverHost(curHunt.getBrokerURL())
                .buildAsync();

        try {
            Mqtt3ConnAck mqtt3ConnAck = mqttClient.connect().get();
            if (mqtt3ConnAck.getReturnCode().isError()) {
                Toast.makeText(this,"Client could not connect to MQTT Broker with address " + curHunt.getBrokerURL(), Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        topicPub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/up";
        topicSub = curHunt.getHuntID() + "/" + curHunt.getClientID() + "/down";

        TextView tvCurrentHunt = findViewById(R.id.tvCurrentHunt);
        tvCurrentHunt.setText(String.format("Current Hunt: %s", curHunt.getHuntID()));

        OnMessageCallback onMessageCallback = new OnMessageCallback(curHunt, this);

        mqttClient.subscribeWith()
                .topicFilter(topicSub)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(onMessageCallback)
                .send();
        System.out.println("Client subscribed to topic " + topicSub);

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
            runOnUiThread(() -> {
                if (list.size() > 0) {
                    beacon = list.get(0);
                    if (beacon.getProximityUUID().equalsIgnoreCase(JAALEE_BEACON_PROXIMITY_UUID)) {
                        if (beacon.getRssi() > -50) {
                            beaconMinor = beacon.getMinor();
                            if (!curHunt.keyAlreadyPresent(beaconMinor)) {
                                publishMsgForNewHints(String.valueOf(beaconMinor), curHunt.getCurrentStation());
                            }
                        }
                    }
                }
            });
        }
    };

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

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit the hunt.\nYour hunt will be saved for the future.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    /**
     * Replace current Fragment with the destination Fragment.
     * @param frag The Fragment that will displayed
     */
    public void replaceFragment(Fragment frag, String tag){
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayoutcontainer, frag, tag);
        fragmentTransaction.commit();
    }

    /**
     * Start Service for the Ranging and Discovering Devices
     */
    private void connectToService() {
        beaconManager.connect(() -> {
            try {
                beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Send the message to the broker that a new Beacon was discovered
     *
     * @param beaconMiner the station of the beacon in integer
     * @param station the station of the users hunt
     */
    private void publishMsgForNewHints(String beaconMiner, int station) {

        String msg = "{type:NewBeacon,advertisement:" + beaconMiner + ",station:" + station + "}";
        mqttClient.publishWith()
                .topic(topicPub)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(msg.getBytes())
                .send();

    }

    public void save(Hunt obj) {
        FileOutputStream fos = null;

        String ext = ".json";
        String fileName = obj.getHuntID() + ext;
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(new Gson().toJson(obj).getBytes());
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
                .setContentText("New Information available")
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

        private void alertFin(String title, String message) {
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                //System.out.println("in finished on ui thread");
                //set title
                builder.setTitle(title);
                builder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, id) -> {
                            mqttClient.unsubscribeWith()
                                    .topicFilter(topicSub)
                                    .send();

                            mqttClient.disconnect();
                            beaconManager.disconnect();

                            ((Activity) context).finish();
                        });
                System.out.println("after builder sets");

                // create alert dialog
                AlertDialog alertDialog = builder.create();

                System.out.println("after create");

                alertDialog.show();
            });
        }

        private void makeToast(String msg) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
        }

        private void update(String s, Hunt h){
            runOnUiThread(() -> {
                System.out.println(s);
                syncCurrentHunt(h);
                hcf.UpdateHint(s);
            });
        }

        private void sendNotify(String title) {
            runOnUiThread(() -> {
                System.out.println("in send notify");
                sendNotification(title);
            });
        }

    }
}
