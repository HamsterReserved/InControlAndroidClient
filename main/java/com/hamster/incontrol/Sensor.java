package com.hamster.incontrol;

import org.json.JSONArray;
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
    public enum SensorType {
        SENSOR_LIGHT,
        SENSOR_ELECTRICITY,
        SENSOR_MOTION,
        SENSOR_SWITCH,
        SENSOR_IR,
        SENSOR_UNKNOWN
    }

    private static final String JSON_SENSOR_ID_KEY = "sensor_id";
    private static final String JSON_SENSOR_NAME_KEY = "sensor_name";
    private static final String JSON_SENSOR_TYPE_KEY = "sensor_type";
    private static final String JSON_SENSOR_VALUE_KEY = "sensor_info";

    /**
     * 默认的无效传感器ID，表明此实例还没有初始化
     */
    public static final int INVALID_SENSOR_ID = -1;

    /**************************类成员*************************/

    /**
     * 传感器ID，必须，只能通过构造函数传入，但可以读出
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

    Sensor(int sensor_id) {
        super();
        this.mSensorId = sensor_id;
    }

    public static SensorType convertIntToType(int conv) {
        try {
            return SensorType.values()[conv];
        } catch (ArrayIndexOutOfBoundsException e) {
            return SensorType.SENSOR_UNKNOWN;
        }
    }

    /**
     * 查询所有可用的传感器
     *
     * @param na         已初始化有device_id的NetworkAccessor实例
     * @param sensorType （不是必须）需要的传感器类型，可以过滤用
     * @return 传感器数组
     */
    public static Sensor[] getSensorList(NetworkAccessor na, SensorType sensorType) throws IOException, JSONException {
        JSONArray jsonArray = na.updateSensorListJSON();

        Sensor[] sensors = new Sensor[jsonArray.length()];
        for (int i = 0; i < sensors.length; ++i) {
            if (sensorType != null) {
                if (convertIntToType(jsonArray.getJSONObject(i).getInt(JSON_SENSOR_TYPE_KEY)) == sensorType) {
                    sensors[i] = new Sensor(jsonArray.getJSONObject(i).getInt(JSON_SENSOR_ID_KEY));
                    sensors[i].setSensorType(sensorType);
                    sensors[i].setSensorName(jsonArray.getJSONObject(i).getString(JSON_SENSOR_NAME_KEY));
                }
            } else {
                sensors[i] = new Sensor(jsonArray.getJSONObject(i).getInt(JSON_SENSOR_ID_KEY));
                sensors[i].setSensorType(convertIntToType(jsonArray.getJSONObject(i).getInt(JSON_SENSOR_TYPE_KEY)));
                sensors[i].setSensorName(jsonArray.getJSONObject(i).getString(JSON_SENSOR_NAME_KEY));
            }
        }

        return sensors;
    }

    public int getSensorTypeInt() {
        return mSensorType.ordinal();
    }

    public SensorType getSensorType() {
        return mSensorType;
    }

    public void setSensorType(SensorType mSensorType) {
        System.out.println(mSensorType.toString());
        this.mSensorType = mSensorType;
    }

    public int getSensorId() {
        return mSensorId;
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

    public String getSensorCachedValue() {
        return mSensorCachedValue;
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

    public boolean isInfoComplete() {
        return mSensorId != INVALID_SENSOR_ID;
    }

    /**
     * 查询指定传感器信息D
     *
     * @param na 已初始化有Homeevice的NetworkAccessor实例
     * @throws IOException   网络错误
     * @throws JSONException JSON格式错误
     */
    public void update(NetworkAccessor na) throws IOException, JSONException {
        if (this.isInfoComplete()) {
            JSONObject json;
            json = na.updateSensorInfoJSON(mSensorId);
            this.setSensorName(json.getString(JSON_SENSOR_NAME_KEY), false);
            this.setSensorType(convertIntToType(json.getInt(JSON_SENSOR_TYPE_KEY)));
            mSensorCachedValue = json.getString(JSON_SENSOR_VALUE_KEY);
            mLastUpdateDate = System.currentTimeMillis();
        } else {
            throw new IllegalArgumentException("Sensor ID not defined!");
        }
    }
}
