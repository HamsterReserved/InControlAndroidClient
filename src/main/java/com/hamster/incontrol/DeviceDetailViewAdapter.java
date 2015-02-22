package com.hamster.incontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

class DeviceDetailViewAdapter extends BaseAdapter {

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
                intentDummy.putExtra("id", ((TextView)
                        ((View) v.getParent()).findViewById(R.id.tv_control_center_id))
                        .getText().toString());

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
                    LocalConfigStore lcs = new LocalConfigStore(mContext);
                    lcs.removeDevice(Integer.parseInt(item.getIntent().getStringExtra("id")));
                    addToControlCenters(lcs.getControlCenters());
                    lcs.close();
                }
            });
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuEditOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // Copied from net
            Resources res = mContext.getResources();
            AlertDialog.Builder builder;
            final AlertDialog alertDialog;
            final LayoutInflater inflater =
                    LayoutInflater.from(mContext);

            // Create the dialog
            View layout = inflater.inflate(R.layout.control_center_edit_dialog, null);
            builder = new AlertDialog.Builder(mContext);
            builder.setView(layout)
                    .setNegativeButton(res.getText(R.string.button_cancel), null)
                    .setTitle(res.getText(R.string.dialog_title_edit_controller));
            alertDialog = builder.create();

            // Set default values for EditText
            final EditText et_cc_id = (EditText) layout.findViewById(R.id.et_control_id);
            final EditText et_cc_name = (EditText) layout.findViewById(R.id.et_control_name);
            final EditText et_cc_cred = (EditText) layout.findViewById(R.id.et_control_cred);

            ControlCenter cc = findControllerById(
                    Integer.parseInt(item.getIntent().getStringExtra("id")));
            if (cc == null) {
                return true;
            }
            et_cc_id.setText("ID: " + String.valueOf(cc.getDeviceId()));
            et_cc_id.setEnabled(false);
            et_cc_name.setText(cc.getDeviceName());
            et_cc_name.requestFocus();
            et_cc_cred.setText(cc.getCredentials());

            // What a big listener
            DialogInterface.OnClickListener ocl_positive = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ControlCenter cc = new ControlCenter(mContext);
                    cc.setDeviceId(Integer.parseInt(et_cc_id.getText().toString().substring(4))); // Remove prefix "ID: "
                    // -1 if null, which is exactly INVAILD_SENSOR_ID
                    cc.setDeviceName(et_cc_name.getText().toString());
                    cc.setCredentials(et_cc_cred.getText().toString());

                    final LocalConfigStore lcs = new LocalConfigStore(mContext);

                    if (cc.isInfoComplete()) lcs.updateDevice(cc, ControlCenter.INVALID_DEVICE_ID);

                    final ControlCenter ccs[] = lcs.getControlCenters();
                    lcs.close();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            clearAll();
                            addToControlCenters(ccs);
                        }
                    });
                }
            };

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, res.getText(R.string.button_ok), ocl_positive);
            alertDialog.show();
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
        cc_id.setText(String.valueOf(mCenters.get(position).getDeviceId())); // setText(int) is for resources
        ib.setOnClickListener(menuOnClickListener);

        return convertView;
    }

    public void clearAll() {
        mCenters.clear();
        this.notifyDataSetChanged();
    }

    /**
     * Note that this is only for internal use.
     * It only searches in this adapter's memory.
     * Useful for quick search when adapters info is loaded into adapter already.
     *
     * @param id ID to search
     * @return the ControlCenter found, or null
     */
    private ControlCenter findControllerById(int id) {
        for (ControlCenter cc : mCenters) {
            if (cc.getDeviceId() == id) return cc;
        }
        return null;
    }
}
