package com.hamster.incontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;


public class SensorHistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_history);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_sensor_history, menu); We don't need menu here.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        // return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LocalConfigStore lcs = new LocalConfigStore(this);
        int sensor_id = this.getIntent().getIntExtra("id", Sensor.INVALID_SENSOR_ID);

        try {
            ArrayList<Sensor.SensorHistory> list = NetworkAccessor.loadSensorHistory(lcs.getSensorById(sensor_id), 50);
            if (list == null) return;

            DataPoint points[] = new DataPoint[list.size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = new DataPoint(list.get(i).getValueDate(), list.get(i).getValue());
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
            series.setDrawBackground(true);
            graph.addSeries(series);
        } catch (IOException e) {
            Toast.makeText(this, getResources().getString(R.string.toast_error_loading_history)
                    + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
