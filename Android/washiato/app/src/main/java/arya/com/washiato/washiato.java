package arya.com.washiato;

import android.content.SharedPreferences;

import com.firebase.client.Firebase;

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
