package com.hamster.incontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private Handler mHandler = new Handler();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DeviceListViewAdapter mListAdapter;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                //initiateRefresh();
                refreshSensorList();
            }
        });

        ListView lv = (ListView) mSwipeRefreshLayout.findViewById(R.id.device_list);
        mListAdapter = new DeviceListViewAdapter(getApplicationContext());
        lv.setAdapter(mListAdapter);

        LocalConfigStore lcs = new LocalConfigStore(getApplicationContext());
        boolean isNoDevice = lcs.getControlCenters() == null;
        lcs.close();

        TextView empty_tv = (TextView) ((FrameLayout) mSwipeRefreshLayout.getParent()).findViewById(R.id.tv_empty_device_list);
        if (isNoDevice) {
            empty_tv.setText(R.string.text_empty_centers);
        } else {
            empty_tv.setText(R.string.text_loading);
        }
        lv.setEmptyView(empty_tv); //This will cause Swipe icon to disappear if empty

        loadCachedSensors(); // 显示缓存的传感器数据
        refreshSensorList(); // 从网络获取传感器数据
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //List_adapter.addItem();
            mSwipeRefreshLayout.setRefreshing(true);
            refreshSensorList();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), ControlCenterActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshSensorList() {
        //Log.i(LOG_TAG, "initiateRefresh");

        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new RefreshDataBackgroundTask().execute();
    }

    private void loadCachedSensors() {
        final DeviceListViewAdapter la = (DeviceListViewAdapter) ((ListView) mSwipeRefreshLayout.findViewById(R.id.device_list)).getAdapter();
        LocalConfigStore lcs = new LocalConfigStore(this.getApplicationContext());
        final ControlCenter[] ccs = lcs.getControlCenters(); // This is local only

        if (ccs == null) {
            lcs.close();
            return;
        }

        la.clearAll(false);
        for (final ControlCenter cc : ccs) {
            la.addToSensors(lcs.getSensors(cc));
        }
        la.notifyDataSetChanged();
    }

    private void onRefreshComplete(Sensor[] snrs) {
        // Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        if (snrs != null) { // Do not erase loaded cache sensors if no new ones
            mListAdapter.clearAll(true);
            mListAdapter.addToSensors(snrs);
            mListAdapter.notifyDataSetChanged();
        }

        // Stop the refreshing indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class RefreshDataBackgroundTask extends AsyncTask<Void, Void, Sensor[]> {

        @Override
        protected Sensor[] doInBackground(Void... params) {
            ArrayList<Sensor> snr_list = new ArrayList<Sensor>();
            LocalConfigStore lcs = new LocalConfigStore(getApplicationContext());
            final ControlCenter[] ccs = lcs.getControlCenters(); // This is local only

            if (ccs == null) return null;
            for (ControlCenter cc : ccs) {
                try {
                    cc.updateSensors();
                } catch (final Exception e) {
                    Log.e(TAG, "updateSensors encountered error! Msg:" + e.getLocalizedMessage());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    e.getLocalizedMessage(), // TODO this is hardcoded in NetworkAccessor in Chinese
                                    Toast.LENGTH_SHORT).show();
                            loadCachedSensors();
                        }
                    });
                    return null; // We don't want incomplete data to go through. Just show cached.
                }
                for (Sensor snr : cc.getSensors()) {
                    snr_list.add(snr);
                }
            }

            // Sensor ret_snr[] = snr_list.toArray()
            Sensor ret_snr[] = new Sensor[snr_list.size()];
            for (int i = 0; i < snr_list.size(); i++)
                ret_snr[i] = snr_list.get(i);
            return ret_snr;
        }

        @Override
        protected void onPostExecute(Sensor[] result) {
            super.onPostExecute(result);

            onRefreshComplete(result);
        }

    }
}
