package com.example.lehunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.BundleCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChooseActivity extends AppCompatActivity {

    private Hunt mSelectedHunt;
    private List<Hunt> mStoredHunts;
    private ArrayAdapter<Hunt> mHuntAdapter;

    EditText burl, huntid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        mStoredHunts = getStoredHunts();

        switch (intent.getExtras().getInt("CHOOSE_VALUE")){
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

                lvMyHunts.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedHunt = (Hunt)lvMyHunts.getItemAtPosition(position);
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

        updateStoredHunts();
    }

    private void updateStoredHunts(){
        // TODO: 23.11.2019 update all hints of a hunt, or safe a new hunt
    }

    /**
     * getStoredHunts
     * load all Hunts with the hints from permanent storage
     * @return a List of the Hunts
     */
    private List<Hunt> getStoredHunts(){
        return new ArrayList<Hunt>() {{
            add(new Hunt("Hunt1234", "broker.url.com"));
            add(new Hunt("Hunt5678", "broker.url.com"));
        }};
    }

    public void btnDeleteHuntClicked(View v){
        // TODO: 22.11.2019 Remove this from permanent storage
        if(this.mSelectedHunt != null) {
            this.mStoredHunts.remove(this.mSelectedHunt);
            Toast.makeText(this, "selected Hunt: " + this.mSelectedHunt.toString() + " deleted", Toast.LENGTH_SHORT).show();
            this.mSelectedHunt = null;
        }

        mHuntAdapter.notifyDataSetChanged();
    }

    public void btnBeginHuntClicked(View v){
        // TODO: 23.11.2019 check the broker url and the id are available

        Hunt hunt = new Hunt(huntid.getText().toString(), burl.getText().toString());
        String clientid = UUID.randomUUID().toString();
        //String clientid = "androidTest";
        hunt.setClientID(clientid);
        // System.out.println(hunt.getBrokerURL());
        // System.out.println(hunt.toString());

        // TODO: 27.12.2019 check the host is reachable
        // boolean reachable = isHostAvailable(hunt.getBrokerURL(), 1883, 2000);



        //if(reachable){
            Bundle bundle = new Bundle();
            bundle.putSerializable("HUNT", hunt);
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        //} else {
        //    Toast.makeText(this, "The given URL is not reachable on Port 1883", Toast.LENGTH_SHORT).show();
        //}
    }

    public void btnResumeHuntClicked(View v){
        Toast.makeText(ChooseActivity.this, "Resume Hunt clicked", Toast.LENGTH_SHORT).show();
    }


    public static boolean isHostAvailable(final String host, final int port, final int timeout) {
        try (final Socket socket = new Socket()) {
            final InetAddress inetAddress = InetAddress.getByName(host);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);

            socket.connect(inetSocketAddress, timeout);
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
