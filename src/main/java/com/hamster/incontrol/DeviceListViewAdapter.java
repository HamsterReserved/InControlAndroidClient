package com.hamster.incontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

class DeviceListViewAdapter extends BaseAdapter {

    /**
     * Items in List
     */
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<Sensor> mSensors;
    private Handler handler = new Handler();

    private View.OnClickListener menuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sensor_popup_menu) {
                PopupMenu pop = new PopupMenu(mContext, v);

                // TODO: This is a dirty way to pass info to menuitem. Fix it if possible.
                Intent intentDummy = new Intent();
                intentDummy.putExtra("id", ((TextView)
                        ((View) v.getParent()).findViewById(R.id.tv_sensor_id_invisible))
                        .getText().toString());

                pop.getMenuInflater().inflate(R.menu.menu_sensor_overflow, pop.getMenu());
                pop.getMenu().getItem(0).setOnMenuItemClickListener(menuTriggerOnClickListener);
                pop.getMenu().getItem(0).setIntent(intentDummy);
                pop.getMenu().getItem(1).setOnMenuItemClickListener(menuRenameOnClickListener);
                pop.getMenu().getItem(1).setIntent(intentDummy);
                pop.getMenu().getItem(2).setOnMenuItemClickListener(menuShowHistoryOnClickListener);
                pop.getMenu().getItem(2).setIntent(intentDummy);
                pop.show();
            }
        }
    };

    private MenuItem.OnMenuItemClickListener menuTriggerOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            item.getIntent().setClass(mContext, TriggerActivity.class);
            mContext.startActivity(item.getIntent()); // ID info is already in the intent
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuRenameOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View dialogView = inflater.inflate(R.layout.dialog_rename_sensor, null);
            int sensorId = Integer.parseInt(item.getIntent().getStringExtra("id"));
            final Sensor matchSensor = findSensorById(sensorId);

            builder.setTitle(R.string.dialog_title_rename_sensor) // TODO Resource-ify
                    .setView(dialogView)
                    .setNegativeButton(R.string.button_cancel, null);
            AlertDialog dialog = builder.create();

            final EditText et_name = (EditText) dialogView.findViewById(R.id.et_sensor_name);
            et_name.setText(matchSensor.getSensorName());

            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newName = et_name.getText().toString();
                    if (newName != null && !newName.equals(matchSensor.getSensorName())) {
                        try {
                            matchSensor.setSensorName(newName, true); // Ask to upload now.
                            // It's so difficult to refresh the things
                            // TODO Why can't we do this inside the adapter since it's carrying this role?
                            ((MainActivity) mContext).getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    ((MainActivity) mContext).loadCachedSensors();
                                }
                            });
                        } catch (final IOException e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext,
                                            mContext.getResources().getString(R.string.toast_error_renaming_sensor) +
                                                    e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            };

            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    mContext.getResources().getString(R.string.button_ok),
                    onClickListener);
            dialog.show();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuShowHistoryOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent();
            intent.setClass(mContext, SensorHistoryActivity.class);
            intent.putExtra("id", Integer.parseInt(item.getIntent().getStringExtra("id")));
            mContext.startActivity(intent);
            return true;
        }
    };

    DeviceListViewAdapter(Context context) {
        if (context != null)
            mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mSensors = new ArrayList<Sensor>();
    }

    public void addToSensors(Sensor new_sensors[]) {
        if (new_sensors == null) return;
        Collections.addAll(mSensors, new_sensors);
    }

    @Override
    public int getCount() {
        return mSensors.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > mSensors.size() - 1 || position < 0)
            return null;
        return mSensors.get(position);
    }

    @Override
    public long getItemId(int position) {
        // Is this needed by Cursors?
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.listitem_sensor_info_detail, null);
        TextView sensor_name = (TextView) convertView.findViewById(R.id.sensor_name);
        TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
        TextView status_message = (TextView) convertView.findViewById(R.id.status_message);
        TextView update_time = (TextView) convertView.findViewById(R.id.update_time);
        // ImageView image = (ImageView) convertView.findViewById(R.id.sensor_pic);
        ImageButton ib = (ImageButton) convertView.findViewById(R.id.sensor_popup_menu);
        TextView hidden_sensor_id = (TextView) convertView.findViewById(R.id.tv_sensor_id_invisible);

        Sensor this_sensor = mSensors.get(position);
        sensor_name.setText(this_sensor.getSensorName());
        device_name.setText("@" + this_sensor.getParentControlCenter().getDeviceName());
        status_message.setText(mContext.getResources().getText(R.string.current_value)
                + this_sensor.getSensorCachedValue());
        update_time.setText(DateUtils.getRelativeTimeSpanString(this_sensor.getLastUpdateDate() * 1000,
                System.currentTimeMillis(), 0));
        //image.setPic(this_sens.getType)
        hidden_sensor_id.setText(String.valueOf(this_sensor.getSensorId()));
        ib.setOnClickListener(menuOnClickListener);

        return convertView;
    }

    public void clearAll(boolean isShownImmediately) {
        mSensors.clear();
        if (isShownImmediately) this.notifyDataSetChanged();
    }

    /**
     * 与notifyDataSetChanged相同，就是多了一个手动设置emptyView显示与隐藏的功能
     * 用来解决setEmptyView后列表为空时SwipeRefresh手势动画不出现的问题
     *
     * @param v 要显示的emptyView，不要用setEmptyView
     */
    public void reloadDataWithEmptyView(View v) {
        this.notifyDataSetChanged();
        if (this.getCount() == 0)
            v.setVisibility(View.VISIBLE);
        else
            v.setVisibility(View.GONE);
    }

    /**
     * Note that this is only for internal use.
     * It only searches in this adapter's memory.
     * Useful for quick search when adapters info is loaded into adapter already.
     *
     * @param id ID to search
     * @return the ControlCenter found, or null
     */
    private Sensor findSensorById(int id) {
        for (Sensor snr : mSensors) {
            if (snr.getSensorId() == id) return snr;
        }
        return null;
    }
}
