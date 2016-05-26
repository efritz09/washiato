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
    public static ClusterStatusAdapter dryerStatusAdapter;

    public DryerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
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
        if(dryerStatusAdapter != null) dryerStatusAdapter.notifyDataSetChanged();
    }
}