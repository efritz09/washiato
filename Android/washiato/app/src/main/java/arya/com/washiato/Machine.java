package arya.com.washiato;

/**
 * Created by Eric on 5/10/2016.
 */
public class Machine {
    String name;
    String localCluster;
    int status;
    boolean washer;
    boolean omw; //whether or not user is on their way
    int time; //time since user is on their way


    public Machine() {

    }

    public Machine(String name, String localCluster, int status, boolean washer, boolean omw, int time) {
        this.name = name;
        this.localCluster = localCluster;
        this.status = status;
        this.washer = washer;
        this.omw = omw;
        this.time = time;
    }

    public String getName() {
        return this.name;
    }
    public String getLocalCluster() {
        return this.localCluster;
    }
    public boolean getWasher() {
        return this.washer;
    }
    public int getStatus() {
        return this.status;
    }
    public boolean getOmw() {
        return this.omw;
    }
    public int getTime() {
        return this.time;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setLocalCluster(String localCluster) {
        this.localCluster = localCluster;
    }
    public void setWasher(boolean washer) {
        this.washer = washer;
    }
    public void setStatus(int status) { this.status = status; }
    public void setOmw(boolean omw) { this.omw = omw; }
    //public void setTime(int time) { this.time = time; }
}

