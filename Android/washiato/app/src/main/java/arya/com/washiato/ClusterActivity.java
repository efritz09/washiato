package arya.com.washiato;

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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class ClusterActivity extends AppCompatActivity {
    private final String TAG = "ClusterActivity";
    public Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    public Cluster cluster = new Cluster();
    public Map<String,Object> clusterMap;
    public String clusterName;
    public ArrayList<Machine> machineList;
    public ClusterStatusAdapter statusAdapter;

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
        if(ControlActivity.thisUser.containsKey("defaultCluster")) {
            //cluster exists
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
            ref.child("Machines").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "updating machines");
                    //update the machines in the listview
                    updateMachines();
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
    }

    public void updateMachines() {

        //fill the array list with all the machines!
        Log.i(TAG,"number of machines: " + Integer.toString(cluster.machines.size()));
        for(int i = 0; i < cluster.machines.size(); i++) {
            Log.i(TAG,"filling number: " + Integer.toString(i)+", " + (String)cluster.machines.get(i));

            //we may want to change this to .addValueEventListener. I don't know yet
            ref.child("Machines").child(cluster.machines.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //populate the "machineList" for the listView
                    Machine machine = new Machine();
                    Map map = (Map<String,Object>) dataSnapshot.getValue();
                    if(map == null) Log.i(TAG,"fucker is null");
                    if(map.containsKey("localCluster")) Log.i(TAG,"key is in there");
                    machine.setLocalCluster((String)map.get("localCluster"));
                    machine.setName((String)map.get("name"));
                    machine.setStatus((int)(long)map.get("status"));
                    machine.setWasher((boolean)map.get("washer"));
                    machineList.add(machine);
                    statusAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }

    }

}
