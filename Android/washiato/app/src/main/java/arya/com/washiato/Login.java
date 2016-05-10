package arya.com.washiato;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
    static EditText password;
    static EditText username;
    Button register;
    String usernameHint;
    String passwordHint;
    public Firebase ref;
    private ProgressDialog mAuthProgressDialog;
    private Firebase.AuthStateListener mAuthStateListener;
    final Context context = this; //Set context
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    private static SharedPreferences loginSettings;
    private static SharedPreferences.Editor preferencesEditor;

    private final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check for local variables
//        loginSettings = getPreferences(0); //get private preferences
//        if(loginSettings.getBoolean("logged in",false)) {
//            Log.i(TAG,"Logged in");
//            Intent Successful_login = new Intent(Login.this, ControlActivity.class);
//            startActivity(Successful_login);
//            finish();
//        } else Log.i(TAG,"not logged");

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

//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    //Function implemented when Login button is pressed
    public void confirmLogin(View view) {
        mAuthProgressDialog.show();
        final Intent intent = new Intent(this, ControlActivity.class);

        //Create new instances of EditTexts and link them to name and password from layout
//        EditText username = (EditText) findViewById(R.id.edit_name);
//        EditText password = (EditText) findViewById(R.id.edit_password);

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
                Toast.makeText(context, getString(R.string.success_ctrl_activity), Toast.LENGTH_LONG).show(); //show toast for successful login

                //store this user info in shared preferences

                startActivity(intent); //start control activity
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                Log.e("LaunchActivity", "Error logging in");
                showErrorDialog(firebaseError.toString());
                Toast.makeText(context, getString(R.string.fail), Toast.LENGTH_LONG).show(); //show toast for failed login
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
                Log.i(TAG, "Anonymous authentication success");
                startActivity(intent); //start control activity
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                Log.i(TAG, "Anonymous authentication failure");
                showErrorDialog(firebaseError.toString());
            }
        });
    }

    public static void populateUP(String user, String pass) {
        username.setText(user);
        password.setText(pass);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
