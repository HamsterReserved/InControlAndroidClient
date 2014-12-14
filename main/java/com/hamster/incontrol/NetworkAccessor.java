package com.hamster.incontrol;

/**
 * Created by Hamster on 2014/12/14.
 * <p/>
 * 用来从云端读取主机数据的类
 */
public class NetworkAccessor {
    private final int DEVICE_TYPE = 2; // 设备类型是客户端，不可修改
    /**
     * 查询所有传感器
     */
    public int REQUEST_TYPE_QUERY_SENSORS = 1;
    /**
     * 查询某一传感器的具体值
     */
    public int REQUEST_TYPE_QUERY_SENSOR_INFO = 2;

    /**
     * 主机序列号（用int真的好么？）
     */
    private int mDeviceId = 0;
    /**
     * 用户设定的密码，有可能不需要
     */
    private String mCredentials = null;

    public void setDeviceId(int device_id) {
        mDeviceId = device_id;
    }

    public void setCredentials(String credentials) {
        mCredentials = credentials;
    }
}
