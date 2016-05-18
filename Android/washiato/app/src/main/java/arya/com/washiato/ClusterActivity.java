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
    public String defclusterName;
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
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

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

            ref.child("Clusters").child(clusterName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Store this in the cluster class
                    clusterMap = (Map<String, Object>) dataSnapshot.getValue();
                    Log.i(TAG, (String) clusterMap.get("location"));
                    cluster.setLocation((String) clusterMap.get("location"));
                    cluster.setNumDry((int) (long) clusterMap.get("numDry"));
                    cluster.setNumWash((int) (long) clusterMap.get("numWash"));
                    cluster.setMachines((ArrayList<String>) clusterMap.get("machines"));
                    //update all the text views
                    updateCluster();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
//            ref.child("Machines").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i(TAG, "updating machines");
//                    //update the machines in the listview
//                    updateMachines();
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {
//                }
//            });
        }

        else if (ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus()== true){
            //cluster is different from default cluster
            currclusterName = (String) ControlActivity.thisUser.get("CurrCluster");
            clusterName = currclusterName;
            Log.i(TAG,clusterName);
            Log.i(TAG, "different cluster from default! getting data from " + clusterName);

            clusterStatusListener = ref.child("Clusters").child(clusterName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Store this in the cluster class
                    clusterMap = (Map<String, Object>) dataSnapshot.getValue();
                    Log.i(TAG, (String) clusterMap.get("location"));
                    cluster.setLocation((String) clusterMap.get("location"));
                    cluster.setNumDry((int) (long) clusterMap.get("numDry"));
                    cluster.setNumWash((int) (long) clusterMap.get("numWash"));
                    cluster.setMachines((ArrayList<String>) clusterMap.get("machines"));
                    //update all the text views
                    updateCluster();
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
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

    public void updateCluster() {
        Log.i(TAG, "updating all the cluster shit");
        text_cluster_name.setText(clusterName + ", ");
        text_cluster_location.setText(cluster.getLocation());
        text_cluster_dryers_available.setText(Integer.toString(cluster.getNumDry()) + " dryers available");
        text_cluster_washers_available.setText(Integer.toString(cluster.getNumWash()) + " washers available");

        //this is here to prevent the machine from updating before the cluster in the event of a cluster change
//        ref.child("Machines").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.i(TAG, "updating machines");
//                //update the machines in the listview
//                updateMachines();
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//            }
//        });
        //now that the cluster is ready, populate the machines
        Query queryRef = ref.child("Machines").orderByChild("localCluster").equalTo(clusterName);
        clusterMachineListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                System.out.println(dataSnapshot.getKey());
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
                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
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
                        statusAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                for(int i = 0; i < machineList.size(); i++) {
                    Machine machine = machineList.get(i);
                    if(machine.getName().equals(map.get("name"))) {
                        machineList.remove(i); //remove the one that's in there
                        statusAdapter.notifyDataSetChanged();
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
    }


//    public void updateMachines() {
//        machineList.clear();
//
//        //fill the array list with all the machines!
//        Log.i(TAG,"number of machines: " + Integer.toString(cluster.machines.size()));
//
//
//
//
//
//        for(int i = 0; i < cluster.machines.size(); i++) {
//            Log.i(TAG,"filling number: " + Integer.toString(i)+", " + (String)cluster.machines.get(i));
//
//            //we may want to change this to .addValueEventListener. I don't know yet
//            ref.child("Machines").child(cluster.machines.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    //populate the "machineList" for the listView
//                    Machine machine = new Machine();
//                    Map map = (Map<String,Object>) dataSnapshot.getValue();
//                    if(map == null) Log.i(TAG,"fucker is null");
//                    machine.setLocalCluster((String)map.get("localCluster"));
//                    machine.setName((String)map.get("name"));
//                    machine.setStatus((int)(long)map.get("status"));
//                    machine.setWasher((boolean)map.get("washer"));
//                    machineList.add(machine);
//
//                    if(machineList.size() > 1) {
//                        Log.i(TAG,"sorting");
//                        Collections.sort(machineList, new Comparator<Machine>() {
//                            @Override
//                            public int compare(Machine m1, Machine m2) {
//                                return m1.status - m2.status;
//                            }
//                        });
//                    }
//
//                    statusAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {}
//            });
//        }
//
//    }

    //Debugging cluster view: function when button stat1 is pressed; changes status of a machine in Firebase
    public void changeStat1(View view) {
        Log.i(TAG,"changing status 1 in Firebase");
        ref.child("Machines").child("04457C8A6F4080").child("status").setValue(2);
    }

    //Debugging cluster view: function when button stat2 is pressed; changes status of a machine in Firebase
    public void changeStat2(View view) {
        Log.i(TAG,"changing status 2 in Firebase");
        ref.child("Machines").child("044D5B8A6F4080").child("status").setValue(0);
    }
}
