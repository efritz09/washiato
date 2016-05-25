package arya.com.washiato.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import arya.com.washiato.Cluster;
import arya.com.washiato.ClusterStatusAdapter;
import arya.com.washiato.ControlActivity;
import arya.com.washiato.Machine;
import arya.com.washiato.R;
import arya.com.washiato.TabActivity;

public class WasherFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    TextView vAlarm;
    Button bAlarmOn;
    Button bAlarmOff;

    public static ClusterStatusAdapter washerStatusAdapter;

    public WasherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_washer, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_washer_cluster);
        washerStatusAdapter = new ClusterStatusAdapter(getActivity(), R.layout.cluster_status, TabActivity.washerList);
        if(washerStatusAdapter == null) Log.i(TAG,"shit be null");
        else {
            listView.setAdapter(washerStatusAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                    Log.i(TAG, "longclicked");
                    return false;
                }
            });
            washerStatusAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    public static void updateWasherList() {
        washerStatusAdapter.notifyDataSetChanged();
    }



    /*private static final String TAG = "SettingsFragment";
    public static Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    public ArrayList<Machine> washerList;
    public static String clusterName;
    public Map<String, Object> clusterMap;
    public ClusterStatusAdapter washerAdapter;
    public Cluster cluster = new Cluster();
    public String currclusterName;
    //final Fragment fragment = this; //Set context
    static ChildEventListener clusterMachineListener;
    static ValueEventListener clusterStatusListener;
    TextView vAlarm;
    Button bAlarmOn;
    Button bAlarmOff;

    TextView text_cluster_name;
    TextView text_cluster_location;
    TextView text_cluster_washers_available;

    public WasherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_washer, container, false);

        vAlarm = (TextView) rootView.findViewById(R.id.ble_status);
        //bAlarmOn = (Button) rootView.findViewById(R.id.alarmOn);
        //bAlarmOff = (Button) rootView.findViewById(R.id.alarmOff);

        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

        washerList = new ArrayList<>();
        //check to see if there's a default cluster. if so, get it
        if (ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus() == false) {
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
                    //cluster.setNumDry((int) (long) clusterMap.get("numDry"));
                    cluster.setNumWash((int) (long) clusterMap.get("numWash"));
                    cluster.setMachines((ArrayList<String>) clusterMap.get("machines"));
                    //update all the text views
                    updateCluster();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

        } else if (ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus() == true) {
            //cluster is different from default cluster
            currclusterName = (String) ControlActivity.thisUser.get("CurrCluster");
            clusterName = currclusterName;
            Log.i(TAG, clusterName);
            Log.i(TAG, "different cluster from default! getting data from " + clusterName);

            clusterStatusListener = ref.child("Clusters").child(clusterName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Store this in the cluster class
                    clusterMap = (Map<String, Object>) dataSnapshot.getValue();
                    Log.i(TAG, (String) clusterMap.get("location"));
                    cluster.setLocation((String) clusterMap.get("location"));
                    //cluster.setNumDry((int) (long) clusterMap.get("numDry"));
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

        ListView listView = (ListView) rootView.findViewById(R.id.listview_washer);
        washerAdapter = new ClusterStatusAdapter(getActivity(), R.layout.fragment_washer, washerList);
        listView.setAdapter(washerAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                Log.i(TAG, "longclicked");
                return false;
            }
        });
        washerAdapter.notifyDataSetChanged();

    public void updateCluster() {
        Log.i(TAG, "updating all the cluster shit");
        text_cluster_name.setText(clusterName + ", ");
        text_cluster_location.setText(cluster.getLocation());
        //text_cluster_dryers_available.setText(Integer.toString(cluster.getNumDry()) + " dryers available");
        text_cluster_washers_available.setText(Integer.toString(cluster.getNumWash()) + " washers available");

        //this is here to prevent the machine from updating before the cluster in the event of a cluster change
//
        //now that the cluster is ready, populate the machines
        Query queryRef = ref.child("Machines").orderByChild("localCluster").equalTo(clusterName);
        clusterMachineListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                System.out.println(dataSnapshot.getKey());
                Machine machine = new Machine();
                Map map = (Map<String, Object>) dataSnapshot.getValue();
                if (map == null) Log.i(TAG, "fucker is null");
                machine.setLocalCluster((String) map.get("localCluster"));
                machine.setName((String) map.get("name"));
                machine.setStatus((int) (long) map.get("status"));
                machine.setWasher((boolean) map.get("washer"));
                machine.setTime((int) (long) map.get("time"));
                machine.setOmw((boolean) map.get("omw"));
                washerList.add(machine);
                SortMachines();
                washerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map map = (Map<String, Object>) dataSnapshot.getValue();
                if (map == null) Log.i(TAG, "fucker is null");
                for (int i = 0; i < washerList.size(); i++) {
                    //look for the one that has changed in washerList
                    Machine machine = washerList.get(i);
                    if (machine.getName().equals(map.get("name"))) {
                        //update all the info
                        machine.setLocalCluster((String) map.get("localCluster"));
                        machine.setName((String) map.get("name"));
                        machine.setStatus((int) (long) map.get("status"));
                        machine.setWasher((boolean) map.get("washer"));
                        machine.setTime((int) (long) map.get("time"));
                        machine.setOmw((boolean) map.get("omw"));
                        washerList.remove(i); //remove the one that's in there
                        washerList.add(machine); //add our updated one
                        SortMachines(); //resort
                        washerAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map map = (Map<String, Object>) dataSnapshot.getValue();
                for (int i = 0; i < washerList.size(); i++) {
                    Machine machine = washerList.get(i);
                    if (machine.getName().equals(map.get("name"))) {
                        washerList.remove(i); //remove the one that's in there
                        washerAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void SortMachines() {
        if (washerList.size() > 1) {
            Log.i(TAG, "sorting");
            Collections.sort(washerList, new Comparator<Machine>() {
                @Override
                public int compare(Machine m1, Machine m2) {
                    return m1.status - m2.status;
                }
            });
        }
    }

    public static void endClusterListeners() {
        Log.i("ClusterActivity", "removing listeners");
        if (clusterMachineListener != null) ref.removeEventListener(clusterMachineListener);
        if (clusterStatusListener != null) ref.removeEventListener(clusterStatusListener);
    }


    public void updateUIAlarmStatus(final boolean alarmStatus) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (alarmStatus) {
                    vAlarm.setText("Washer On");
                } else {
                    vAlarm.setText("Washer Off");
                }
            }
        });
    }
    }*/
}
