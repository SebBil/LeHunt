package com.example.lehunt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BluetoothScanService extends Service {




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
