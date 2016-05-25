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
    int finWash;
    int finDry;
    ArrayList<String> machines;

    public Cluster() {

    }

    public Cluster(String location, ArrayList<String> machines) {
        //this.name = name;
        this.location = location;
        this.machines = machines;
    }

    public Cluster(String location, ArrayList<String> machines, int numWash, int numDry, int finWash, int finDry) {
        //this.name = name;
        this.location = location;
        this.machines = machines;
        this.finDry = finDry;
        this.finWash = finWash;
        this.numWash = numWash;
        this.numDry = numDry;
    }

    //public String getName() {return this.name;}
    public String getLocation() {
        return this.location;
    }
    public int getNumWash() {return this.numWash;}
    public int getNumDry() {return this.numDry;}
    public ArrayList<String> getMachines() {return this.machines;}
    public int getFinWash() {return this.finWash;}
    public int getFinDry() {return this.finDry;}

    // public void setName(String name) {this.name = name;}
    public void setLocation(String location) {this.location = location;}
    public void setNumWash(int numWash) {this.numWash = numWash;}
    public void setNumDry(int numDry) {this.numDry = numDry;}
    public void setMachines(ArrayList<String> machines) {this.machines = machines;}
    public void setFinWash(int finWash) {this.finWash = finWash;}
    public void setFinDry(int finDry) {this.finDry = finDry;}
}
