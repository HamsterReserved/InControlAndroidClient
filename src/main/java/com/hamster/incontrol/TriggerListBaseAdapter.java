package com.hamster.incontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Hamster on 2015/2/22.
 * Base class for listing actions and conditions
 */
public class TriggerListBaseAdapter<E> extends BaseAdapter {
    private ArrayList<E> mList;
    private Context mContext;

    TriggerListBaseAdapter(Context ctx) {
        mContext = ctx;
        mList = new ArrayList<>();
    }

    public void addItem(E item) {
        mList.add(item);
    }

    public void addAllItems(E[] items) {
        Collections.addAll(mList, items);
    }

    public void addAllItems(ArrayList<E> items) {
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
        tv.setText(mList.get(position).toString());

        return convertView;
    }

}
