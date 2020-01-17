package com.example.lehunt;

import android.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

public class Hunt extends Observable implements Serializable{

    private String mHuntID;
    private String mBrokerURL;
    private String mClientID;
    private TreeMap<Integer, String> mHints;
    //private List<String> mHints;

    public Hunt(){
        mHints = new TreeMap<>();
    }

    public Hunt(String hID, String bURL){
        this.mHuntID = hID;
        this.mBrokerURL = bURL;

        this.mHints = new TreeMap<>();
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

    public int getCurrentStation(){
        return this.mHints.size();
    }

    public synchronized TreeMap getHints(){
        return this.mHints;
    }

    /**
     * Addind a new Hint to the List of Hints
     * @param hint
     * @param id
     */
    public void newHint(int id, String hint){
        synchronized (this) {
            mHints.put(id, hint);
        }
        setChanged();
        notifyObservers();
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
