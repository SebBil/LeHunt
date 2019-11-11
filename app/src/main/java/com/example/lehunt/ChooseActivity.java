package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseActivity extends AppCompatActivity {

    String[] hunts = {
            "hunt 1",
            "hunt 2",
            "hunt 3",
            "hunt 4",
            "hunt 5",
            "hunt 76"
    };

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

    // TODO: 11.11.2019 constraints by choose_resume landscape setzen

    public void btnDeleteHuntClicked(View v){

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
