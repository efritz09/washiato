package arya.com.washiato;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.vstechlab.easyfonts.EasyFonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import arya.com.washiato.fragments.DryerFragment;
import arya.com.washiato.fragments.WasherFragment;

public class TabActivity extends AppCompatActivity {

    private static final String TAG = ControlActivity.class.getSimpleName();

    public static Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    public static Cluster cluster = new Cluster();
    public Map<String,Object> clusterMap;
    public String currclusterName;
    public static String clusterName;

    public static ArrayList<Machine> dryerList;
    public static ArrayList<Machine> washerList;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    static ChildEventListener clusterMachineListener;
    static ValueEventListener clusterStatusListener;

    TextView text_cluster_name;
    TextView text_cluster_location;
    TextView text_cluster_washers_available;
    TextView text_cluster_dryers_available;
//    private int[] tabIcons = { //TODO: Add our icons here
//            R.drawable.icon_worksite_select,
//            R.drawable.icon_settings
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        text_cluster_dryers_available = (TextView) findViewById(R.id.text_cluster_dryers_available);
        text_cluster_dryers_available.setTypeface(EasyFonts.robotoRegular(this));
        text_cluster_washers_available = (TextView) findViewById(R.id.text_cluster_washers_available);
        text_cluster_washers_available.setTypeface(EasyFonts.robotoRegular(this));
        text_cluster_location = (TextView) findViewById(R.id.text_cluster_location);
        text_cluster_location.setTypeface(EasyFonts.robotoBold(this));
        text_cluster_name = (TextView) findViewById(R.id.text_cluster_name);
        text_cluster_name.setTypeface(EasyFonts.robotoBold(this));

        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

//        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
//        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

        dryerList = new ArrayList<>();
        washerList = new ArrayList<>();
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

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WasherFragment(), "WASHERS");
        adapter.addFragment(new DryerFragment(), "DRYERS");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /*
    creates the cluster listener. Only gathers the machine list and the location
     */
    public void setUpClusterListener(String name) {
        /*if(clusterStatusListener != null) {
            Log.i(TAG,"cluster listener exists");
            return;
        }*/
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
/*        if(clusterMachineListener != null) {
            Log.i(TAG,"machine listener exists");
            return;
        }*/
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
                if(machine.getWasher()) {
                    washerList.add(machine);
                    SortMachines(true);
                    updateOpenMachines(true);
                    WasherFragment.updateWasherList();
                }else {
                    dryerList.add(machine);
                    SortMachines(false);
                    updateOpenMachines(false);
                    DryerFragment.updateDryerList();
                }
//                machineList.add(machine);
//                SortMachines();
//                updateOpenMachines();
//                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                Log.i(TAG,"machine child changed");
                if(map == null) Log.i(TAG,"fucker is null");
                if((boolean)map.get("washer")) {
                    for (int i = 0; i < washerList.size(); i++) {
                        //look for the one that has changed in machineList
                        Machine machine = washerList.get(i);
                        if (machine.getName().equals(map.get("name"))) {
                            //update all the info
                            machine.setLocalCluster((String) map.get("localCluster"));
                            machine.setName((String) map.get("name"));
                            machine.setStatus((int) (long) map.get("status"));
                            machine.setWasher((boolean) map.get("washer"));
                            machine.setTime((int) (long) map.get("time"));
                            machine.setOmw((boolean) map.get("omw"));
                            washerList.remove(i);
                            washerList.add(machine);
                            SortMachines(true);
                            updateOpenMachines(true);
                            WasherFragment.updateWasherList();
//                            machineList.remove(i); //remove the one that's in there
//                            machineList.add(machine); //add our updated one
//                            SortMachines(); //resort
//                            updateOpenMachines();
//                            statusAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }else {
                    for(int i = 0; i < dryerList.size(); i++) {
                        //look for the one that has changed in machineList
                        Machine machine = dryerList.get(i);
                        if(machine.getName().equals(map.get("name"))) {
                            //update all the info
                            machine.setLocalCluster((String)map.get("localCluster"));
                            machine.setName((String)map.get("name"));
                            machine.setStatus((int)(long)map.get("status"));
                            machine.setWasher((boolean)map.get("washer"));
                            machine.setTime((int)(long)map.get("time"));
                            machine.setOmw((boolean)map.get("omw"));
                            dryerList.remove(i);
                            dryerList.add(machine);
                            SortMachines(false);
                            updateOpenMachines(false);
                            DryerFragment.updateDryerList();
                            break;
                        }
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map map = (Map<String,Object>) dataSnapshot.getValue();
                Log.i(TAG,"machine child removed");
                for(int i = 0; i < washerList.size(); i++) {
                    Machine machine = washerList.get(i);
                    if(machine.getName().equals(map.get("name"))) {
                        washerList.remove(i); //remove the one that's in there
                        WasherFragment.updateWasherList();
                        updateOpenMachines(true);
                        break;
                    }
                }
                for(int i = 0; i < dryerList.size(); i++) {
                    Machine machine = dryerList.get(i);
                    if(machine.getName().equals(map.get("name"))) {
                        dryerList.remove(i); //remove the one that's in there
                        DryerFragment.updateDryerList();
                        updateOpenMachines(false);
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

    public void updateCluster() {
        Log.i(TAG, "updating all the cluster shit");
        if(text_cluster_name == null)Log.i(TAG,"nulllllll");
        if(text_cluster_location == null)Log.i(TAG,"nulllllll");
        text_cluster_name.setText(clusterName);
        text_cluster_location.setText(cluster.getLocation());
        updateOpenMachines(true);
        updateOpenMachines(false);
    }

    /*
    updates the number of open and finished machines
     */
    public void updateOpenMachines(boolean wash) {
        int open = 0;
        int finished = 0;
        ArrayList<Machine> machineList;
        if(wash) machineList = washerList;
        else machineList = dryerList;

        // update the displayed number of washers and dryers available
        for(int i = 0; i < machineList.size(); i++) {
            Machine thisMachine = machineList.get(i);
            if(thisMachine.status == 0) {
                open++;
            }else if(thisMachine.status == 1) {
                finished++;
            }
        }
//        String plural1 = "s";
//        String plural2 = "s";
        if(wash) {
//            Log.i(TAG,"num wash = " + open + "; fin wash = "+ finished);
            cluster.setNumWash(open);
            cluster.setFinWash(finished);
//            if(open == 1) plural1 = "";
//            if(finished == 1) plural2 = "";
            text_cluster_washers_available.setText("Washers: "+open+" open, "+finished+" finished");
//            text_cluster_washers_available.setText(open+" washer"+plural1+" open, "+finished+" washer"+plural2+" finished");
        }else {
//            Log.i(TAG,"num dry = " + open + "; fin dry = "+ finished);
            cluster.setNumDry(open);
            cluster.setFinDry(finished);
//            if(open == 1) plural1 = "";
//            if(finished == 1) plural2 = "";
            text_cluster_dryers_available.setText("Dryers: "+open+" open, "+finished+" finished");
//            text_cluster_dryers_available.setText(open+" dryer"+plural1+" open, "+finished+" dryer"+plural2+" finished");
        }
    }
    /*
    sorts the machines by the status
     */
    public void SortMachines(boolean wash) {
        if(wash) {
            if (washerList.size() > 1) {
//                Log.i(TAG, "sorting");
                Collections.sort(washerList, new Comparator<Machine>() {
                    @Override
                    public int compare(Machine m1, Machine m2) {
                        return m1.status - m2.status;
                    }
                });
            }
        } else {
            if (dryerList.size() > 1) {
//                Log.i(TAG, "sorting");
                Collections.sort(dryerList, new Comparator<Machine>() {
                    @Override
                    public int compare(Machine m1, Machine m2) {
                        return m1.status - m2.status;
                    }
                });
            }
        }
    }

    public static void endClusterListeners() {
        Log.i("ClusterActivity","removing listeners");
        if(clusterMachineListener != null) ref.removeEventListener(clusterMachineListener);
        if(clusterStatusListener != null) ref.removeEventListener(clusterStatusListener);
        clusterStatusListener = null;
        clusterMachineListener = null;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() event");
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
