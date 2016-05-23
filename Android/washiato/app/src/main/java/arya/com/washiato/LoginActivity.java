package arya.com.washiato;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final boolean RESET_FIREBASE_CLUSTERS = false;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    public Firebase ref;

    private TextInputEditText _passwordText;
    private TextInputEditText _emailText;
    private TextView _signupLink;
    private TextView _passwordResetLink;
    //private CheckBox _showPwdCheckBox;
    private ProgressDialog mAuthProgressDialog;
    private AppCompatButton _loginButton;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide the action bar if it's visible
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

        // Check logged in status in shared preferences
        washiato.preferences = getPreferences(0);
        if(washiato.preferences.getBoolean(getString(R.string.pref_logged_in),false)
                && ref.getAuth() != null) {
            Log.i(TAG, "Already Logged in");
            Intent intent = new Intent(LoginActivity.this, ControlActivity.class);
            startActivity(intent);
            finish();
        } else Log.i(TAG,"Not logged in");

        if(RESET_FIREBASE_CLUSTERS) {
            createMachines();
        }

        //set up the firebase connection progress dialog
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setMessage("Logging in...");
        mAuthProgressDialog.setCancelable(false);

        // Set up signup listener
        _signupLink = (TextView) findViewById(R.id.link_signup);
        _signupLink.setPaintFlags(_signupLink.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                //startActivityForResult(intent, REQUEST_SIGNUP);
                startActivity(intent);
            }
        });

        _passwordResetLink = (TextView) findViewById(R.id.forgot_password);
        _passwordResetLink.setPaintFlags(_signupLink.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        _passwordResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        _loginButton = (AppCompatButton) findViewById(R.id.btn_login);
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogin();
            }
        });

        _passwordText = (TextInputEditText) findViewById(R.id.input_password);
        _emailText = (TextInputEditText) findViewById(R.id.input_email);
    }


    public void confirmLogin() {
        mAuthProgressDialog.show();
        final Intent intent = new Intent(this, ControlActivity.class);
        final String name = _emailText.getText().toString();
        final String pawd = _passwordText.getText().toString();

        //Authenticate using Firebase
        ref.authWithPassword(name, pawd, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthProgressDialog.cancel();
                Map<String, Object> map = new HashMap<String, Object>(); //hashmap of username and password
                map.put("Password", pawd); //fill map
                map.put("UserName", name);
                ref.child("Users").child(authData.getUid()).updateChildren(map); //update firebase database

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
                String auth = authData.getProvider();
                Log.i(TAG,auth);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                Log.i(TAG, "Anonymous authentication failure");
                showErrorDialog(firebaseError.toString());
            }
        });
    }

    private void changeEmail(String oldEmail, String pass, String newEmail) {
        ref.changeEmail(oldEmail, pass, newEmail, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),
                        "Email successfully changed", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                showErrorDialog(firebaseError.toString());
            }
        });

    }

    private void changePassword(String email, String oldPass, String newPass) {
        ref.changePassword(email, oldPass, newPass, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),
                        "Password successfully changed", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                Log.i(TAG, "Change password failure");
                showErrorDialog(firebaseError.toString());
            }
        });
    }

    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText email = new EditText(getApplicationContext());
        email.setTextColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        email.setHintTextColor(ContextCompat.getColor(this,R.color.colorAccent));
        email.setHint(getString(R.string.hint_enter_email));
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(email);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Context cont = getApplicationContext();
                String address = email.getText().toString();
                if(address.isEmpty() ||
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                    Toast.makeText(cont, getString(R.string.error_email_required), Toast.LENGTH_LONG).show();
                } else {
                    ref.resetPassword(address, new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(),
                                    "Password reset email sent", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Log.i(TAG, "Reset password failure");
                            showErrorDialog(firebaseError.toString());
                        }
                    });
                }
            }
        });

        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void removeUser(String email, String password) {
        ref.removeUser(email, password, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),
                        "User removed", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FirebaseError firebaseError) {
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
        Machine mach1 = new Machine("Apollo","Olympus",0,true,false,0);
        Machine mach2 = new Machine("Athena","Olympus",2,false, false,0);
        Machine mach5 = new Machine("Zeus","Olympus",0,false, false,0);
        Machine mach6 = new Machine("Ares","Olympus",1,true, false,5);
        Machine mach3 = new Machine("Thor","Valhalla",1,true, false,5);
        Machine mach4 = new Machine("Odin","Valhalla",2,false, false,0);
        Machine mach7 = new Machine("Loki","Valhalla",0,true, false,0);
        Machine mach8 = new Machine("Poseidon","Olympus",1,false, false,10);


        ref.child("Machines").child("04457C8A6F4080").setValue(mach1);
        ref.child("Machines").child("044D5B8A6F4080").setValue(mach2);
        ref.child("Machines").child("04F72452783F80").setValue(mach3);
        ref.child("Machines").child("04CF3652783F80").setValue(mach4);
        ref.child("Machines").child("666xx666").setValue(mach5);
        ref.child("Machines").child("666666").setValue(mach6);
        ref.child("Machines").child("04F2C58A6F4080").setValue(mach7);
        ref.child("Machines").child("6666").setValue(mach8);

        ArrayList<String> mach_array = new ArrayList<String>();
        mach_array.add("6666");
        mach_array.add("666666");
        mach_array.add("666xx666");
        mach_array.add("044D5B8A6F4080");
        mach_array.add("04457C8A6F4080");

        ArrayList<String> mach_array2 = new ArrayList<String>();
        mach_array2.add("04F72452783F80");
        mach_array2.add("04CF3652783F80");
        mach_array2.add("04F2C58A6F4080");

        Cluster clus1 = new Cluster("Rains 218", 1, 1, mach_array);
        Cluster clus2 = new Cluster("Rains 217", 1, 0, mach_array2);

        ref.child("Clusters").child("Olympus").setValue(clus1);
        ref.child("Clusters").child("Valhalla").setValue(clus2);
    }

}
