package arya.com.washiato.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
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
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import arya.com.washiato.ClusterStatusAdapter;
import arya.com.washiato.Machine;
import arya.com.washiato.R;
import arya.com.washiato.TabActivity;

public class DryerFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    public static ClusterStatusAdapter dryerStatusAdapter;
    public static Firebase ref;
    private static final String FIREBASE_URL = "https://washiato.firebaseio.com/";

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
        //Create a reference to firebase database
        ref = new Firebase(FIREBASE_URL);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_dryer_cluster);
        dryerStatusAdapter = new ClusterStatusAdapter(getActivity(), R.layout.cluster_status, TabActivity.dryerList);
        if(dryerStatusAdapter == null) Log.i(TAG,"shit be null");
        else {
            if(listView == null) Log.i(TAG,"listview is null");
            listView.setAdapter(dryerStatusAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                    Log.i(TAG, "longclicked");
                    final Machine machine = TabActivity.dryerList.get(pos);
                    AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
                    alertbox.setTitle("Connect to " + machine.getName() + "?");
//                    alertbox.setMessage(R.string.alert_nfc_on_message);
                    alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
    //                      //set the serial somehow
                            TabActivity.ConnectToMachine(machine.getName(),getActivity());
                        }
                    });
                    alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });
                    alertbox.show();
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