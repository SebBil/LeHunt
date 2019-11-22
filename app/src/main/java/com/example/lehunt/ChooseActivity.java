package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChooseActivity extends AppCompatActivity {

    // TODO: 22.11.2019 Get hunts from permanent storage
    ArrayList<String> hunts = new ArrayList<String>() {{
        add("hunt 1");
        add("hunt 2");
        add("hunt 3");
        add("hunt 4");
        add("hunt 5");
        add("hunt 6");
    }};

    private int posSelectedHunt = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if(ChoosenContext(intent)) {
            setContentView(R.layout.activity_choose_begin);
        } else {
            setContentView(R.layout.activity_choose_resume);

            final ListView myHunts = findViewById(R.id.listHunts);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_list_item_1, hunts
            );
            myHunts.setAdapter(adapter);

            myHunts.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object o = myHunts.getItemAtPosition(position);

                    Toast.makeText(ChooseActivity.this, "Selected :" + o.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void btnDeleteHuntClicked(View v){
        // TODO: 22.11.2019 Remove this from permanent storage
        if(this.posSelectedHunt != -1)
            this.hunts.remove(this.posSelectedHunt);
        Toast.makeText(ChooseActivity.this, "Delete Button clicked", Toast.LENGTH_SHORT).show();
    }

    public void btnBeginHuntClicked(View v){
        // TODO: 22.11.2019 connect to broker url and save the client uuid
        Toast.makeText(ChooseActivity.this,"Begin Hunt clicked", Toast.LENGTH_SHORT).show();
    }

    public void btnResumeHuntClicked(View v){
        Toast.makeText(ChooseActivity.this, "Resume Hunt clicked", Toast.LENGTH_SHORT).show();
    }

    /**
     * Look for begin or resume a hunt
     * @param i
     * @return True if User begin a new hunt, false for resume a hunt
     */
    private boolean ChoosenContext(Intent i){
        Bundle b = i.getExtras();

        if (b.getInt("BEGIN") == 1000){
            return true;
        } else {
            return false;
        }
    }
}
