package arya.com.washiato;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    //Declare module level object variables needed
    CheckBox checkbox;
    public static EditText password;
    public static EditText username;
    Button register;
    public Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    private ProgressDialog mAuthProgressDialog;
    private Firebase.AuthStateListener mAuthStateListener;
    final Context context = this; //Set context

    private final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createMachines();

        //
        washiato.preferences = getPreferences(0); //get private preferences
        if(washiato.preferences.getBoolean(getString(R.string.pref_logged_in),false)) {
            Log.i(TAG,"Logged in");
            Intent Successful_login = new Intent(Login.this, ControlActivity.class);
            startActivity(Successful_login);
            finish();
        } else Log.i(TAG,"not logged");

        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);
        //set up the firebase connection progress dialog
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);

        //set up register listener
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentSignUP = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intentSignUP); //begin signup activity
            }
        });

        //Create an instance of EditText and link it to password from layout
        password = (EditText) findViewById(R.id.edit_password);
        username = (EditText) findViewById(R.id.edit_name);
        //Create an instance of CheckBox and link it to checkbox from layout
        checkbox = (CheckBox) findViewById(R.id.ShowPwd);

        //Function implemented when checkbox is clicked
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //@Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) { //If checkbox is not checked
                    //Hide password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    checkbox.setText(getString(R.string.show_password)); //Checkbox says "Show Password"
                } else {
                    //Else show password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    checkbox.setText(getString(R.string.hide_password));//Checkbox says "Hide Password"
                }
            }
        });
    }


    //Function implemented when Login button is pressed
    public void confirmLogin(View view) {
        mAuthProgressDialog.show();
        final Intent intent = new Intent(this, ControlActivity.class);

        //Get strings entered as name and password
        final String name = username.getText().toString();
        final String pawd = password.getText().toString();

        //Authenticate using Firebase
        ref.authWithPassword(name, pawd, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthProgressDialog.hide();
                Map<String, String> map = new HashMap<String, String>(); //hashmap of username and password
                map.put("Password", pawd); //fill map
                map.put("UserName", name);
                ref.child("Users").child(authData.getUid()).setValue(map); //update firebase database
//                Toast.makeText(context, getString(R.string.success_ctrl_activity), Toast.LENGTH_LONG).show(); //show toast for successful login

                //store this user info in shared preferences
                washiato.preferencesEditor = washiato.preferences.edit();
                washiato.preferencesEditor.putBoolean(getString(R.string.pref_logged_in), true);
                washiato.preferencesEditor.putString(getString(R.string.pref_user_id), authData.getUid());
                washiato.preferencesEditor.apply();

                startActivity(intent); //start control activity
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                Log.e("LaunchActivity", "Error logging in");
                showErrorDialog(firebaseError.toString());
//                Toast.makeText(context, getString(R.string.fail), Toast.LENGTH_LONG).show(); //show toast for failed login
            }
        });
    }

    public void anonLogin(View view) {
        mAuthProgressDialog.show();
        final Intent intent = new Intent(this, ControlActivity.class);

        ref.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthProgressDialog.hide();
//                Log.i(TAG, "Anonymous authentication success");
                startActivity(intent); //start control activity
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
//                Log.i(TAG, "Anonymous authentication failure");
                showErrorDialog(firebaseError.toString());
            }
        });
    }


    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createMachines() {
        Log.i(TAG,"create the machines in firebase");
        Machine mach1 = new Machine("Apollo","Olympus","DEADBEEF",0,true);
        Machine mach2 = new Machine("Athena","Olympus","12345678",2,false);
        Machine mach3 = new Machine("Thor","Valhalla","666EF666",3,true);
        Machine mach4 = new Machine("Odin","Valhalla","666AB666",2,false);

        ref.child("Machines").child("DEADBEEF").setValue(mach1);
        ref.child("Machines").child("12345678").setValue(mach2);
        ref.child("Machines").child("666EF666").setValue(mach3);
        ref.child("Machines").child("666AB666").setValue(mach4);

    }

}
