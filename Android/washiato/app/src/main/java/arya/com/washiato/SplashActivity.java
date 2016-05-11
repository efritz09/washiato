package arya.com.washiato;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_DISPLAY_TIME = 4000;

    private View splash;
    private TextView status;
    private ProgressBar progressBar;

    /**
     * Runnable to open the worksite select activity after all data is loaded
     */
    private final Runnable openWorksiteSelectActivity = new Runnable() {
        @Override
        public void run() {
            /* Create an Intent that will start the Worksite Select Activity. */
            Log.i(TAG, getString(R.string.app_name) + ": Starting NextActivity");
            Intent mainIntent = new Intent(SplashActivity.this, Login.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the action bar if it's visible
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Hide the UI menus
        splash = findViewById(R.id.splash_image);
        if (splash != null) {
            splash.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        // Set textview and loading animation to let user know data is loading
        status = (TextView) findViewById(R.id.status_message);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Kick off runnable to move to next activity
        new Handler().postDelayed(openWorksiteSelectActivity, SPLASH_DISPLAY_TIME);
    }
}
