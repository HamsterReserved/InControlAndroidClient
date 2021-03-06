package com.hamster.incontrol;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Hamster on 2015/2/19.
 * 用来做与网络相关的AsyncTask，只支持判断返回为boolean型的
 */
class NetworkBackgroundOperator {
    private static final String TAG = "InContro_NetBG";
    private BackgroundTaskDesc mBGTaskDesc;

    public static enum Operation {
        OPERATION_RENAME_SENSOR,
        OPERATION_RENAME_DEVICE,
        OPERATION_UPLOAD_SENSOR_TRIGGER,
        OPERATION_USER_REGISTRATION
    }

    public static class BackgroundTaskDesc {
        public Operation mOperation;
        public Object[] mParams;
        public Runnable mRunOnSuccess;
        public Runnable mRunOnFail;

        BackgroundTaskDesc(Operation op, Object[] param, Runnable onSucc, Runnable onFail) {
            mOperation = op;
            mParams = param;
            mRunOnSuccess = onSucc;
            mRunOnFail = onFail;
        }
    }

    NetworkBackgroundOperator(BackgroundTaskDesc taskDesc) {
        mBGTaskDesc = taskDesc;
    }

    public void execute() {
        new NetworkBGTask().execute(mBGTaskDesc);
    }

    private class NetworkBGTask extends AsyncTask<BackgroundTaskDesc, Void, Boolean> {
        private BackgroundTaskDesc mBGTask;

        @Override
        protected Boolean doInBackground(BackgroundTaskDesc... params) {
            mBGTask = params[0];
            switch (params[0].mOperation) {
                case OPERATION_RENAME_DEVICE:
                    return renameDevice((ControlCenter) params[0].mParams[0]);
                case OPERATION_RENAME_SENSOR:
                    return renameSensor((Sensor) params[0].mParams[0]);
                case OPERATION_UPLOAD_SENSOR_TRIGGER:
                    return uploadSensorTrigger((Sensor) params[0].mParams[0]);
                case OPERATION_USER_REGISTRATION:
                    return userRegistration((Integer) params[0].mParams[0],
                            (String) params[0].mParams[1]);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mBGTask.mRunOnSuccess != null) mBGTask.mRunOnSuccess.run();
            } else {
                if (mBGTask.mRunOnFail != null) mBGTask.mRunOnFail.run();
            }
        }

        private boolean renameDevice(ControlCenter cc) {
            if (cc.isInfoComplete()) {
                try {
                    return NetworkAccessor.uploadDeviceName(cc);
                } catch (IOException e) {
                    Log.e(TAG, "renameDevice failed with " + e.getLocalizedMessage());
                }
            }
            return false;
        }

        private boolean renameSensor(Sensor snr) {
            if (snr.isInfoComplete()) {
                try {
                    return NetworkAccessor.uploadSensorName(snr);
                } catch (IOException e) {
                    Log.e(TAG, "renameSensor failed with " + e.getLocalizedMessage());
                }
            }
            return false;
        }

        private boolean uploadSensorTrigger(Sensor snr) {
            if (snr.isInfoComplete()) {
                try {
                    return NetworkAccessor.uploadSensorTrigger(snr);
                } catch (IOException e) {
                    Log.e(TAG, "uploadSensorTrigger failed with " + e.getLocalizedMessage());
                }
            }
            return false;
        }

        private boolean userRegistration(int device_id, String name) {
            try {
                return NetworkAccessor.userRegistration(device_id, name);
            } catch (IOException e) {
                Log.e(TAG, "userRegistration failed with " + e.getLocalizedMessage());
            }
            return false;
        }
    }
}
