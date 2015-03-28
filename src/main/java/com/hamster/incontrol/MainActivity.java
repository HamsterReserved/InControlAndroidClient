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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {

    private static final String TAG = "InControl_MainActivity";
    private Handler mHandler = new Handler();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DeviceListViewAdapter mListAdapter;
    private View mEmptyView;
    private boolean isFirstLaunch = true; // for refresh only once (like onResume) in onWindowFocusChanged

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                refreshSensorList(true);
            }
        });

        ListView lv = (ListView) mSwipeRefreshLayout.findViewById(R.id.device_list);
        mListAdapter = new DeviceListViewAdapter(this);
        lv.setAdapter(mListAdapter);
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
            refreshSensorList(false);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), ControlCenterActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // To let the load animation show on 1st load
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFirstLaunch) {
            refreshSensorList(false);
            isFirstLaunch = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // SDK Documentation suggest loading data here. At least not in onCreate

        // Update TextView
        LocalConfigStore lcs = new LocalConfigStore(getApplicationContext());
        boolean isNoDevice = lcs.getControlCenters() == null;
        lcs.close();

        mEmptyView = ((FrameLayout) mSwipeRefreshLayout.getParent())
                .findViewById(R.id.tv_empty_device_list);
        if (isNoDevice) {
            ((TextView) mEmptyView).setText(R.string.text_empty_centers);
        } else {
            ((TextView) mEmptyView).setText(R.string.text_loading);
        }
        // lv.setEmptyView(mEmptyView);
        // This will cause Swipe indicator to disappear if list is empty

        loadCachedSensors(); // 显示缓存的传感器数据
        isFirstLaunch = true;
    }

    private void refreshSensorList(boolean isCalledFromSwipe) {
        Log.v(TAG, "refreshSensorList called, isCalledFromSwipe=" + isCalledFromSwipe);

        if (!isCalledFromSwipe) mSwipeRefreshLayout.setRefreshing(true);
        new RefreshDataBackgroundTask().execute();
    }

    public void loadCachedSensors() {
        LocalConfigStore lcs = new LocalConfigStore(this.getApplicationContext());
        final ControlCenter[] ccs = lcs.getControlCenters(); // This is local only

        if (ccs == null) {
            lcs.close();
            return;
        }

        mListAdapter.clearAll(false);
        for (final ControlCenter cc : ccs) {
            mListAdapter.addToSensors(lcs.getSensors(cc));
        }
        mListAdapter.reloadDataWithEmptyView(mEmptyView);

        lcs.close();
    }

    public Handler getHandler() {
        return mHandler;
    } // Yes I'm crazy

    private void onRefreshComplete(Sensor[] snrs) {
        Log.v(TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        if (snrs != null) { // Do not erase loaded cache sensors if no new ones
            mListAdapter.clearAll(true);
            mListAdapter.addToSensors(snrs);
            mListAdapter.reloadDataWithEmptyView(mEmptyView);
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
            lcs.close();

            if (ccs == null) return null;
            for (ControlCenter cc : ccs) {
                try {
                    cc.updateSensors();
                } catch (final Exception e) {
                    Log.e(TAG, "updateSensors encountered error! Msg:" + e.getLocalizedMessage());
                    e.printStackTrace();
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
                Collections.addAll(snr_list, cc.getSensors());
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
