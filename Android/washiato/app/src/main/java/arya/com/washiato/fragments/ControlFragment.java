package arya.com.washiato.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import arya.com.washiato.R;

public class ControlFragment extends Fragment {
    TextView text_user;
    TextView text_cluster;
    TextView text_cluster_current;
    TextView text_time;
    TextView text_machine;
    TextView text_machine_status;
    Button button_nfcOn;
    EditText editText_machine_name;
    Button button_machine_select;

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false);

    }

}
