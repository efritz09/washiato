package arya.com.washiato;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

        if(ControlActivity.thisUser.containsKey("defaultCluster")) {
            //cluster exists
            clusterName = (String)ControlActivity.thisUser.get("defaultCluster");
            Log.i(TAG,"cluster exists! getting data from " + clusterName);

            ref.child("Clusters").child(clusterName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    clusterMap = (Map<String,Object>) dataSnapshot.getValue();
                    Log.i(TAG,(String)clusterMap.get("location"));
                    cluster.setLocation((String)clusterMap.get("location"));
                    cluster.setNumDry((int)(long)clusterMap.get("numDry"));
                    cluster.setNumWash((int)(long)clusterMap.get("numWash"));
                    cluster.setMachines((ArrayList<String>)clusterMap.get("machines"));
                    //update all the shit!
                    updateCluster(cluster);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }
    }

    public void updateCluster(Cluster cluster) {
        Log.i(TAG,"updating all the cluster shit");
        text_cluster_name.setText(clusterName + ", ");
        text_cluster_location.setText(cluster.getLocation());
        text_cluster_dryers_available.setText(Integer.toString(cluster.getNumDry()) + " dryers available");
        text_cluster_washers_available.setText(Integer.toString(cluster.getNumWash()) + " washers available");


    }

}
