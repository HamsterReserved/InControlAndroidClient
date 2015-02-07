package com.hamster.incontrol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView) findViewById(R.id.device_list);
        lv.setAdapter(new DeviceListViewAdapter(getApplicationContext()));

        TextView empty_tv = new TextView(this.getApplicationContext());
        ((ViewGroup) lv.getParent()).addView(empty_tv); // This is the key to get setEmptyView working.
        empty_tv.setText(R.string.text_empty_centers);
        empty_tv.setLayoutParams(
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)); // Auto-select FrameLayout
        empty_tv.setGravity(Gravity.CENTER);
        empty_tv.setTextColor(Color.GRAY);
        empty_tv.setTextSize(22);
        lv.setEmptyView(empty_tv);

        refreshSensorList();
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
        final DeviceListViewAdapter la = (DeviceListViewAdapter) ((ListView) findViewById(R.id.device_list)).getAdapter();
        LocalConfigStore lcs = new LocalConfigStore(this.getApplicationContext());
        lcs.open();
        final ControlCenter[] ccs = lcs.getControlCenters(); // This is local only
        lcs.close();

        if (ccs == null) return;
        new Thread() {
            @Override
            public void run() { // Code to run in thread
                // TODO Auto-generated method stub
                super.run();
                for (int i = 0; i < ccs.length; ++i) {
                    final ControlCenter cc = ccs[i];
                    try {
                        cc.updateSensors();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error when retreiving sensor info@!", Toast.LENGTH_SHORT);
                        continue;
                    }
                    handler.post(new Runnable() { // Code to run in main UI
                        @Override
                        public void run() {
                            la.clearAll();
                            la.addToSensors(cc.getSensors());
                            la.notifyDataSetChanged();
                        }
                    });
                }
            }
        }.start();
    }
}
