package arya.com.washiato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    //Declare module level variables
    Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";
    EditText editTextUserName, editTextPassword, editTextConfirmPassword;
    Button btnCreateAccount;
    private String mUsername;
    private final String TAG = "SignUpActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create reference to firebase database
        ref = new Firebase(FIREBASE_URL);//.child("signups");

        //Find EditTexts for username and password
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);

        btnCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);

        //Function implemented when Create Account button is pressed
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                final String username = editTextUserName.getText().toString();
                final String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                if (username.equals("") || password.equals("") || confirmPassword.equals("")) { //if username and password are blank
                    Toast.makeText(getApplicationContext(), "Field Vacant",Toast.LENGTH_LONG).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    //if password input does not match password entered in confirm password
                    Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //if username is unique, proceed with adding entry to database
                    ref.createUser(username, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            Log.i(TAG, "Successful signup");
                            Toast.makeText(getApplicationContext(), "Account Successfully Created!", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(SignUpActivity.this,Login.class);
                            startActivity(i); //go back to launch activity
                            Login.populateUP(username,password);
                            finish();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Log.i(TAG, "username exists");
                            showErrorDialog(firebaseError.toString());
                        }
                    });

                }
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

}
