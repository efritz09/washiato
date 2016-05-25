package arya.com.washiato;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ClusterActivity extends AppCompatActivity {
    private final String TAG = "ClusterActivity";
    public static Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    public Cluster cluster = new Cluster();
    public Map<String,Object> clusterMap;
    public String currclusterName;
    public static String clusterName;
    public ArrayList<Machine> machineList;
    public ClusterStatusAdapter statusAdapter;

    static ChildEventListener clusterMachineListener;
    static ValueEventListener clusterStatusListener;

    TextView text_cluster_name;
    TextView text_cluster_location;
    TextView text_cluster_washers_available;
    TextView text_cluster_dryers_available;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        text_cluster_dryers_available = (TextView) findViewById(R.id.text_cluster_dryers_available);
        text_cluster_washers_available = (TextView) findViewById(R.id.text_cluster_washers_available);
        text_cluster_location = (TextView) findViewById(R.id.text_cluster_location);
        text_cluster_name = (TextView) findViewById(R.id.text_cluster_name);

        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

        machineList = new ArrayList<>();
        //check to see if there's a default cluster. if so, get it
        if(ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus()== false) {
            //cluster exists; pull this data
            clusterName = (String) ControlActivity.thisUser.get("defaultCluster");
            Log.i(TAG, "cluster exists! getting data from " + clusterName);
            setUpClusterListener(clusterName);
            setUpMachineListener();
        }

        else if (ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus()== true){
            //cluster is different from default cluster
            currclusterName = (String) ControlActivity.thisUser.get("CurrCluster");
            clusterName = currclusterName;
            Log.i(TAG, "different cluster from default! getting data from " + clusterName);
            setUpClusterListener(clusterName);
            setUpMachineListener();
        }

        ListView listView = (ListView) findViewById(R.id.listview_cluster);
        statusAdapter = new ClusterStatusAdapter(this, R.layout.cluster_status, machineList);
        listView.setAdapter(statusAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                Log.i(TAG,"longclicked");
                return false;
            }
        });


        statusAdapter.notifyDataSetChanged();
    }

    /*
    creates the cluster listener. Only gathers the machine list and the location
     */
    public void setUpClusterListener(String name) {
        clusterStatusListener = ref.child("Clusters").child(clusterName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Store this in the cluster class
                clusterMap = (Map<String, Object>) dataSnapshot.getValue();
                Log.i(TAG, (String) clusterMap.get("location"));
                cluster.setLocation((String) clusterMap.get("location"));
                cluster.setMachines((ArrayList<String>) clusterMap.get("machines"));
                //update all the text views
                updateCluster();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /*
    creates the machine listener.
     */
    public void setUpMachineListener() {
        //now that the cluster is ready, populate the machines
        Query queryRef = ref.child("Machines").orderByChild("localCluster").equalTo(clusterName);
        Log.i(TAG,"setting up machine listener");
        clusterMachineListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                System.out.println(dataSnapshot.getKey());
                Log.i(TAG,"machine child added");
                Machine machine = new Machine();
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                if(map == null) Log.i(TAG,"fucker is null");
                machine.setLocalCluster((String)map.get("localCluster"));
                machine.setName((String)map.get("name"));
                machine.setStatus((int)(long)map.get("status"));
                machine.setWasher((boolean)map.get("washer"));
                machine.setTime((int)(long)map.get("time"));
                machine.setOmw((boolean)map.get("omw"));
                machineList.add(machine);
                SortMachines();
                updateOpenMachines();
                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                Log.i(TAG,"machine child changed");
                if(map == null) Log.i(TAG,"fucker is null");
                for(int i = 0; i < machineList.size(); i++) {
                    //look for the one that has changed in machineList
                    Machine machine = machineList.get(i);
                    if(machine.getName().equals(map.get("name"))) {
                        //update all the info
                        machine.setLocalCluster((String)map.get("localCluster"));
                        machine.setName((String)map.get("name"));
                        machine.setStatus((int)(long)map.get("status"));
                        machine.setWasher((boolean)map.get("washer"));
                        machine.setTime((int)(long)map.get("time"));
                        machine.setOmw((boolean)map.get("omw"));
                        machineList.remove(i); //remove the one that's in there
                        machineList.add(machine); //add our updated one
                        SortMachines(); //resort
                        updateOpenMachines();
                        statusAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                Log.i(TAG,"machine child removed");
                for(int i = 0; i < machineList.size(); i++) {
                    Machine machine = machineList.get(i);
                    if(machine.getName().equals(map.get("name"))) {
                        machineList.remove(i); //remove the one that's in there
                        statusAdapter.notifyDataSetChanged();
                        updateOpenMachines();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    /*
    updates the cluster values locally
     */
    public void updateCluster() {
        Log.i(TAG, "updating all the cluster shit");
        text_cluster_name.setText(clusterName + ", ");
        text_cluster_location.setText(cluster.getLocation());
        updateOpenMachines();
    }

    /*
    updates the number of open and finished machines
     */
    public void updateOpenMachines() {
        int openWashers = 0;
        int openDryers = 0;
        int finishedWashers = 0;
        int finishedDryers = 0;
        // update the displayed number of washers and dryers available
        for(int i = 0; i < machineList.size(); i++) {
            Machine thisMachine = machineList.get(i);
            if(thisMachine.status == 0) {
                if(thisMachine.getWasher()) openWashers++;
                else openDryers++;
            }else if(thisMachine.status == 1) {
                if(thisMachine.getWasher()) finishedWashers++;
                else finishedDryers++;
            }
        }
        Log.i(TAG,"num wash = " + openWashers + "; num dry = "+ openDryers);
        Log.i(TAG,"fin wash = " + finishedWashers + "; fin dry = "+finishedDryers);
        cluster.setNumWash(openWashers);
        cluster.setNumDry(openDryers);
        cluster.setFinWash(finishedWashers);
        cluster.setFinDry(finishedDryers);
        String plural1 = "s";
        String plural2 = "s";
        if(openWashers == 1) plural1 = "";
        if(finishedWashers == 1) plural2 = "";
        text_cluster_washers_available.setText(openWashers+" washer"+plural1+" open, "+finishedWashers+" washer"+plural2+" finished");
        if(openDryers != 1) plural1 = "s";
        if(finishedDryers != 1) plural2 = "s";
        text_cluster_dryers_available.setText(openDryers+" dryer"+plural1+" open, "+finishedDryers+" dryer"+plural2+" finished");
    }

    /*
    sorts the machines by the status
     */
    public void SortMachines() {
        if(machineList.size() > 1) {
            Log.i(TAG,"sorting");
            Collections.sort(machineList, new Comparator<Machine>() {
                @Override
                public int compare(Machine m1, Machine m2) {
                    return m1.status - m2.status;
                }
            });
        }
    }

    public static void endClusterListeners() {
        Log.i("ClusterActivity","removing listeners");
        if(clusterMachineListener != null) ref.removeEventListener(clusterMachineListener);
        if(clusterStatusListener != null) ref.removeEventListener(clusterStatusListener);
        clusterStatusListener = null;
        clusterMachineListener = null;
    }


/*     //Debugging cluster view: function when button stat1 is pressed; changes status of a machine in Firebase
   public void changeStat1(View view) {
        Log.i(TAG,"changing status 1 in Firebase");
        ref.child("Machines").child("04457C8A6F4080").child("status").setValue(2);
    }

    //Debugging cluster view: function when button stat2 is pressed; changes status of a machine in Firebase
    public void changeStat2(View view) {
        Log.i(TAG,"changing status 2 in Firebase");
        ref.child("Machines").child("044D5B8A6F4080").child("status").setValue(0);
    }*/
}
