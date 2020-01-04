package com.example.lehunt;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.ArrayList;
import java.util.List;

public class BLEScanner {

    private static final int SCAN_INTERVAL_MS = 30000;
    private final String TAG = "BLEScanner";

    private Handler scanHandler = new Handler();
    private List<ScanFilter> scanFilters = new ArrayList<>();
    private ScanSettings scanSettings;
    private boolean isScanning = false;

    private MqttAndroidClient client;

    public void beginScanning(MqttAndroidClient c) {
        Log.d(TAG, "beginScanning");
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = scanSettingsBuilder.build();

        this.client = c;

        scanHandler.post(scanRunnable);
    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

            if (isScanning) {
                scanner.stopScan(scanCallback);
            } else {
                scanner.startScan(scanFilters, scanSettings, scanCallback);
            }

            isScanning = !isScanning;

            scanHandler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @TargetApi(26)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice dev = result.getDevice();
            if(dev.getName() != null){
                Log.d(TAG, String.valueOf(callbackType));
                Log.d(TAG, dev.getName() + "");
            }
            //int rssi = result.getRssi();

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("SCANCALLBACK", "Scan failed with errorcode: " + errorCode);
            // a scan error occurred
        }
    };
/*
    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.lehunt_launcher_round)
                        .setContentTitle(topic)
                        .setContentText(msg);
        Intent resultIntent = new Intent(this, GameActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(GameActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }
 */
}
