package arya.com.washiato;

/**
 * Created by Eric on 5/10/2016.
 */
public class Machine {
    String name;
    String localCluster;
    String serialNumber;
    boolean washer;


    public Machine() {

    }

    public Machine(String name, String localCluster, String serialNumber, boolean washer) {
        this.name = name;
        this.localCluster = localCluster;
        this.serialNumber = serialNumber;
        this.washer = washer;
    }

    public String getName() {
        return this.name;
    }
    public String getLocalCluster() {
        return this.localCluster;
    }
    public String getSerialNumber() {
        return this.serialNumber;
    }
    public boolean getWasher() {
        return this.washer;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setLocalCluster(String localCluster) {
        this.localCluster = localCluster;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public void setWasher(boolean washer) {
        this.washer = washer;
    }

}

