package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.BundleCompat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChooseActivity extends AppCompatActivity {

    private final String TAG = "CHOOSEN";

    private Hunt mSelectedHunt;
    private List<Hunt> mStoredHunts;
    private ArrayAdapter<Hunt> mHuntAdapter;

    EditText burl, huntid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        System.out.println("choose activtiy onCreate");

        mStoredHunts = getStoredHunts();

        switch (intent.getExtras().getInt("CHOOSE_VALUE")) {
            case 1000:
                setContentView(R.layout.activity_choose_begin);

                burl = findViewById(R.id.etBrokerURL);
                huntid = findViewById(R.id.etHuntID);

                break;
            case 1001:
                setContentView(R.layout.activity_choose_resume);

                mHuntAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStoredHunts);

                final ListView lvMyHunts = findViewById(R.id.listHunts);
                lvMyHunts.setAdapter(mHuntAdapter);

                lvMyHunts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedHunt = (Hunt) lvMyHunts.getItemAtPosition(position);
                    }
                });

                break;
            default:
                // should not go there
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
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
                System.out.println(strFile);
                ret.add(load(strFile));
            };
        }

        return ret;
    }

    public void btnDeleteHuntClicked(View v) {
        if (this.mSelectedHunt != null) {

            // delete it from list
            this.mStoredHunts.remove(this.mSelectedHunt);

            String ext = ".json";
            File dir = getFilesDir();
            File file = new File(dir, mSelectedHunt.getHuntID() + ext);
            boolean deleted = file.delete();
            if(deleted) {
                Toast.makeText(this, "selected Hunt: " + this.mSelectedHunt.toString() + " deleted", Toast.LENGTH_SHORT).show();
                this.mSelectedHunt = null;
            } else {
                Toast.makeText(this, "Something went wrong for deleting the file", Toast.LENGTH_LONG).show();
            }
        }

        mHuntAdapter.notifyDataSetChanged();
    }

    public void btnBeginHuntClicked(View v) {

        Hunt hunt = new Hunt(huntid.getText().toString(), burl.getText().toString());
        String clientid = UUID.randomUUID().toString();
        hunt.setClientID(clientid);

        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", hunt);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void btnResumeHuntClicked(View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("HUNT", mSelectedHunt);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public Hunt load(String fileName){
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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
