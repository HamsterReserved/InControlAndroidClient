package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Hamster on 2015/2/22.
 * Base class for listing actions and conditions
 */
public class TriggerListBaseAdapter extends BaseAdapter {
    private ArrayList<Describable> mList;
    private Context mContext;

    TriggerListBaseAdapter(Context ctx) {
        mContext = ctx;
        mList = new ArrayList<>();
    }

    public void addItem(Describable item) {
        mList.add(item);
    }

    public void addAllItems(Describable[] items) {
        Collections.addAll(mList, items);
    }

    public void addAllItems(ArrayList<Describable> items) {
        mList.addAll(items);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater li = LayoutInflater.from(mContext);
            convertView = li.inflate(R.layout.text_in_card_view, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tv_text_in_card);
        tv.setText(mList.get(position).getDescription());

        return convertView;
    }

    public void clearAll() {
        mList.clear();
    }
}
