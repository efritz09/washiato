package arya.com.washiato;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Eric on 5/15/2016.
 */
public class ClusterStatusAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    ArrayList<Machine> data;

    public ClusterStatusAdapter(Context context, int layoutResourceID, ArrayList<Machine> data) {
        super(context, layoutResourceID, data);
        this.layoutResourceId = layoutResourceID;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent,false);
            holder = new ViewHolder();
            holder.icon = (ImageView) row.findViewById(R.id.icon_cluster_status);
            holder.status = (TextView) row.findViewById(R.id.text_cluster_status);
            holder.time = (TextView) row.findViewById(R.id.text_cluster_time_since_finished);
            row.setTag(holder);
        }else {
            holder = (ViewHolder)row.getTag();
        }
        Machine thisMachine = data.get(position);
        switch (thisMachine.getStatus()) {
            case 0:
                holder.status.setText("Open");
                //check to see if it's a washer or dryer, set icon accordingly
                if (thisMachine.getWasher())
                    Picasso.with(context).load(R.mipmap.wm_open).into(holder.icon);
                else Picasso.with(context).load(R.mipmap.dry_open).into(holder.icon);
                break;
            case 1:
                holder.status.setText("Running");
                if (thisMachine.getWasher()) Picasso.with(context).load(R.mipmap.wm_running).into(holder.icon);
                else Picasso.with(context).load(R.mipmap.dry_running).into(holder.icon);
                break;
            case 2:
                holder.status.setText("Finished");
                if (thisMachine.getWasher()) Picasso.with(context).load(R.mipmap.wm_finished).into(holder.icon);
                else Picasso.with(context).load(R.mipmap.dry_finished).into(holder.icon);
                //maybe display time here?
                break;
        }
        return row;
    }

    static class ViewHolder {
        TextView status;
        TextView time;
        ImageView icon;
    }
}