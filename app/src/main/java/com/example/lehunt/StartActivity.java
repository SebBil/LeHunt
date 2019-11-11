package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    public static final String BEGIN = "BEGIN";
    public static final String RESUME = "RESUME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void btnBeginHuntClick(View v){

        Intent chooseIntent = new Intent(this, ChooseActivity.class);
        chooseIntent.putExtra (BEGIN, 1000);
        startActivity(chooseIntent);

    }

    public void btnResumeHuntClick(View v){
        Intent chooseIntent = new Intent(this, ChooseActivity.class);
        chooseIntent.putExtra(RESUME, 1001);
        startActivity(chooseIntent);
    }
}
