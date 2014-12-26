package com.hamster.incontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends Activity {

    private DeviceListViewImpl List_adapter;
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        List_adapter = new DeviceListViewImpl(getApplicationContext());
        ListView lv = (ListView) findViewById(R.id.device_list);
        lv.setAdapter(List_adapter);


        new Thread() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
                NetworkAccessor na = new NetworkAccessor(1);
                try {
                    Sensor[] sensors = Sensor.getSensorList(na, null);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
        if (id == R.id.action_settings) {
            List_adapter.addItem();
            List_adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
