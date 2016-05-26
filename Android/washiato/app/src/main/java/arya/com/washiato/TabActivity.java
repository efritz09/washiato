package arya.com.washiato;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.vstechlab.easyfonts.EasyFonts;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arya.com.washiato.fragments.DryerFragment;
import arya.com.washiato.fragments.WasherFragment;
import arya.com.washiato.fragments.ControlFragment;

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

/*----- from control activity -----*/
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    ValueEventListener user_listener;
    static ValueEventListener machine_listener;
    public static Map thisUser;
    public static Map thisMachine;
    static String defClus;
//    TextView text_user;
//    TextView text_cluster;
//    TextView text_cluster_current;
//    TextView text_time;
//    TextView text_machine;
//    TextView text_machine_status;
//    Button button_nfcOn;
//    EditText editText_machine_name;
//    Button button_machine_select;
    public static boolean is_nfc_detected = false;
    public static String serial;
    final Context context = this; //Set context


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        text_cluster_dryers_available = (TextView) findViewById(R.id.text_cluster_dryers_available);
        text_cluster_dryers_available.setTypeface(EasyFonts.robotoThin(this));
        text_cluster_washers_available = (TextView) findViewById(R.id.text_cluster_washers_available);
        text_cluster_washers_available.setTypeface(EasyFonts.robotoThin(this));
        text_cluster_location = (TextView) findViewById(R.id.text_cluster_location);
        text_cluster_location.setTypeface(EasyFonts.robotoBold(this));
        text_cluster_name = (TextView) findViewById(R.id.text_cluster_name);
        text_cluster_name.setTypeface(EasyFonts.robotoBold(this));

        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

        checkPermissions(this); //check the permissions

        //check to see if we should ask about NFC (first see if NFC is supported)
        if(washiato.preferences != null) {
            if(washiato.preferences.getBoolean(getString(R.string.nfc_supported),true)
                    && !washiato.preferences.getBoolean(getString(R.string.pref_nfc),false)) {
                Log.i(TAG, "asking about NFC");
                checkNFCon(); //check to see if NFC is on
            }
        }

        //ensure we're properly logged in
        if(ref.getAuth() == null) LogOut(null);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ControlFragment(), "CONTROL");
        adapter.addFragment(new WasherFragment(), "WASHERS");
        adapter.addFragment(new DryerFragment(), "DRYERS");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

/////////////////////////START HERE FOR TEXTVIEW SHIT///////////////////////////////////////

        if(ref.getAuth().getProvider().equals("anonymous")) {
            Log.i(TAG,"anonymous user");
//            text_cluster.setText("");
//            text_user.setText("Welcome Anonymous User!");
            thisUser = new HashMap();
            thisUser.put("UserName","anonymous");
        }else {
            //check to see if this user has used this shit before:
            user_listener =  ref.child("Users").child(ref.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(ref.getAuth() == null) return;
                    Log.i(TAG, "Checking for previous info");
                    thisUser = (Map<String, String>) dataSnapshot.getValue();
//                    text_user.setText("Welcome " + (String) thisUser.get("UserName"));
                    if (thisUser.containsKey("defaultCluster")) { //if default cluster already exists, set textview
                        Log.i(TAG, "previous cluster exists");
                        defClus = (String) thisUser.get("defaultCluster");
//                        text_cluster.setText("Home Cluster: " + defClus);
                    } else { //else textview is blank
//                        text_cluster.setText("Home Cluster: " + "Not set");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

        dryerList = new ArrayList<>();
        washerList = new ArrayList<>();
        if(ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus()== false) {
            //cluster exists; pull this data
            clusterName = (String) ControlActivity.thisUser.get("defaultCluster");
            Log.i(TAG, "cluster exists! getting data from " + clusterName);
            setUpClusterListener(clusterName);
            setUpMachineListener();
        }else if (ControlActivity.thisUser.containsKey("defaultCluster") && ControlActivity.getNfcStatus()== true){
            //cluster is different from default cluster
            currclusterName = (String) ControlActivity.thisUser.get("CurrCluster");
            clusterName = currclusterName;
            Log.i(TAG, "different cluster from default! getting data from " + clusterName);
            setUpClusterListener(clusterName);
            setUpMachineListener();
        }
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
        text_cluster_name.setText(clusterName + ", ");
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
        String plural1 = "s";
        String plural2 = "s";
        if(wash) {
            Log.i(TAG,"num wash = " + open + "; fin wash = "+ finished);
            cluster.setNumWash(open);
            cluster.setFinWash(finished);
            if(open == 1) plural1 = "";
            if(finished == 1) plural2 = "";
            text_cluster_washers_available.setText(open+" washer"+plural1+" open, "+finished+" washer"+plural2+" finished");
        }else {
            Log.i(TAG,"num dry = " + open + "; fin dry = "+ finished);
            cluster.setNumDry(open);
            cluster.setFinDry(finished);
            if(open == 1) plural1 = "";
            if(finished == 1) plural2 = "";
            text_cluster_dryers_available.setText(open+" dryer"+plural1+" open, "+finished+" dryer"+plural2+" finished");
        }
//        Log.i(TAG,"fin wash = " + finishedWashers + "; fin dry = "+finishedDryers);
//        cluster.setNumWash(openWashers);
//        cluster.setNumDry(openDryers);
//        cluster.setFinWash(finishedWashers);
//        cluster.setFinDry(finishedDryers);
//        String plural1 = "s";
//        String plural2 = "s";
//        if(openWashers == 1) plural1 = "";
//        if(finishedWashers == 1) plural2 = "";
//        text_cluster_washers_available.setText(openWashers+" washer"+plural1+" open, "+finishedWashers+" washer"+plural2+" finished");
//        if(openDryers != 1) plural1 = "s";
//        if(finishedDryers != 1) plural2 = "s";
//        text_cluster_dryers_available.setText(openDryers+" dryer"+plural1+" open, "+finishedDryers+" dryer"+plural2+" finished");
    }

    /*
    sorts the machines by the status
     */
    public void SortMachines(boolean wash) {
        if(wash) {
            if (washerList.size() > 1) {
                Log.i(TAG, "sorting");
                Collections.sort(washerList, new Comparator<Machine>() {
                    @Override
                    public int compare(Machine m1, Machine m2) {
                        return m1.status - m2.status;
                    }
                });
            }
        } else {
            if (dryerList.size() > 1) {
                Log.i(TAG, "sorting");
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



    /*----- from control activity -----*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "LOCATION granted");
                }else {
                    Log.i(TAG, "LOCATION denied");
                }
            }
            //add more case statements if we need other info
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(washiato.preferences.getBoolean(getString(R.string.nfc_supported),false)) {
            // creating pending intent:
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // creating intent receiver for NFC events:
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            // enabling foreground dispatch for getting intent from NFC event:
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // disabling foreground dispatch:
        if(washiato.preferences.getBoolean(getString(R.string.nfc_supported),false)) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction() == null) Log.i(TAG,"null action");
        else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            //Get serial number from NFC tag and convert to String
            serial = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            //Display NFC serial number
//            ((TextView)findViewById(R.id.text_nfc_serial)).setText("NFC Tag\n" + serial);

            //Access AuthData object created during login
            AuthData authData = ref.getAuth();
            //Push to Firebase (temporarily)
            ref.child("Users").child(authData.getUid()).child("Washer NFC Serial").setValue(serial);
            setMachineListener(context);
            Log.i(TAG,"nfc tag = " + serial);
        }
    }

/////////////////////////START HERE FOR TEXTVIEW SHIT///////////////////////////////////////
    public static void setMachineListener(Context thiscont) {
        final Context cont = thiscont;
        Log.i(TAG,"setting up machine listener");
        //create a listener for changes in the system
        machine_listener = ref.child("Machines").child(serial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(ref.getAuth() == null) return;
                thisMachine = (Map<String, String>) dataSnapshot.getValue();
                if(thisMachine == null) {
                    Log.i(TAG, "Unrecognized NFC tag scanned: " + serial);
                    Toast.makeText(cont, "No machine with this serial number found.", Toast.LENGTH_LONG).show();
//                        ((TextView)findViewById(R.id.text_nfc_serial)).setText("NFC Tag\n" + serial + "\n(Not found in database)");
                    return;
                }
                is_nfc_detected = true; // update nfc check variable (only for registered machines)
                final String cluster = (String) thisMachine.get("localCluster");
                Log.i(TAG, "found cluster: " + cluster);
                //first handle the anonymous user case
                if(ref.getAuth().getProvider().equals("anonymous")) {
                    thisUser.put("CurrCluster",cluster);
                    thisUser.put("defaultCluster",cluster);
//                    text_cluster.setText("Cluster: " + cluster);
//                    text_cluster_current.setText("");
                }
                else if(thisUser.containsKey("defaultCluster")){//if default cluster already exists
                    if(!cluster.equals(defClus)){ //AND cluster triggered by NFC is not same as default, ask user to change
                        Log.i(TAG,"cluster = " + cluster + ", def = " + defClus);


                        AlertDialog.Builder alertbox = new AlertDialog.Builder(cont);
                        alertbox.setTitle("New Home?");
                        alertbox.setMessage("Set " + cluster + " as your home cluster?");
                        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //set this as default
                                ref.child("Users").child(ref.getAuth().getUid()).child("defaultCluster").setValue(cluster); //current cluster to Firebase
                                ref.child("Users").child(ref.getAuth().getUid()).child("CurrCluster").setValue(cluster); //current cluster to Firebase
//                                text_cluster.setText("Home Cluster: " + cluster );
//                                text_cluster_current.setText("");
                            }
                        });
                        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ref.child("Users").child(ref.getAuth().getUid()).child("CurrCluster").setValue(cluster); //current cluster to Firebase
//                                text_cluster_current.setText("Current Cluster: " + cluster );
                            }
                        });
                        alertbox.show();
                    }
//                    else text_cluster_current.setText("");

                }
                else { //NO default cluster set yet, hence add to Firebase and display in Control Activity
                    ref.child("Users").child(ref.getAuth().getUid()).child("defaultCluster").setValue(cluster);
//                    text_cluster.setText("Home Cluster: " + cluster);
//                    text_cluster_current.setText("");
                }
                //set the machine name:
//                text_machine.setText((String)thisMachine.get("name") + " ");
                //update the shit with statuses
                int status = (int)(long)thisMachine.get("status");
                boolean washer = (boolean)thisMachine.get("washer");
//                Button button = (Button)findViewById(R.id.button_omw);
                if(status == 0) {
                    Log.i(TAG,"machine is open");
//                    text_machine_status.setText(R.string.machine_open_flavortext);
//                    text_machine_status.setTextColor(getResources().getColor(R.color.green));
//                    text_machine.setTextColor(getResources().getColor(R.color.green));
//                    text_time.setText("");
//                    if(button != null) button.setVisibility(View.INVISIBLE);
                } else if(status == 1) {
                    Log.i(TAG,"machine is finished");
//                    if(washer) text_machine_status.setText(R.string.wash_finished_flavortext);
//                    else text_machine_status.setText(R.string.dry_finished_flavortext);
//                    text_machine_status.setTextColor(getResources().getColor(R.color.gold));
//                    text_machine.setTextColor(getResources().getColor(R.color.gold));
//                    text_time.setText(Integer.toString((int)(long)thisMachine.get("time")) + " minutes ago");
                    //set up button
//                    if(button != null) button.setVisibility(View.VISIBLE);
                    //only create notification if omw is false. Prevents setting the omw from buzzing the user
                    if(!(boolean)thisMachine.get("omw")) createNotification(cont);

                } else if(status == 2) {
                    Log.i(TAG,"machine is running");
//                    if(washer) text_machine_status.setText(R.string.wash_running_flavortext);
//                    else text_machine_status.setText(R.string.dry_running_flavortext);
//                    text_machine_status.setTextColor(getResources().getColor(R.color.red));
//                    text_machine.setTextColor(getResources().getColor(R.color.red));
//                    text_time.setText("");
//                    if(button != null) button.setVisibility(View.INVISIBLE);
                }
                else Log.i(TAG,"Somehow we have a status issue");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG,"cancelled");
            }
        });
    }

    public void LogOut(View view) {
        //log the user out
        washiato.preferencesEditor = washiato.preferences.edit();
        washiato.preferencesEditor.putBoolean(getString(R.string.pref_logged_in),false);
        washiato.preferencesEditor.putString(getString(R.string.pref_user_id), null);
        washiato.preferencesEditor.commit();
        ref.unauth();
        Log.i(TAG,"logging out...");
        if(user_listener != null) ref.removeEventListener(user_listener);
        if(machine_listener != null) ref.removeEventListener(machine_listener);

        //remove listeners in the cluster
        TabActivity.endClusterListeners();
        is_nfc_detected = false;
        //open the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void LaunchNFC(View view) {
        Log.i(TAG, "resetting NFC settings");
        washiato.preferencesEditor = washiato.preferences.edit();
        washiato.preferencesEditor.putBoolean(getString(R.string.pref_nfc), false);
        washiato.preferencesEditor.apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        }
        checkNFCon();
    }


    public void checkPermissions( Activity thisActivity) {
        // Here, thisActivity is the current activity, maybe in case we need to call this from other activities
        Log.i(TAG, "Checking permissions...");
        if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG,"Location 'should we show an explanation'");
            }else {
                Log.i(TAG,"Location requesting permission");
                ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }else Log.i(TAG, "Location permission already granted!");
    }

    public void checkNFCon () {
        android.nfc.NfcAdapter mNfcAdapter= android.nfc.NfcAdapter.getDefaultAdapter(TabActivity.this);
        if(mNfcAdapter == null) {
            Log.i(TAG, "This device does not support NFC");
            washiato.preferencesEditor = washiato.preferences.edit();
            washiato.preferencesEditor.putBoolean(getString(R.string.nfc_supported),false);
            washiato.preferencesEditor.apply();
            return;
        }

        washiato.preferencesEditor = washiato.preferences.edit();
        washiato.preferencesEditor.putBoolean(getString(R.string.nfc_supported),true);
        washiato.preferencesEditor.apply();


        if (!mNfcAdapter.isEnabled()) {
            AlertDialog.Builder alertbox = new AlertDialog.Builder(TabActivity.this);
            alertbox.setTitle("Enable NFC");
            alertbox.setMessage(R.string.alert_nfc_on_message);
            alertbox.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    setNFCvisuals(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                    }
                }
            });
            alertbox.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder noNFC = new AlertDialog.Builder(TabActivity.this);
                    noNFC.setMessage(R.string.alert_nfc_on_message_repeat);
                    noNFC.setNegativeButton("That's fine", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //show the Turn NFC on button and editText for manual input
//                            setNFCvisuals(false);
                        }
                    });
                    noNFC.setPositiveButton("Turn NFC On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            setNFCvisuals(true);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    });
                    noNFC.setNeutralButton("Don't show this again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            washiato.preferencesEditor = washiato.preferences.edit();
                            washiato.preferencesEditor.putBoolean(getString(R.string.pref_nfc),true);
                            washiato.preferencesEditor.apply();
                            //show the Turn NFC on button
//                            setNFCvisuals(false);
                        }
                    });
                    noNFC.show();
                }
            });
            alertbox.show();

        } else { //NFC is enable. Disable all the other stuff
//            setNFCvisuals(true);
        }
    }

    public static void ConnectToMachine(String name, Context thiscont) {
        final Context cont = thiscont;
        //find the name of the machine and connect to it
        final String machineName = name;
        Log.i(TAG,"Finding " + machineName);

        ref.child("Machines").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                //loop through all machines to find the one we want
                for (Map.Entry<String, Object> entry : map.entrySet()) {
//                    System.out.println((String)entry.getKey() + "/" + (String)entry.getValue());
                    if(map.containsKey(entry.getKey())) {
                        Map<String,Object> machine = (HashMap<String, Object>)map.get(entry.getKey());
                        if(((String)machine.get("name")).equalsIgnoreCase(machineName)) {
                            Log.i(TAG,"WE FOUND IT!");
//                            defClus = (String)machine.get("localCluster");
//                            SetConnection(entry.getKey());
                            serial = entry.getKey();
                            setMachineListener(cont);
                            return;
                        }
                    }
                }
                Log.i(TAG,"couldn't find it...");
                Toast.makeText(cont, "Unable to connect to " + machineName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


    //There might be a better way to convert to string in java if anyone wants to change this function
    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    //function to check whether nfc has been triggered already
    public static boolean getNfcStatus() {
        if(is_nfc_detected == true){
            Log.i(TAG, "nfc detected");
            return true;
        }
        else {
            Log.i(TAG, "NO nfc");
            return false;
        }
    }


    /* notification area */
    public static void createNotification(Context cont) {
        long[] pattern = {0,100,100,100,250,500};
        Vibrator v = (Vibrator) cont.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern,-1);
        Log.i(TAG,"setting up notification");
        Bitmap icon;
        String title;
        if((boolean)thisMachine.get("washer")) {
            icon = BitmapFactory.decodeResource(cont.getResources(),R.mipmap.wm_finished);
            title = "Your Laundry is washed!";
        }else {
            icon = BitmapFactory.decodeResource(cont.getResources(),R.mipmap.dry_finished);
            title = "Your Laundry is dry!";
        }


        final String time = DateFormat.getTimeInstance().format(new Date()).toString();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(cont)
                        .setSmallIcon(R.drawable.ic_wm_icon)
                        .setLargeIcon(icon)
                        .setContentTitle(title)
                        .setContentText("Completed at " + time)
                        .setAutoCancel(true)
                        .setLights(Color.parseColor("#FFFFFFFF"),1000,1000); //doesn't work
        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, ControlActivity.class);
        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(ControlActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_CANCEL_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }


}



