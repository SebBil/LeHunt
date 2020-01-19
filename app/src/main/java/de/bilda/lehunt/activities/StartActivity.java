package de.bilda.lehunt.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.jaalee.sdk.BeaconManager;

import de.bilda.lehunt.R;

public class StartActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Check if device supports Bluetooth Low Energy.
        if (!new BeaconManager(this).hasBluetooth()) {
            AlertDialog.Builder aldb = new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Unfortunatelly your device doesn't support Bluetooth Low Energy\nSo you cannot use this app")
                    .setNeutralButton("Close App", (dialog, which) -> finish());
            aldb.create().show();
        }

        // Check all Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
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
