package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DeviceListViewImpl extends BaseAdapter {

    /**
     * Items in List
     */
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private List<Sensor> mSensors;

    DeviceListViewImpl(Context context) {
        if (context != null)
            mContext = context;
        mInflater = LayoutInflater.from(mContext);

    }

    public void addToSensors(Sensor new_sensors[]) {
        if (new_sensors == null) return;
        for (int i = 0; i < new_sensors.length; ++i) {
            mSensors.add(new_sensors[i]);
        }
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO: Implement!
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.device_info_detail, null);
        TextView sensor_name = (TextView) convertView.findViewById(R.id.sensor_name);
        TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
        TextView status_message = (TextView) convertView.findViewById(R.id.status_message);
        TextView update_time = (TextView) convertView.findViewById(R.id.update_time);
        ImageView image = (ImageView) convertView.findViewById(R.id.device_pic);

        Sensor this_sensor = mSensors.get(position);
        sensor_name.setText(this_sensor.getSensorName());
        device_name.setText("From Device: Test");
        status_message.setText("Current value: " + this_sensor.getSensorCachedValue());
        update_time.setText(TimeStamp2Date(this_sensor.getLastUpdateDate()));
        //image.setPic(this_sens.getType)

        return convertView;
    }

    public String TimeStamp2Date(long timestamp) {
        String date = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date(timestamp));
        return date;
    }
}
