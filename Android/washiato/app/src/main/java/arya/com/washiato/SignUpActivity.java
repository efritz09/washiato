package arya.com.washiato;

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

                final String userName = editTextUserName.getText().toString();
                final String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText()
                        .toString();

                if (userName.equals("") || password.equals("")
                        || confirmPassword.equals("")) { //if username and password are blank

                    Toast.makeText(getApplicationContext(), "Field Vacant",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    //if password input does not match password entered in confirm password
                    Toast.makeText(getApplicationContext(),
                            "Password does not match", Toast.LENGTH_LONG)
                            .show();
                    return;
                } else {
                    //if username is unique, proceed with adding entry to database
                    ref.createUser(userName, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            Log.i("SignUpActivity", "Successful signup");
                            Toast.makeText(getApplicationContext(), "Account Successfully Created!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Log.i("SignUpActivity", "Erroneous signup");
                            Toast.makeText(getApplicationContext(), "Account already exists. Try again", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(SignUpActivity.this,
                                    Login.class);
                            startActivity(i); //go back to Launch activity if username repeated
                            finish();
                        }
                    });
                    Intent i = new Intent(SignUpActivity.this,
                            Login.class);
                    startActivity(i); //go back to launch activity
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
