package com.hamster.incontrol;

import android.content.Context;
import android.widget.Toast;

import com.hamster.incontrol.NetworkBackgroundOperator.BackgroundTaskDesc;
import com.hamster.incontrol.NetworkBackgroundOperator.Operation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

/**
 * 储存一个传感器的信息，并可以查询其值（还能干嘛……）
 */
class Sensor {

    /******************常量和内部枚举********************/

    /**
     * 传感器类型，从0起始，后续有待添加。
     * 请保持与云端PHP、单片机程序中的int值一致！
     */
    public static enum SensorType {
        SENSOR_LIGHT,
        SENSOR_ELECTRICITY,
        SENSOR_MOTION,
        SENSOR_SWITCH,
        SENSOR_IR,
        SENSOR_UNKNOWN
    }

    public static class SensorHistory implements Comparable<SensorHistory> {
        private Date ValueDate;
        private int Value;

        SensorHistory(Date date, int value) {
            ValueDate = date;
            Value = value;
        }

        /**
         * @param date Needs to be Unix timestamp (seconds) not Java's milliseconds!
         */
        SensorHistory(Long date, int value) {
            ValueDate = new Date(date * 1000);
            Value = value;
        }

        @Override
        public int compareTo(SensorHistory another) {
            if (ValueDate.equals(another.getValueDate()))
                return 0;
            else if (ValueDate.getTime() > another.getValueDate().getTime())
                return 1;
            else
                return -1;
        }

        public Date getValueDate() {
            return ValueDate;
        }

        public int getValue() {
            return Value;
        }
    }

    /**
     * 默认的无效传感器ID，表明此实例还没有初始化
     */
    public static final int INVALID_SENSOR_ID = -1;

    /**************************类成员*************************/

    /**
     * 传感器ID，必须
     */
    private int mSensorId = INVALID_SENSOR_ID;
    /**
     * 传感器名称，可以储存在控制中心里，也可以暂时保存在手机上
     *
     * @see Sensor#setSensorName(String)
     * @see Sensor#setSensorName(String, boolean)
     */
    private String mSensorName;
    private SensorType mSensorType;
    private String mSensorCachedValue;
    private long mLastUpdateDate;
    private ControlCenter mParentControlCenter;
    private Context mContext;
    private Trigger mTrigger;

    /**
     * @param parent_cc 所属ControlCenter
     */
    Sensor(ControlCenter parent_cc, Context ctx) {
        super();
        this.mParentControlCenter = parent_cc;
        this.mContext = ctx;
        this.mTrigger = new Trigger(mContext, "", this); // To avoid NPE
    }

    public static SensorType convertIntToType(int conv) {
        try {
            return SensorType.values()[conv];
        } catch (ArrayIndexOutOfBoundsException e) {
            return SensorType.SENSOR_UNKNOWN;
        }
    }

    public int getSensorId() {
        return mSensorId;
    }

    public void setSensorId(int mSensorId) {
        this.mSensorId = mSensorId;
    }

    public int getSensorTypeInt() {
        return mSensorType.ordinal();
    }

    public SensorType getSensorType() {
        return mSensorType;
    }

    public void setSensorType(SensorType mSensorType) {
        this.mSensorType = mSensorType;
    }

    public String getSensorName() {
        return mSensorName;
    }

    /**
     * 设置传感器名称（默认不上传）
     *
     * @param mSensorName 目标传感器名称
     */
    public void setSensorName(String mSensorName) {
        try {
            this.setSensorName(mSensorName, false);
        } catch (IOException e) {
            // If the 2nd param is false, it won't cause any exception.
        }
    }

    public void setSensorCachedValue(String mSensorCachedValue) {
        this.mSensorCachedValue = mSensorCachedValue;
    }


    public String getSensorCachedValue() {
        return mSensorCachedValue;
    }

    public void setLastUpdateDate(int dt) {
        this.mLastUpdateDate = dt;
    }

    public long getLastUpdateDate() {
        return mLastUpdateDate;
    }

    /**
     * 设置传感器名称（上传）
     * <p/><p/>
     * 若要上传，请先确定ID已设好，否则会报错IllegalArgument，并且要多开一个AsyncTask
     *
     * @param mSensorName 新的传感器名称
     * @param upload      是否上传到控制中心储存
     */
    public boolean setSensorName(String mSensorName, boolean upload) throws IOException {
        this.mSensorName = mSensorName;
        if (this.isInfoComplete() && upload) {
            this.saveToDatabase(); // We are *changing* name, not just initializing
            BackgroundTaskDesc task =
                    new BackgroundTaskDesc(Operation.OPERATION_RENAME_SENSOR,
                            this,
                            null,
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext,
                                            mContext.getResources().getString(R.string.toast_error_renaming_sensor),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
            NetworkBackgroundOperator op = new NetworkBackgroundOperator(task);
            op.execute();
        }
        return true;
    }

    public ControlCenter getParentControlCenter() {
        return mParentControlCenter;
    }

    public void setParentControlCenter(ControlCenter mParentControlCenter) {
        this.mParentControlCenter = mParentControlCenter;
    }

    public Trigger getTriggerInstance() {
        return mTrigger;
    }

    public void setTriggerString(String mTriggers) {
        try {
            setTriggerString(mTriggers, false);
        } catch (IOException e) {
            // False won't cause exception
        }
    }

    public boolean setTriggerString(String mTriggerString, boolean upload) throws IOException {
        if (mTriggerString != null && !mTriggerString.equals("")) {
            mTrigger = new Trigger(mContext, mTriggerString, this);
            if (this.isInfoComplete() && upload) {
                uploadTriggerSetting();
            }
        }
        return true;
    }

    // If we use Trigger instance to change act/cond, we need this to upload
    public void uploadTriggerSetting() {
        this.saveToDatabase();
        BackgroundTaskDesc task =
                new BackgroundTaskDesc(Operation.OPERATION_UPLOAD_SENSOR_TRIGGER,
                        this,
                        null,
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,
                                        mContext.getResources().getString
                                                (R.string.toast_error_upload_trigger),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
        NetworkBackgroundOperator op = new NetworkBackgroundOperator(task);
        op.execute();
    }

    public boolean isInfoComplete() {
        return mSensorId != INVALID_SENSOR_ID &&
                mSensorType != null &&
                mParentControlCenter != null;
    }

    /**
     * @throws IOException   网络错误
     * @throws JSONException JSON格式错误
     * @deprecated 查询指定传感器信息（不知道这是干嘛的，明明在ControlCenter已经有了刷新列表的功能）
     */
    public void update() throws IOException, JSONException {
        if (this.isInfoComplete()) {
            JSONObject json;

            json = NetworkAccessor.fetchSensorInfoJSON(mSensorId, mParentControlCenter);
            this.setSensorName(json.getString(NetworkAccessor.JSON_SENSOR_NAME_KEY), false);
            this.setSensorType(convertIntToType(json.getInt(NetworkAccessor.JSON_SENSOR_TYPE_KEY)));
            mSensorCachedValue = json.getString(NetworkAccessor.JSON_SENSOR_VALUE_KEY);
            mLastUpdateDate = System.currentTimeMillis();
        } else {
            throw new IllegalArgumentException("Sensor ID not defined!");
        }
    }

    public void saveToDatabase() {
        LocalConfigStore lcs = new LocalConfigStore(this.mContext);
        lcs.updateSensor(this, Sensor.INVALID_SENSOR_ID);
        lcs.close();
    }
}
