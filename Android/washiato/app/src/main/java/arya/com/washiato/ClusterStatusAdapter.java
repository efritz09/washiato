package arya.com.washiato;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

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
            holder.name = (TextView) row.findViewById(R.id.text_machine_name);
            holder.icon = (ImageView) row.findViewById(R.id.icon_cluster_status);
            holder.status = (TextView) row.findViewById(R.id.text_cluster_status);
            holder.time = (TextView) row.findViewById(R.id.text_cluster_time_since_finished);
            row.setTag(holder);
        }else {
            holder = (ViewHolder)row.getTag();
        }
        Machine thisMachine = data.get(position);
        holder.name.setText(thisMachine.getName());
        switch (thisMachine.getStatus()) {
            case 0:
                holder.name.setTypeface(EasyFonts.robotoBold(context));
                holder.status.setText(R.string.text_machine_open);
                holder.status.setTypeface(EasyFonts.robotoBold(context));
//                holder.status.setText(ContextCompat.getColor(context, R.color.green));
                holder.time.setText("");
                //check to see if it's a washer or dryer, set icon accordingly
                if (thisMachine.getWasher()) Picasso.with(context).load(R.drawable.ic_wm_open).into(holder.icon);
                else Picasso.with(context).load(R.drawable.ic_dry_open).into(holder.icon);
                break;
            case 1:
                holder.name.setTypeface(EasyFonts.robotoMedium(context));
                holder.status.setText(R.string.text_machine_finished);
                holder.status.setTypeface(EasyFonts.robotoMedium(context));
//                holder.status.setText(ContextCompat.getColor(context, R.color.gold));
                holder.time.setText(Integer.toString(thisMachine.getTime()) + " minutes ago");
                holder.time.setTypeface(EasyFonts.robotoMedium(context));
                if (thisMachine.getWasher()) Picasso.with(context).load(R.drawable.ic_wm_finished).into(holder.icon);
                else Picasso.with(context).load(R.drawable.ic_dry_finished).into(holder.icon);
                //maybe display time here?
                break;
            case 2:
                holder.name.setTypeface(EasyFonts.robotoRegular(context));
                holder.status.setText(R.string.text_machine_running);
                holder.status.setTypeface(EasyFonts.robotoRegular(context));
//                holder.status.setText(ContextCompat.getColor(context, R.color.red));
                holder.time.setText("");
                if (thisMachine.getWasher()) Picasso.with(context).load(R.drawable.ic_wm_running).into(holder.icon);
                else Picasso.with(context).load(R.drawable.ic_dry_running).into(holder.icon);
                break;
        }
        return row;
    }

    static class ViewHolder {
        TextView name;
        TextView status;
        TextView time;
        ImageView icon;
    }
}