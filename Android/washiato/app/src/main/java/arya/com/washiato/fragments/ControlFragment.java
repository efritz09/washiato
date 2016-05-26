//package arya.com.washiato.fragments;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.vstechlab.easyfonts.EasyFonts;
//
//import java.util.Map;
//import java.util.Objects;
//
//import arya.com.washiato.Cluster;
//import arya.com.washiato.Machine;
//import arya.com.washiato.R;
//import arya.com.washiato.TabActivity;
//
//public class ControlFragment extends Fragment {
//    private final static String TAG = "Control Fragment";
//    static TextView text_tab_user;
//    static TextView text_tab_cluster;
//    static TextView text_tab_cluster_current;
//    static TextView text_tab_time;
//    static TextView text_tab_machine;
//    static TextView text_tab_machine_status;
//    Button button_nfcOn;
//    EditText editText_machine_name;
//    Button button_machine_select;
//
//    public ControlFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
////        text_tab_user = (TextView) getActivity().findViewById(R.id.text_tab_user);
////        text_tab_user.setTypeface(EasyFonts.robotoBlack(getActivity()));
////        text_tab_cluster = (TextView) getActivity().findViewById(R.id.text_tab_cluster);
////        text_tab_user.setTypeface(EasyFonts.robotoThin(getActivity()));
////        text_tab_cluster_current = (TextView) getActivity().findViewById(R.id.text_tab_cluster_current);
////        text_tab_user.setTypeface(EasyFonts.robotoThin(getActivity()));
////        text_tab_time = (TextView) getActivity().findViewById(R.id.text_tab_time);
////        text_tab_time.setTypeface(EasyFonts.robotoLightItalic(getActivity()));
////        text_tab_machine = (TextView) getActivity().findViewById(R.id.text_tab_machine);
////        text_tab_machine.setTypeface(EasyFonts.robotoBold(getActivity()));
////        text_tab_machine_status = (TextView) getActivity().findViewById(R.id.text_tab_machine_status);
////        text_tab_machine_status.setTypeface(EasyFonts.robotoThin(getActivity()));
//        Log.i(TAG,"cluster initialized");
//
////        button_nfcOn = (Button)findViewById(R.id.button_nfc);
////        editText_machine_name = (EditText)findViewById(R.id.edit_machine_id);
////        button_machine_select = (Button)findViewById(R.id.button_select_machine);
//        return inflater.inflate(R.layout.fragment_control, container, false);
//
//    }
//
////    public static void setCluster(final String clusterName, final int i, Context cont) {
////        runOnUiThread(new Runnable() {
////            @Override
////            public void run() {
////                if(i == 0) { //anonymous
////                    text_tab_cluster.setText("Cluster: " + clusterName);
////                    text_tab_cluster_current.setText("");
////                }else if(i == 1) { //in the home cluster
////                    text_tab_cluster.setText("Home Cluster: " + clusterName );
////                    text_tab_cluster_current.setText("");
////                }else if(i == 2) { //new current cluster
////                    text_tab_cluster_current.setText("Current Cluster: " + clusterName );
////                }else if(i == 3) { //empty the current cluster
////                    text_tab_cluster_current.setText("");
////                }
////            }
////        }
////
////    }
//
////    public static void setMachine(Map currentMachine, Context cont) {
////        int status = (int)(long)currentMachine.get("status");
////        boolean washer = (boolean)currentMachine.get("washer");
//////                Button button = (Button)findViewById(R.id.button_omw);
////        if(status == 0) {
////            Log.i(TAG,"machine is open");
////                    text_tab_machine_status.setText(R.string.machine_open_flavortext);
//////                    text_tab_machine_status.setTextColor(getResources().getColor(R.color.green));
//////                    text_tab_machine.setTextColor(getResources().getColor(R.color.green));
////                    text_tab_time.setText("");
//////                    if(button != null) button.setVisibility(View.INVISIBLE);
////        } else if(status == 1) {
////            Log.i(TAG,"machine is finished");
////                    if(washer) text_tab_machine_status.setText(R.string.wash_finished_flavortext);
////                    else text_tab_machine_status.setText(R.string.dry_finished_flavortext);
//////                    text_tab_machine_status.setTextColor(getResources().getColor(R.color.gold));
//////                    text_tab_machine.setTextColor(getResources().getColor(R.color.gold));
////                    text_tab_time.setText(Integer.toString((int)(long)currentMachine.get("time")) + " minutes ago");
////            //set up button
//////                    if(button != null) button.setVisibility(View.VISIBLE);
////            //only create notification if omw is false. Prevents setting the omw from buzzing the user
////            if(!(boolean)currentMachine.get("omw")) TabActivity.createNotification(cont);
////
////        } else if(status == 2) {
////            Log.i(TAG,"machine is running");
////                    if(washer) text_tab_machine_status.setText(R.string.wash_running_flavortext);
////                    else text_tab_machine_status.setText(R.string.dry_running_flavortext);
//////                    text_tab_machine_status.setTextColor(getResources().getColor(R.color.red));
//////                    text_tab_machine.setTextColor(getResources().getColor(R.color.red));
////                    text_tab_time.setText("");
//////                    if(button != null) button.setVisibility(View.INVISIBLE);
////        }
////        else Log.i(TAG,"Somehow we have a status issue");
////
////    }
//
//}
