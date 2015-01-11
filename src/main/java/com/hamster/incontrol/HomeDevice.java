package com.hamster.incontrol;

/**
 * Created by Hamster on 2015/1/11.
 * 描述一台主机
 */
public class HomeDevice {
    public static int INVALID_DEVICE_ID = -1;

    /**
     * 主机序列号（用int真的好么？）
     */
    private int device_id = INVALID_DEVICE_ID;
    /**
     * 用户设定的密码，有可能不需要
     */
    private String credentials;
    private String device_name;
    private Sensor sensors[];

    HomeDevice(int device_id, String credentials) {
        this.device_id = device_id;
        this.credentials = credentials;
    }

    public boolean isInfoComplete() {
        if (this.device_id == INVALID_DEVICE_ID)
            return false;
        return true;
    }

    // 以下是自动生成的，Android Studio大法好
    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int device_id) {
        this.device_id = device_id;
    }

    public String getDeviceName() {
        return device_name;
    }

    public void setDeviceName(String device_name) {
        this.device_name = device_name;
    }

    public Sensor[] getSensors() {
        return sensors;
    }

    public void setSensors(Sensor[] sensors) {
        this.sensors = sensors;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

}
