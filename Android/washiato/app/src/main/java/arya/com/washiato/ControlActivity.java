package arya.com.washiato;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.app.PendingIntent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.realtime.util.StringListReader;
import com.vstechlab.easyfonts.EasyFonts;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ControlActivity extends AppCompatActivity {

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
    public Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final String TAG = "ControlActivity";
    String defClus;
    TextView text_user;
    TextView text_cluster;
    TextView text_cluster_current;
    TextView text_time;
    TextView text_machine;
    TextView text_machine_status;
    Button button_nfcOn;
    EditText editText_machine_name;
    Button button_machine_select;
    public static Map thisUser;
    public Map thisMachine;
    public static boolean is_nfc_detected = false;
    public static String serial;
    final Context context = this; //Set context
    ValueEventListener user_listener;
    ValueEventListener machine_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        text_user = (TextView) findViewById(R.id.text_user);
        text_user.setTypeface(EasyFonts.robotoBlack(this));
        text_cluster = (TextView) findViewById(R.id.text_cluster);
        text_user.setTypeface(EasyFonts.robotoThin(this));
        text_cluster_current = (TextView) findViewById(R.id.text_cluster_current);
        text_user.setTypeface(EasyFonts.robotoThin(this));
        text_time = (TextView) findViewById(R.id.text_time);
        text_time.setTypeface(EasyFonts.robotoLightItalic(this));
        text_machine = (TextView) findViewById(R.id.text_machine);
        text_machine.setTypeface(EasyFonts.robotoBold(this));
        text_machine_status = (TextView) findViewById(R.id.text_machine_status);
        text_machine_status.setTypeface(EasyFonts.robotoBlack(this));

        button_nfcOn = (Button)findViewById(R.id.button_nfc);
        editText_machine_name = (EditText)findViewById(R.id.edit_machine_id);
        button_machine_select = (Button)findViewById(R.id.button_select_machine);

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
        if(ref.getAuth() == null) logOut(null);

        if(ref.getAuth().getProvider().equals("anonymous")) {
            Log.i(TAG,"anonymous user");
            text_cluster.setText("");
            text_user.setText("Welcome Anonymous User!");
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
                    text_user.setText("Welcome " + (String) thisUser.get("UserName"));
                    if (thisUser.containsKey("defaultCluster")) { //if default cluster already exists, set textview
                        Log.i(TAG, "previous cluster exists");
                        defClus = (String) thisUser.get("defaultCluster");
                        text_cluster.setText("Home Cluster: " + defClus);
                    } else { //else textview is blank
                        text_cluster.setText("Home Cluster: " + "Not set");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

    }


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
            setMachineListener();

        }
    }

    public void setMachineListener() {
        Log.i(TAG,"setting up machine listener");
        //create a listener for changes in the system
        machine_listener = ref.child("Machines").child(serial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(ref.getAuth() == null) return;
                thisMachine = (Map<String, String>) dataSnapshot.getValue();
                if(thisMachine == null) {
                    Log.i(TAG, "Unrecognized NFC tag scanned: " + serial);
                    Toast.makeText(context, "No machine with this serial number found.", Toast.LENGTH_LONG).show();
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
                    text_cluster.setText("Cluster: " + cluster);
                    text_cluster_current.setText("");
                }
                else if(thisUser.containsKey("defaultCluster")){//if default cluster already exists
                    if(!cluster.equals(defClus)){ //AND cluster triggered by NFC is not same as default, ask user to change
                        Log.i(TAG,"cluster = " + cluster + ", def = " + defClus);


                        AlertDialog.Builder alertbox = new AlertDialog.Builder(ControlActivity.this);
                        alertbox.setTitle("New Home?");
                        alertbox.setMessage("Set " + cluster + " as your home cluster?");
                        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //set this as default
                                ref.child("Users").child(ref.getAuth().getUid()).child("defaultCluster").setValue(cluster); //current cluster to Firebase
                                ref.child("Users").child(ref.getAuth().getUid()).child("CurrCluster").setValue(cluster); //current cluster to Firebase
                                text_cluster.setText("Home Cluster: " + cluster );
                                text_cluster_current.setText("");
                            }
                        });
                        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ref.child("Users").child(ref.getAuth().getUid()).child("CurrCluster").setValue(cluster); //current cluster to Firebase
                                text_cluster_current.setText("Current Cluster: " + cluster );
                            }
                        });
                        alertbox.show();
                    }
                    else text_cluster_current.setText("");

                }
                else { //NO default cluster set yet, hence add to Firebase and display in Control Activity
                    ref.child("Users").child(ref.getAuth().getUid()).child("defaultCluster").setValue(cluster);
                    text_cluster.setText("Home Cluster: " + cluster);
                    text_cluster_current.setText("");
                }
                //set the machine name:
                text_machine.setText((String)thisMachine.get("name") + " ");
                //update the shit with statuses
                int status = (int)(long)thisMachine.get("status");
                boolean washer = (boolean)thisMachine.get("washer");
                Button button = (Button)findViewById(R.id.button_omw);
                if(status == 0) {
                    Log.i(TAG,"machine is open");
                    text_machine_status.setText(R.string.machine_open_flavortext);
                    text_machine_status.setTextColor(getResources().getColor(R.color.green));
                    text_machine.setTextColor(getResources().getColor(R.color.green));
                    text_time.setText("");
                    if(button != null) button.setVisibility(View.INVISIBLE);
                } else if(status == 1) {
                    Log.i(TAG,"machine is finished");
                    if(washer) text_machine_status.setText(R.string.wash_finished_flavortext);
                    else text_machine_status.setText(R.string.dry_finished_flavortext);
                    text_machine_status.setTextColor(getResources().getColor(R.color.gold));
                    text_machine.setTextColor(getResources().getColor(R.color.gold));
                    text_time.setText(Integer.toString((int)(long)thisMachine.get("time")) + " minutes ago");
                    //set up button
                    if(button != null) button.setVisibility(View.VISIBLE);
                    //only create notification if omw is false. Prevents setting the omw from buzzing the user
                    if(!(boolean)thisMachine.get("omw")) createNotification();

                } else if(status == 2) {
                    Log.i(TAG,"machine is running");
                    if(washer) text_machine_status.setText(R.string.wash_running_flavortext);
                    else text_machine_status.setText(R.string.dry_running_flavortext);
                    text_machine_status.setTextColor(getResources().getColor(R.color.red));
                    text_machine.setTextColor(getResources().getColor(R.color.red));
                    text_time.setText("");
                    if(button != null) button.setVisibility(View.INVISIBLE);
                }
                else Log.i(TAG,"Somehow we have a status issue");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG,"cancelled");
            }
        });
    }

    //launches Cluster activity
    public void launchCluster(View view) {
        Log.i(TAG,"starting cluster activity");
//        Intent intent = new Intent(this, ClusterActivity.class);
//        startActivity(intent);
        Intent intent = new Intent(this, TabActivity.class);
        startActivity(intent);
    }

    //launches Tab activity
    public void launchTab(View view) {
        Log.i(TAG,"starting tab activity");
        Intent intent = new Intent(this, TabActivity.class);
        startActivity(intent);
    }

    //function implemented when "I'm on my way" button is pressed; updates omw variable under Machines in Firebase
    public void omw(View view) {
        Log.i(TAG,"OMW");
        if(getNfcStatus()==true){
            ref.child("Machines").child(serial).child("omw").setValue(true); //change omw variable under Machines in Firebase
            Toast.makeText(context, getString(R.string.omw), Toast.LENGTH_LONG).show(); //show toast for OMW
        }
        else {
            Toast.makeText(context, getString(R.string.no_connection), Toast.LENGTH_LONG).show(); //show toast for OMW
        }
    }

    public void logOut(View view) {
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

    // a test function for later implementation
/*    public void setMachineInfo(String cluster, String tempCluster, String name, int status, boolean washer) {
        if(!cluster.equals("")) {

        }
        if(!tempCluster.equals("")) {

        }
        if(!name.equals("")) {

        }
        if(status == 0) {

        }else if(status == 1) {

        }else if(status == 2) {

        }
    }*/

    /*
    checkNFCon: Checks if NFC is on. Asks user to turn it on, opens the NFC settings
     */
    public void checkNFCon () {
        android.nfc.NfcAdapter mNfcAdapter= android.nfc.NfcAdapter.getDefaultAdapter(ControlActivity.this);
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
            AlertDialog.Builder alertbox = new AlertDialog.Builder(ControlActivity.this);
            alertbox.setTitle("Enable NFC");
            alertbox.setMessage(R.string.alert_nfc_on_message);
            alertbox.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setNFCvisuals(true);
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
                    final AlertDialog.Builder noNFC = new AlertDialog.Builder(ControlActivity.this);
                    noNFC.setMessage(R.string.alert_nfc_on_message_repeat);
                    noNFC.setNegativeButton("That's fine", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //show the Turn NFC on button and editText for manual input
                            setNFCvisuals(false);
                        }
                    });
                    noNFC.setPositiveButton("Turn NFC On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNFCvisuals(true);
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
                            setNFCvisuals(false);
                        }
                    });
                    noNFC.show();
                }
            });
            alertbox.show();

        } else { //NFC is enable. Disable all the other stuff
            setNFCvisuals(true);
        }
    }

    public void ConnectMachine(View view) {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        //find the name of the machine and connect to it
        final String machineName = editText_machine_name.getText().toString();
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
                            serial = entry.getKey();
                            setMachineListener();
                            return;
                        }
                    }
                }
                Log.i(TAG,"couldn't find it...");
                Toast.makeText(context, "No machine with this name was found.", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        if(defClus != null) {//grab the cluster data and search for the machine name
            ref.child("Clusters").child(defClus).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }

    }

    private void setNFCvisuals(boolean on) {
        if(on) {
            button_nfcOn.setVisibility(View.GONE);
//            editText_machine_name.setVisibility(View.GONE);
//            editText_machine_name.setHint("");
//            button_machine_select.setVisibility(View.GONE);
        }else {
            button_nfcOn.setVisibility(View.VISIBLE);
//            editText_machine_name.setVisibility(View.VISIBLE);
//            editText_machine_name.setHint("Enter Machine Name");
//            button_machine_select.setVisibility(View.VISIBLE);
        }
    }

    public void launchNFC(View view) {
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

    /* notification area */
    public void createNotification() {
        long[] pattern = {0,100,100,100,250,500};
        Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern,-1);
//        v.vibrate(500);
        Log.i(TAG,"setting up notification");
        Bitmap icon;
        String title;
        if((boolean)thisMachine.get("washer")) {
            icon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.wm_finished);
            title = "Your Laundry is washed!";
        }else {
            icon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.dry_finished);
            title = "Your Laundry is dry!";
        }


        final String time = DateFormat.getTimeInstance().format(new Date()).toString();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_wm_icon)
                        .setLargeIcon(icon)
                        .setContentTitle(title)
                        .setContentText("Completed at " + time)
                        .setAutoCancel(true)
                        .setLights(Color.parseColor("#FFFFFFFF"),1000,1000); //doesn't work
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ControlActivity.class);
        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ControlActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

}
