package de.bilda.lehunt.classes;

import java.io.Serializable;
import java.util.Observable;
import java.util.TreeMap;

public class Hunt extends Observable implements Serializable{

    private String mHuntID;
    private String mBrokerURL;
    private String mClientID;
    private boolean alreadyFinished;
    private TreeMap<Integer, String> mHints;

    public Hunt(String hID, String bURL){
        this.mHuntID = hID;
        this.mBrokerURL = bURL;
        this.alreadyFinished = false;

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
     * @param hint the response of the broker for new hints
     * @param id the beacon station that the user currently reached
     */
    public void newHint(int id, String hint) {
        mHints.put(id, hint);
    }

    public void setAlreadyFinished(boolean finished){
        this.alreadyFinished = finished;
    }

    public boolean isFinished(){
        if(alreadyFinished)
            return true;
        return false;
    }

    /**
     * Funktion to get the last Hint of the hunt
     * @return a String that include the ast entry
     */
    public String getLastHint(){
        if(mHints.size() > 0)
            return mHints.lastEntry().getValue();
        return "";
    }

    /**
     * Funktion that checks if the hint is already present in the List of Hints
     * It is for not oversending mqtt messages to the broker, if this beacon is already found
     * @param id the advertisment of the beacon that reached
     * @return true if this beacon is already found
     */
    public boolean keyAlreadyPresent(int id){
        return mHints.containsKey(id);
    }

     @Override
    public String toString(){
        return this.mHuntID;
    }
}
