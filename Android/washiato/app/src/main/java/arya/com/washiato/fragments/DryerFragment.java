package arya.com.washiato.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import arya.com.washiato.ClusterStatusAdapter;
import arya.com.washiato.Machine;
import arya.com.washiato.R;
import arya.com.washiato.TabActivity;

public class DryerFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    TextView vAlarm;
    Button bAlarmOn;
    Button bAlarmOff;

    public static ClusterStatusAdapter dryerStatusAdapter;

    public DryerFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_dryer, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_dryer_cluster);
        dryerStatusAdapter = new ClusterStatusAdapter(getActivity(), R.layout.cluster_status, TabActivity.dryerList);
        if(dryerStatusAdapter == null) Log.i(TAG,"shit be null");
        else {
            if(listView == null) Log.i(TAG,"listview is null");
            Log.i(TAG,"shit not be null");
            listView.setAdapter(dryerStatusAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                    Log.i(TAG, "longclicked");
                    return false;
                }
            });
            dryerStatusAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    public static void updateDryerList() {
        dryerStatusAdapter.notifyDataSetChanged();
        /*//recount the number of things
        int open = 0;
        int finished = 0;

        // update the displayed number of washers and dryers available
        for(int i = 0; i < TabActivity.dryerList.size(); i++) {
            Machine thisMachine = TabActivity.dryerList.get(i);
            if(thisMachine.status == 0) {
                open++;
            }else if(thisMachine.status == 1) {
                finished++;
            }
        }
        String plural = "s";
        Log.i(TAG,"num wash = " + open + "; fin wash = "+ finished);
        TabActivity.cluster.setNumDry(open);
        TabActivity.cluster.setFinWash(finished);
        if(open == 1) plural = "";

        }
        Log.i(TAG,"fin wash = " + finishedWashers + "; fin dry = "+finishedDryers);
        cluster.setNumWash(openWashers);
        cluster.setNumDry(openDryers);
        cluster.setFinWash(finishedWashers);
        cluster.setFinDry(finishedDryers);
        String plural1 = "s";
        String plural2 = "s";
        if(openWashers == 1) plural1 = "";
        if(finishedWashers == 1) plural2 = "";
        TabActivity.text_cluster_washers_available.setText(openWashers+" washer"+plural1+" open, "+finishedWashers+" washer"+plural2+" finished");
        if(openDryers != 1) plural1 = "s";
        if(finishedDryers != 1) plural2 = "s";
        text_cluster_dryers_available.setText(openDryers+" dryer"+plural1+" open, "+finishedDryers+" dryer"+plural2+" finished");
    }*/
    }


}