package arya.com.washiato;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.content.SharedPreferences;

import java.util.ResourceBundle;

public class ControlActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_NFC = 0;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private final String TAG = "ControlActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions(this);


    }

/*    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                Log.i(TAG,"Something is happening...");
                boolean audioAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(audioAccepted) Log.i(TAG,"audio accepted");
                else Log.i(TAG,"audio rejected");
                boolean cameraAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted) Log.i(TAG,"camera accepted");
                else Log.i(TAG,"camera rejected");
                break;
        }
    }*/


/*    private boolean shouldWeAsk(String permission) {
        return (sharedPreferences.getBoolean(permission, true));
    }
    private void markAsAsked(String permission){
        sharedPreferences.edit().putBoolean(permission, false).apply;
    }*/



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_NFC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "NFC granted");
                    checkNFCon();

                } else {
                    //maybe set up a dialog telling them no NFC permission and instructing them
                    Log.i(TAG, "NFC denied");
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "LOCATION granted");
                }else {
                    Log.i(TAG, "LOCATION denied");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void checkPermissions( Activity thisActivity) {
        // Here, thisActivity is the current activity
        Log.i(TAG, "Checking permissions...");

        if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG,"Location 'should we show an explanation'");
            }else {
                Log.i(TAG,"Location requesting permission");
                ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }else {
            Log.i(TAG, "Location permission already granted!");
        }

        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.NFC)) {
                Log.i(TAG, "NFC 'should we show an explanation'");
            } else {
                Log.i(TAG, "Location requesting permission");
                ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.NFC}, MY_PERMISSIONS_REQUEST_NFC);
            }
        }else {
            Log.i(TAG, "NFC permission already granted!");
            checkNFCon();
        }
    }



    /*
    checkNFCon: Checks if NFC is on. Asks user to turn it on, opens the NFC settings
     */
    public void checkNFCon () {
        android.nfc.NfcAdapter mNfcAdapter= android.nfc.NfcAdapter.getDefaultAdapter(ControlActivity.this);
        if (!mNfcAdapter.isEnabled()) {
            AlertDialog.Builder alertbox = new AlertDialog.Builder(ControlActivity.this);
            alertbox.setTitle("Enable NFC");
            alertbox.setMessage("This app uses NFC to link your phone with the washer/dryer you're using, but we need to turn it on. To use NFC, just set the phone near the washiato device.");
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
                    noNFC.setMessage("That's fine! To link this phone manually, you'll need to enter the washer/dryer ID in the text box");
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
                    noNFC.show();
                }
            });
            alertbox.show();

        }
    }



}
