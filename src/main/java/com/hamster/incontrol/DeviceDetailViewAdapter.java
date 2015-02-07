package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceDetailViewAdapter extends BaseAdapter {

    /**
     * Items in List
     */
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<ControlCenter> mCenters;

    DeviceDetailViewAdapter(Context context) {
        if (context != null)
            mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mCenters = new ArrayList<ControlCenter>();
    }

    public void addToControlCenters(ControlCenter new_ccs[]) {
        if (new_ccs == null) return;
        for (int i = 0; i < new_ccs.length; ++i) {
            mCenters.add(new_ccs[i]);
        }
    }

    @Override
    public int getCount() {
        return mCenters.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > mCenters.size() - 1 || position < 0)
            return null;
        return mCenters.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.control_center_detail, null);
        TextView cc_name = (TextView) convertView.findViewById(R.id.tv_control_center_name);
        TextView cc_id = (TextView) convertView.findViewById(R.id.tv_control_center_id);

        cc_name.setText(mCenters.get(position).getDeviceName());
        cc_id.setText(String.valueOf(mCenters.get(position).getDeviceId())); // setText(int) is for resources

        return convertView;
    }

    public void clearAll() {
        mCenters.clear();
        this.notifyDataSetChanged();
    }
}
