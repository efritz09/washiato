package arya.com.washiato.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import arya.com.washiato.Cluster;
import arya.com.washiato.ClusterStatusAdapter;
import arya.com.washiato.ControlActivity;
import arya.com.washiato.Machine;
import arya.com.washiato.R;
import arya.com.washiato.TabActivity;

public class WasherFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    public static ClusterStatusAdapter washerStatusAdapter;

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

        ListView listView = (ListView) rootView.findViewById(R.id.listview_washer_cluster);
        washerStatusAdapter = new ClusterStatusAdapter(getActivity(), R.layout.cluster_status, TabActivity.washerList);
        if(washerStatusAdapter == null) Log.i(TAG,"shit be null");
        else {
            listView.setAdapter(washerStatusAdapter);
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
            washerStatusAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    public static void updateWasherList() {
        if(washerStatusAdapter != null) washerStatusAdapter.notifyDataSetChanged();
    }

}
