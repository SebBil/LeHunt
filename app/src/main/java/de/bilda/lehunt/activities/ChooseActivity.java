package de.bilda.lehunt.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.bilda.lehunt.R;
import de.bilda.lehunt.classes.Hunt;

public class ChooseActivity extends AppCompatActivity {

    private Hunt mSelectedHunt;
    private List<Hunt> mStoredHunts;
    private ArrayAdapter<Hunt> mHuntAdapter;

    private EditText burl, huntId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        mStoredHunts = getStoredHunts();

        switch (intent.getExtras().getInt("CHOOSE_VALUE")) {
            case 1000:
                setContentView(R.layout.activity_choose_begin);

                burl = findViewById(R.id.etBrokerURL);
                huntId = findViewById(R.id.etHuntID);

                break;
            case 1001:
                setContentView(R.layout.activity_choose_resume);

                mHuntAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, mStoredHunts);

                final ListView lvMyHunts = findViewById(R.id.listHunts);
                lvMyHunts.setAdapter(mHuntAdapter);

                lvMyHunts.setOnItemClickListener((parent, view, position, id) -> mSelectedHunt = (Hunt) lvMyHunts.getItemAtPosition(position));

                break;
            default:
                // should not go there
        }
    }

    public void btnDeleteHuntClicked(View v) {
        if (this.mSelectedHunt != null) {

            this.mStoredHunts.remove(this.mSelectedHunt);

            String ext = ".json";
            File dir = getFilesDir();
            File file = new File(dir, mSelectedHunt.getHuntID() + ext);
            boolean deleted = file.delete();
            if(deleted) {
                Toast.makeText(this, "Hunt: " + this.mSelectedHunt.toString() + " deleted", Toast.LENGTH_SHORT).show();
                this.mSelectedHunt = null;
            } else {
                Toast.makeText(this, "Something went wrong for deleting the file", Toast.LENGTH_LONG).show();
            }
        }

        mHuntAdapter.notifyDataSetInvalidated();
    }

    public void btnBeginHuntClicked(View v) {

        if(huntId.length() == 0){
            Toast.makeText(this, "The field hunt id must not be empty.",Toast.LENGTH_SHORT).show();
            return;
        }
        for(Hunt h : mStoredHunts){
            if(h.getHuntID().matches(huntId.getText().toString())) {
                Toast.makeText(this, "This huntid is already present in your saved hunts. If you want to retry, first delete the hunt.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        if(!huntId.getText().toString().matches("^[a-zA-Z0-9]+")){
            Toast.makeText(this, "Unknown convention of the huntid.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(burl.length() == 0){
            Toast.makeText(this, "The field broker url must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: 18.01.2020 Check if url is reachable on port 1883

        Hunt hunt = new Hunt(huntId.getText().toString(), burl.getText().toString());
        String clientid = UUID.randomUUID().toString();
        hunt.setClientID(clientid);

        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", hunt);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    public void btnResumeHuntClicked(View v) {
        if(mSelectedHunt == null) {
            Toast.makeText(this, "No Hunt selected. Please select a hunt.", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", mSelectedHunt);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * getStoredHunts
     * load all Hunts with the hints from permanent storage
     *
     * @return a List of the Hunts
     */
    private List<Hunt> getStoredHunts() {
        ArrayList<Hunt> ret = new ArrayList<>();
        File dirFiles = getFilesDir();
        for (String strFile : dirFiles.list())
        {
            if(strFile.matches("^[a-zA-Z0-9]+.json")){
                ret.add(load(strFile));
            }
        }

        return ret;
    }


    private Hunt load(String fileName){
        FileInputStream fis = null;

        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            return new Gson().fromJson(sb.toString(), Hunt.class);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
