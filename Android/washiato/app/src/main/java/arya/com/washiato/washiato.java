package arya.com.washiato;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.firebase.client.Firebase;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Arya on 5/6/16.
 */
public class washiato extends android.app.Application {
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor preferencesEditor;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this); //set system-wide context for firebase
    }


}
