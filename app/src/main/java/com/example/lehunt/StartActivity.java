package com.example.lehunt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jaalee.sdk.BeaconManager;

public class StartActivity extends AppCompatActivity {

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
                    .setNeutralButton("Close App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            finish();
        }
    }

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
