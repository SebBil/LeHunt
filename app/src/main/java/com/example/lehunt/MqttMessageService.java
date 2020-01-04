package com.example.lehunt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttMessageService extends MqttService {

    private static final String TAG = "MqttMessageService";
    private HuntMqttClient huntMqttClient;
    private MqttAndroidClient mqttAndroidClient;

    private Hunt curHunt;

    public MqttMessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        this.curHunt = (Hunt) intent.getExtras().getSerializable("HUNT");

        huntMqttClient = new HuntMqttClient();
        mqttAndroidClient = huntMqttClient.getMqttClient(getApplicationContext(), curHunt.getBrokerURL(), curHunt.getClientID());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d(TAG, "maybe here for the first hint to start");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                Log.d(TAG, new String(mqttMessage.getPayload()));
                // setMessageNotification(s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name){
        Log.d(TAG, "stopService");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
