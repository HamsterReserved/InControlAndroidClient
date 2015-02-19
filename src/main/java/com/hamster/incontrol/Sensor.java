package com.hamster.incontrol;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

/**
 * 储存一个传感器的信息，并可以查询其值（还能干嘛……）
 */
public class Sensor {

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

    public static class SensorHistory {
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

        public Date getValueDate() {
            return ValueDate;
        }

        public void setValueDate(Date valueDate) {
            ValueDate = valueDate;
        }

        /**
         * @param valueDate Needs to be Unix timestamp (seconds) not Java's milliseconds!
         */
        public void setValueDate(long valueDate) {
            ValueDate = new java.util.Date(valueDate * 1000);
        }

        public int getValue() {
            return Value;
        }

        public void setValue(int value) {
            Value = value;
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
    private String mTriggers; // 注意这个没在sensor list里，要自己获取才可以

    /**
     * @param parent_cc 所属ControlCenter
     */
    Sensor(ControlCenter parent_cc, Context ctx) {
        super();
        this.mParentControlCenter = parent_cc;
        this.mContext = ctx;
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
     * 若要上传，请先确定ID已设好，否则会报错IllegalArgument
     *
     * @param mSensorName 新的传感器名称
     * @param upload      是否上传到控制中心储存
     */
    public boolean setSensorName(String mSensorName, boolean upload) throws IOException {
        this.mSensorName = mSensorName;
        if (this.isInfoComplete()) {
            this.saveToDatabase(); // We are *changing* name, not just initializing
            if (upload) {
                return NetworkAccessor.uploadSensorName(this);
            }
        }
        return true;
    }

    public ControlCenter getParentControlCenter() {
        return mParentControlCenter;
    }

    public void setParentControlCenter(ControlCenter mParentControlCenter) {
        this.mParentControlCenter = mParentControlCenter;
    }


    public String getTriggerString() {
        return mTriggers;
    }

    public void setTriggerString(String mTriggers) {
        try {
            setTriggerString(mTriggers, false);
        } catch (IOException e) {
            // False won't cause exception
        }
    }

    public boolean setTriggerString(String mTriggerString, boolean upload) throws IOException {
        this.mTriggers = mTriggerString;
        if (this.isInfoComplete()) {
            this.saveToDatabase();
            if (upload) {
                return NetworkAccessor.uploadSensorTrigger(this);
            }
        }
        return true;
    }

    public boolean isInfoComplete() {
        return mSensorId != INVALID_SENSOR_ID &&
                mSensorType != null &&
                mParentControlCenter != null;
    }

    /**
     * @param na 已初始化有HomeDevice的NetworkAccessor实例
     * @throws IOException   网络错误
     * @throws JSONException JSON格式错误
     * @deprecated 查询指定传感器信息（不知道这是干嘛的，明明在ControlCenter已经有了刷新列表的功能）
     */
    public void update(NetworkAccessor na) throws IOException, JSONException {
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
