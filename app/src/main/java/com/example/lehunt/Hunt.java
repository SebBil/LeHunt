package com.example.lehunt;

import android.util.ArrayMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hunt implements Serializable {

    private String mHuntID;
    private String mBrokerURL;
    private String mClientID;
    private HashMap<Integer, String> mHints;
    //private List<String> mHints;

    public Hunt(String hID, String bURL){
        this.mHuntID = hID;
        this.mBrokerURL = bURL;

        this.mHints = new HashMap<>();
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

    /**
     * Addind a new Hint to the List of Hints
     * @param hint
     * @param id
     */
    public void newHint(String hint, int id){
        mHints.put(id, hint);
    }

    /**
     * Funktion that checks if the hint is already present in the List of Hints
     * @param id
     * @return
     */
    public boolean keyAlreadyPresent(int id){
        if(mHints.containsKey(id)){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        return getHuntID();
     }
}
