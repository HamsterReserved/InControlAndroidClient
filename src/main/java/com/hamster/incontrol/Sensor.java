package com.hamster.incontrol;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        this.setSensorName(mSensorName, false);
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
    public void setSensorName(String mSensorName, boolean upload) {
        this.mSensorName = mSensorName;
        if (upload) {
            if (this.isInfoComplete()) {
                // Upload to control center
            } else {
                throw new IllegalArgumentException("Sensor ID is not defined!");
            }
        }
    }

    public ControlCenter getParentControlCenter() {
        return mParentControlCenter;
    }

    public void setParentControlCenter(ControlCenter mParentControlCenter) {
        this.mParentControlCenter = mParentControlCenter;
    }

    public boolean isInfoComplete() {
        return mSensorId != INVALID_SENSOR_ID;
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
