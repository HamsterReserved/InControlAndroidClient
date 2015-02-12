package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class DeviceDetailViewAdapter extends BaseAdapter {

    /**
     * Items in List
     */
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<ControlCenter> mCenters;

    private View.OnClickListener menuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.device_popup_menu) {
                PopupMenu pop = new PopupMenu(v.getContext(), v);
                pop.getMenuInflater().inflate(R.menu.menu_control_center_overflow, pop.getMenu());
                pop.show();
            }
        }
    };

    DeviceDetailViewAdapter(Context context) {
        if (context != null)
            mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mCenters = new ArrayList<ControlCenter>();
    }

    public void addToControlCenters(ControlCenter new_ccs[]) {
        if (new_ccs == null) return;
        mCenters.addAll(Arrays.asList(new_ccs));
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
        ImageButton ib = (ImageButton) convertView.findViewById(R.id.device_popup_menu);

        cc_name.setText(mCenters.get(position).getDeviceName());
        cc_id.setText(String.valueOf(mCenters.get(position).getDeviceId())); // setText(int) is for resources
        ib.setOnClickListener(menuOnClickListener);

        return convertView;
    }

    public void clearAll() {
        mCenters.clear();
        this.notifyDataSetChanged();
    }
}
