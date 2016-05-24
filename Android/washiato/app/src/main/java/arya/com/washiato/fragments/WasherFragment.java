package arya.com.washiato.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import arya.com.washiato.R;

public class WasherFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    TextView vAlarm;
    Button bAlarmOn;
    Button bAlarmOff;

    public WasherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_washer, container, false);

        vAlarm = (TextView) rootView.findViewById(R.id.ble_status);
        bAlarmOn = (Button) rootView.findViewById(R.id.alarmOn);
        bAlarmOff = (Button) rootView.findViewById(R.id.alarmOff);

        bAlarmOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUIAlarmStatus(true);
            }
        });

        bAlarmOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUIAlarmStatus(false);
            }
        });
        return rootView;
    }

    public void updateUIAlarmStatus(final boolean alarmStatus) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(alarmStatus) {
                    vAlarm.setText("Washer On");
                } else {
                    vAlarm.setText("Washer Off");
                }
            }
        });
    }
}