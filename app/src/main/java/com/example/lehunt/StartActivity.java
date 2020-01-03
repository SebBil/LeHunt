package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Start activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Start Activity destroyed");
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
