package arya.com.washiato;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.content.SharedPreferences;

public class ControlActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
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
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG, "Request granted");

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(TAG, "Request denied");
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

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.READ_CONTACTS)) {
                Log.i(TAG, "in the if statement");

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                Log.i(TAG, "in the else statement");

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }



}
