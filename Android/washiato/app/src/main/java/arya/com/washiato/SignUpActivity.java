package arya.com.washiato;

import android.app.AlertDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.vstechlab.easyfonts.EasyFonts;

import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";

    Firebase ref;
    TextInputEditText _emailText;
    TextInputEditText _passwordText;
    TextInputEditText _passwordConfirmText;
    TextView _cancelLink;
    Button _signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Create reference to firebase database
        ref = new Firebase(FIREBASE_URL);//.child("signups");

        _emailText = (TextInputEditText) findViewById(R.id.etUserName);
        _passwordText = (TextInputEditText) findViewById(R.id.etPassword);
        _passwordConfirmText = (TextInputEditText) findViewById(R.id.etConfirmPassword);

        _cancelLink = (TextView) findViewById(R.id.link_cancel);
        _cancelLink.setTypeface(EasyFonts.robotoBold(this));
        //_cancelLink.setPaintFlags(_cancelLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        _cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        _signupButton = (Button) findViewById(R.id.bCreateAccount);
        _signupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        if(!validate()) {
            return;
        }
        Log.i(TAG, "Made it");
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        Log.i(TAG, "Successful signup");
                        Toast.makeText(getApplicationContext(),
                                "Account Successfully Created!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK, null);
                        finish();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        showErrorDialog(firebaseError.toString());
                    }
                });
    }

    /**
     * Make sure user info fits requirements
     * @return
     */
    private boolean validate() {
        boolean valid = true;

        TextInputLayout emailLayout = (TextInputLayout) findViewById(R.id.emailLayout);
        TextInputLayout passLayout = (TextInputLayout) findViewById(R.id.passLayout);
        TextInputLayout confirmPassLayout = (TextInputLayout) findViewById(R.id.confirmPassLayout);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String passwordConfirm = _passwordConfirmText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passLayout.setError("Enter a valid password");
            return false;
        } else {
            passLayout.setError(null);
        }

        if(!password.equals(passwordConfirm)) {
            confirmPassLayout.setError("Passwords must match");
            return false;
        } else {
            confirmPassLayout.setError(null);
        }

        return true;
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
