package arya.com.washiato;

import java.util.ArrayList;

/**
 * Created by Arya on 5/12/2016.
 */
public class Cluster {
    //String name;
    String location;
    int numWash;
    int numDry;
    ArrayList<String> machines;

    public Cluster() {

    }

    public Cluster(String location, int numWash, int numDry, ArrayList<String> machines) {
        //this.name = name;
        this.location = location;
        this.numWash = numWash;
        this.numDry = numDry;
        this.machines = machines;
    }

    //public String getName() {return this.name;}
    public String getLocation() {
        return this.location;
    }
    public int getNumWash() {return this.numWash;}
    public int getNumDry() {return this.numDry;}
    public ArrayList<String> getMachines() {return this.machines;}

    // public void setName(String name) {this.name = name;}
    public void setLocation(String location) {this.location = location;}
    public void setNumWash(int numWash) {this.numWash = numWash;}
    public void setNumDry(int numDry) {this.numDry = numDry;}
    public void setMachines(ArrayList<String> machines) {this.machines = machines;}
}
