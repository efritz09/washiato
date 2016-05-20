package arya.com.washiato;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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
    TextView text_machine;
    TextView text_machine_status;
    public static Map thisUser;
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
        text_cluster = (TextView) findViewById(R.id.text_cluster);
        text_cluster_current = (TextView) findViewById(R.id.text_cluster_current);
        text_machine = (TextView) findViewById(R.id.text_machine);
        text_machine_status = (TextView) findViewById(R.id.text_machine_status);

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
            text_user.setText("Anonymous User!");
            thisUser = new HashMap();
            thisUser.put("UserName","anonymous");
        }else {
            //check to see if this user has used this shit before:
            user_listener =  ref.child("Users").child(ref.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Checking for previous info");
                    thisUser = (Map<String, String>) dataSnapshot.getValue();
                    text_user.setText((String) thisUser.get("UserName"));
                    if (thisUser.containsKey("defaultCluster")) { //if default cluster already exists, set textview
                        Log.i(TAG, "previous cluster exists");
                        defClus = (String) thisUser.get("defaultCluster");
                        text_cluster.setText("Default Cluster: " + defClus);
                    } else { //else textview is blank
                        text_cluster.setText("Default Cluster: " + "Not set");
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction() == null) Log.i(TAG,"null action");
        else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            //Get serial number from NFC tag and convert to String
            serial = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            //Display NFC serial number
            ((TextView)findViewById(R.id.text_nfc_serial)).setText("NFC Tag\n" + serial);

            //Access AuthData object created during login
            AuthData authData = ref.getAuth();
            //Push to Firebase (temporarily)
            ref.child("Users").child(authData.getUid()).child("Washer NFC Serial").setValue(serial);


            //create a listener for changes in the system
            machine_listener = ref.child("Machines").child(serial).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map thisMachine = (Map<String, String>) dataSnapshot.getValue();
                    if(thisMachine == null) {
                        Log.i(TAG, "Unrecognized NFC tag scanned: " + serial);
                        Toast.makeText(context, "No machine with this serial number found.", Toast.LENGTH_LONG).show();
                        ((TextView)findViewById(R.id.text_nfc_serial)).setText("NFC Tag\n" + serial + "\n(Not found in database)");
                        return;
                    }
                    is_nfc_detected = true; // update nfc check variable (only for registered machines)
                    String cluster = (String) thisMachine.get("localCluster");
                    Log.i(TAG, "found cluster: " + cluster);
                    if(thisUser.containsKey("defaultCluster")){//if default cluster already exists
                        if(cluster != defClus){ //AND cluster triggered by NFC is not same as default, change textview current cluster
                            text_cluster_current.setText("Current Cluster: " + cluster );
                            ref.child("Users").child(ref.getAuth().getUid()).child("CurrCluster").setValue(cluster); //current cluster to Firebase
                        }
                        else {
                            text_cluster_current.setText("Current Cluster: " + cluster ); //else set current cluster to same as default
                        }
                    }
                    else { //NO default cluster set yet, hence add to Firebase and display in Control Activity
                        if(ref.getAuth().getProvider().equals("anonymous")) {
                            thisUser.put("CurrCluster",cluster);
                            thisUser.put("defaultCluster",cluster);
                            text_cluster.setText("Cluster: " + cluster);
                        }else {
                            ref.child("Users").child(ref.getAuth().getUid()).child("defaultCluster").setValue(cluster);
                            text_cluster.setText("Default Cluster: " + cluster);
                        }
                    }
                    //set the machine name:
                    text_machine.setText((String)thisMachine.get("name") + ": ");
                    //update the shit with statuses
                    //EVENTUALLY ADD PUSH NOTIFICATION STUFF HERE?
                    int status = (int)(long)thisMachine.get("status");
                    if(status == 0) {
                        text_machine_status.setText(R.string.text_machine_open);
                        text_machine_status.setTextColor(getResources().getColor(R.color.green));
                    } else if(status == 1) {
                        text_machine_status.setText(R.string.text_machine_finished);
                        text_machine_status.setTextColor(getResources().getColor(R.color.gold));
                        createNotification();
                    } else if(status == 2) {
                        text_machine_status.setText(R.string.text_machine_running);
                        text_machine_status.setTextColor(getResources().getColor(R.color.red));
                    }
                    else Log.i(TAG,"Somehow we have a status issue");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.i(TAG,"cancelled");
                }
            });
        }
    }

    //launches Cluster activity
    public void launchCluster(View view) {
        Log.i(TAG,"starting cluster activity");
        Intent intent = new Intent(this, ClusterActivity.class);
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
        if(getNfcStatus() == true){ //if NFC pairing is active, the event listeners exist => remove them on logout
            if(user_listener!=null && machine_listener != null){
                ref.removeEventListener(user_listener);
                ref.removeEventListener(machine_listener);
            }
           else if (user_listener != null) {
                ref.removeEventListener(user_listener);
            }
            else if (machine_listener != null) {
                ref.removeEventListener(machine_listener);
            }
        }
        //remove listeners in the cluster
        ClusterActivity.endClusterListeners();
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
                        }
                    });
                    noNFC.setPositiveButton("Turn NFC On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                        }
                    });
                    noNFC.show();
                }
            });
            alertbox.show();

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
        long[] pattern = new long[6]; //pattern is fucky
        pattern[0] = 80;
        pattern[1] = 200;
        pattern[2] = 80;
        pattern[3] = 200;
        pattern[4] = 2000;
        pattern[5] = 200;
        Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern,-1);
        Log.i(TAG,"setting up notification");


        final String time = DateFormat.getTimeInstance().format(new Date()).toString();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.wm_finished)
                        .setContentTitle("Your Machine Has Finished!")
                        .setContentText("Completed at " + time)
                        .setAutoCancel(true)
                        .setLights(getResources().getColor(R.color.blue),500,500); //doesn't work
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ControlActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ControlActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

        mNotificationManager.notify(1, mBuilder.build());
    }

}
