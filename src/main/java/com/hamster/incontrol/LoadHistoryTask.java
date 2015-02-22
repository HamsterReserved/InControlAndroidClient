package com.hamster.incontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Hamster on 2015/2/20.
 * Same as the one in SensorHistoryActivity. I moved that here.
 */
class LoadHistoryTask extends AsyncTask<Sensor, Void, ArrayList<Sensor.SensorHistory>> {
    private Context mContext;
    private Runnable mRunOnSuccess;
    private ArrayList<Sensor.SensorHistory> mResult;

    LoadHistoryTask(Context ctx, Runnable runOnSucc) {
        mContext = ctx;
        mRunOnSuccess = runOnSucc;
    }

    @Override
    protected ArrayList<Sensor.SensorHistory> doInBackground(Sensor... params) {
        try {
            return NetworkAccessor.loadSensorHistory((Sensor) params[0], 50);
        } catch (IOException e) {
            return null; // TODO　Show detailed error toast here.
        } catch (IllegalStateException e) {
            return null; // TODO Same as above
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Sensor.SensorHistory> list) {
        if (list == null) {
            Toast.makeText(mContext, R.string.toast_error_loading_history, Toast.LENGTH_SHORT).show();
        } else {
            mResult = list;
            mRunOnSuccess.run();
        }
    }

    public ArrayList<Sensor.SensorHistory> getResult() {
        return mResult;
    }
}