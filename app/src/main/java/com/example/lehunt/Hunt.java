package com.example.lehunt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hunt implements Serializable {

    private String mHuntID;
    private String mBrokerURL;
    private String mClientID;
    private List<String> mHints;

    public Hunt(String hID, String bURL){
        this.mHuntID = hID;
        this.mBrokerURL = bURL;

        this.mHints = new ArrayList<String>();
    }

    public String getBrokerURL(){
        return this.mBrokerURL;
    }

    public void setClientID(String cID){
        this.mClientID = cID;
    }

    public String getHuntID() {
        return this.mHuntID;
    }

    public String getClientID(){ return this.mClientID; }

    @Override
    public String toString(){
        return getHuntID();
     }
}
