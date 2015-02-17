package com.hamster.incontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

    private Handler handler = new Handler();

    private View.OnClickListener menuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.device_popup_menu) {
                PopupMenu pop = new PopupMenu(v.getContext(), v);
                // TODO: This is a dirty way to pass info to menuitem. Fix it if possible.
                Intent intentDummy = new Intent();
                intentDummy.putExtra("id", ((TextView) ((View) v.getParent()).findViewById(R.id.tv_control_center_id)).getText().toString());
                pop.getMenuInflater().inflate(R.menu.menu_control_center_overflow, pop.getMenu());
                pop.getMenu().getItem(0).setOnMenuItemClickListener(menuEditOnClickListener);
                pop.getMenu().getItem(0).setIntent(intentDummy);
                pop.getMenu().getItem(1).setOnMenuItemClickListener(menuDeleteOnClickListener);
                pop.getMenu().getItem(1).setIntent(intentDummy);
                pop.show();
            }
        }
    };

    private MenuItem.OnMenuItemClickListener menuDeleteOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    clearAll();
                    LocalConfigStore lcs_m = new LocalConfigStore(mContext);
                    lcs_m.removeDevice(Integer.parseInt(item.getIntent().getStringExtra("id")));
                    addToControlCenters(lcs_m.getControlCenters());
                    lcs_m.close();
                }
            });
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuEditOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            return true;
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.control_center_detail, null);
        TextView cc_name = (TextView) convertView.findViewById(R.id.tv_control_center_name);
        TextView cc_id = (TextView) convertView.findViewById(R.id.tv_control_center_id);
        ImageButton ib = (ImageButton) convertView.findViewById(R.id.device_popup_menu);

        cc_name.setText(mCenters.get(position).getDeviceName());
        cc_id.setText("ID: " +
                String.valueOf(mCenters.get(position).getDeviceId())); // setText(int) is for resources
        ib.setOnClickListener(menuOnClickListener);

        return convertView;
    }

    public void clearAll() {
        mCenters.clear();
        this.notifyDataSetChanged();
    }
}
