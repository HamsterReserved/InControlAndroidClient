package com.hamster.incontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends Activity {


    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        ListView lv = (ListView) findViewById(R.id.device_list);
        lv.setAdapter(new DeviceListViewAdapter(getApplicationContext()));


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
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshSensorList() {
        final DeviceListViewAdapter la = (DeviceListViewAdapter) ((ListView) findViewById(R.id.device_list)).getAdapter();
        final ControlCenter cc = new ControlCenter(1, null);

        la.clearAll();
        new Thread() {
            @Override
            public void run() { // Code to run in thread
                // TODO Auto-generated method stub
                super.run();
                try {
                    cc.updateSensors();
                    handler.post(new Runnable() { // Code to run in main UI
                        @Override
                        public void run() {
                            la.addToSensors(cc.getSensors());
                            la.notifyDataSetChanged();
                        }
                    });

                } catch (IOException e) {
                    // TODO 这真的很重要！
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
