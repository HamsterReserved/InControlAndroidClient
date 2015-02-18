package com.hamster.incontrol;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Hamster on 2015/1/11.
 * 描述一台主机
 */
public class ControlCenter {
    public static final int INVALID_DEVICE_ID = -1;
    public static final int INVALID_DATE = -1;

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
    private Context context;
    private int man_date = INVALID_DATE; // Manufacturing date. Need to be fetched by updateSelfInfo
    private int reg_date = INVALID_DATE; // Nearest registering date. Same as above.

    ControlCenter(int device_id, String credentials, Context ctx) {
        this.device_id = device_id;
        this.credentials = credentials;
        this.context = ctx;
    }

    // 用它反过来操作数据库
    ControlCenter(Context ctx) {
        this.context = ctx;
    }

    public boolean isInfoComplete() {
        return !(this.device_id == INVALID_DEVICE_ID);
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
        this.sensors = sensors; // This is usually set by updateSensors. No need to add here...
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public int getRegDate() {
        return reg_date;
    }

    public void setRegDate(int reg_date) {
        this.reg_date = reg_date;
    }

    public int getManDate() {
        return man_date;
    }

    public void setManDate(int man_date) {
        this.man_date = man_date;
    }

    /**
     * 更新此ControlCenter下的所有传感器
     */
    public void updateSensors() throws IOException, JSONException {
        JSONArray jsonArray = NetworkAccessor.fetchSensorListJSON(this);

        this.sensors = null; // To keep that fresh w/o memory leak?
        this.sensors = new Sensor[jsonArray.length()];
        for (int i = 0; i < sensors.length; ++i) {
            sensors[i] = new Sensor(this, this.context);
            sensors[i].setSensorId(jsonArray.getJSONObject(i).getInt(NetworkAccessor.JSON_SENSOR_ID_KEY));
            sensors[i].setSensorType(Sensor.convertIntToType(jsonArray.getJSONObject(i).getInt(NetworkAccessor.JSON_SENSOR_TYPE_KEY)));
            sensors[i].setSensorName(jsonArray.getJSONObject(i).getString(NetworkAccessor.JSON_SENSOR_NAME_KEY));
            sensors[i].setSensorCachedValue(jsonArray.getJSONObject(i).getString(NetworkAccessor.JSON_SENSOR_VALUE_KEY));
            sensors[i].setLastUpdateDate(jsonArray.getJSONObject(i).getInt(NetworkAccessor.JSON_SENSOR_DATE_KEY));
            sensors[i].saveToDatabase();
        }
    }
}
