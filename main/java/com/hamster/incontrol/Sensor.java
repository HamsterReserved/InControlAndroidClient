package com.hamster.incontrol;

/**
 * 储存一个传感器的信息，并可以查询其值（还能干嘛……）
 */
public class Sensor {
    /**
     * 默认的无效传感器ID，表明此实例还没有初始化
     */
    public static final int INVALID_SENSOR_ID = -1;

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

    Sensor(int sensor_id) {
        super();
        this.mSensorId = sensor_id;
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

    public String getSensorCachedValue() {
        return mSensorCachedValue;
    }

    public boolean isInfoComplete() {
        if (mSensorId == INVALID_SENSOR_ID)
            return false;

        return true;
    }

    public void updateSensorValue() {
        if (this.isInfoComplete()) {
            // 真的是void吗？在这里可以阻塞访问网络倒是了……
        } else {
            throw new IllegalArgumentException("Sensor ID is not defined!");
        }
    }
}
