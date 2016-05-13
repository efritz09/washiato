package arya.com.washiato;

/**
 * Created by Eric on 5/10/2016.
 */
public class Machine {
    String name;
    String localCluster;
    int status;
    boolean washer;


    public Machine() {

    }

    public Machine(String name, String localCluster, int status, boolean washer) {
        this.name = name;
        this.localCluster = localCluster;
        this.status = status;
        this.washer = washer;
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

    public void setName(String name) {
        this.name = name;
    }
    public void setLocalCluster(String localCluster) {
        this.localCluster = localCluster;
    }
    public void setWasher(boolean washer) {
        this.washer = washer;
    }
    public void setStatus(int status) {
        this.status = status;
    }

}

