package com.example.lehunt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.jaalee.sdk.BeaconManager;

public class StartActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Start activity onCreate");
        setContentView(R.layout.activity_start);

        // Check if device supports Bluetooth Low Energy.
        if (!new BeaconManager(this).hasBluetooth()) {
            AlertDialog.Builder aldb = new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Unfortunatelly your device doesn't support Bluetooth Low Energy\nSo you cannot use this app")
                    .setNeutralButton("Close App", (dialog, which) -> finish());
            aldb.create().show();
            finish();
        }

        // Check all Permissions
        System.out.println("checkPermissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, yay! Start the Bluetooth device scan.
            } else {
                // Alert the user that this application requires the location permission to perform the scan.
            }
        }
    }
    */

    @Override
    protected void onStop() {
        System.out.println("Start activity stopped");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("Start Activity destroyed");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("start activity resumed");
    }

    public void btnBeginHuntClick(View v){
        Intent chooseIntent = new Intent(this, ChooseActivity.class);
        chooseIntent.putExtra ("CHOOSE_VALUE", 1000);
        startActivity(chooseIntent);
    }

    public void btnResumeHuntClick(View v){
        Intent chooseIntent = new Intent(this, ChooseActivity.class);
        chooseIntent.putExtra("CHOOSE_VALUE", 1001);
        startActivity(chooseIntent);
    }
}
