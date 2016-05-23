package arya.com.washiato;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import arya.com.washiato.helper.WaveView;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int ANIMATION_DISPLAY_TIME = 5000;
    private static final int ANIMATION_FADE_TIME = 1000;


    private View splash;
    private ImageView logo;
    private TextView status;
    private ProgressBar progressBar;

    private WaveView mWaveView;
    private AnimatorSet mAnimatorSet;

    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 10;

    /**
     * Runnable to open the worksite select activity after all data is loaded
     */
    private final Runnable openWorksiteSelectActivity = new Runnable() {
        @Override
        public void run() {
            /* Create an Intent that will start the Worksite Select Activity. */


            Log.i(TAG, getString(R.string.app_name) + ": Starting NextActivity");
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    };

    /**
     * Runnable to fade out the loading animation
     */
    private final Runnable fadeAnimation = new Runnable() {
        @Override
        public void run() {
            /* Fade everything out then start next activity. */
            Log.i(TAG, "Fade now!");
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(1000);

            ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                    mWaveView, "waterLevelRatio", 0.45f, 0.4f);
            waterLevelAnim.setDuration(1000);
            waterLevelAnim.setInterpolator(new DecelerateInterpolator());
            waterLevelAnim.start();

            AnimationSet animation = new AnimationSet(true);
            animation.addAnimation(fadeOut);
            mWaveView.startAnimation(fadeOut);
            mWaveView.setVisibility(View.INVISIBLE);
            logo.startAnimation(fadeOut);
            logo.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(openWorksiteSelectActivity, ANIMATION_FADE_TIME);
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

        logo = (ImageView) findViewById(R.id.washiato_logo);

        // Animation wave
        mWaveView = (WaveView) findViewById(R.id.wave_view);
        //mWaveView.setBorder(mBorderWidth, mBorderColor);
        mWaveView.setShowWave(true);

        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1100);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        waveShiftAnim.start();

        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", 0f, 0.45f);
        waterLevelAnim.setDuration(5000);
        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
        waterLevelAnim.start();

        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                mWaveView, "amplitudeRatio", 0f, 0.06f);
        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(1300);
        amplitudeAnim.setInterpolator(new LinearInterpolator());
        amplitudeAnim.start();


        // Set textview and loading animation to let user know data is loading
//        status = (TextView) findViewById(R.id.status_message);

        // Kick off runnable to move to next activity
        new Handler().postDelayed(fadeAnimation, ANIMATION_DISPLAY_TIME);
    }
}
