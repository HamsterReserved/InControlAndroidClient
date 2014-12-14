package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DeviceListViewImpl extends BaseAdapter {

    /**
     * Items in List
     */
    private int mCount = 0;

    private Context mContext = null;

    private LayoutInflater mInflater = null;

    DeviceListViewImpl(Context context) {
        if (context != null)
            mContext = context;
        mInflater = LayoutInflater.from(mContext);

    }

    public void addItem() {
        //TODO: Implement me!
        mCount++;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
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
        //Fill in the View
        return convertView;
    }

}
