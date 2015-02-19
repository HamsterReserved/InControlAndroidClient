package com.hamster.incontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hamster.incontrol.Sensor.SensorHistory;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class SensorHistoryActivity extends Activity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_history);
        mContext = this;
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
    public void onWindowFocusChanged(boolean hasFoucs) {
        if (hasFoucs) {
            LocalConfigStore lcs = new LocalConfigStore(this);
            int sensor_id = this.getIntent().getIntExtra("id", Sensor.INVALID_SENSOR_ID);

            LoadHistoryTask task = new LoadHistoryTask();
            task.execute(lcs.getSensorById(sensor_id));
        }
    }

    private void loadListToGraph(ArrayList<SensorHistory> list) {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        DataPoint points[] = new DataPoint[list.size()];
        Collections.sort(list);

        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(
                    list.get(i).getValueDate().getTime(),
                    list.get(i).getValue());
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);

        Paint my_paint = new Paint();
        my_paint.setAntiAlias(true);
        my_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        my_paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        my_paint.setStrokeWidth(5);
        series.setCustomPaint(my_paint);

        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setDrawBackground(true);
        series.setBackgroundColor(getResources().getColor(R.color.colorPrimary) & 0x22ffffff); // Adjust transparency

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.addSeries(series);
    }

    private class LoadHistoryTask extends AsyncTask<Sensor, Void, ArrayList<SensorHistory>> {

        @Override
        protected ArrayList<SensorHistory> doInBackground(Sensor... params) {
            try {
                return NetworkAccessor.loadSensorHistory((Sensor) params[0], 50);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<SensorHistory> list) {
            if (list == null) {
                Toast.makeText(mContext, R.string.toast_error_loading_history, Toast.LENGTH_SHORT).show();
            } else {
                loadListToGraph(list);
            }
        }
    }
}
