package com.hamster.incontrol;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hamster on 2014/12/14.
 * <p/>
 * 用来从云端读取主机数据的类
 */
class NetworkAccessor {
    private static final String LOG_TAG = "InControl_NA";
    /**
     * API地址
     */
    //public static final String INCONTROL_API_URL = "http://incontrol.sinaapp.com/incontrol_api.php";
    public static final String INCONTROL_API_URL = "http://192.168.137.1/incontrol/incontrol/incontrol_api.php";

    /**
     * 设备类型，这个数值应与PHP保持一致，不能修改
     */
    private static final int DEVICE_TYPE = 2;

    /**
     * 查询所有传感器
     */
    public static final int REQUEST_TYPE_QUERY_SENSORS_LIST = 1;
    /**
     * @deprecated PHP已经去除此选项，将导致错误
     * 查询某一传感器的具体值
     */
    public static final int REQUEST_TYPE_QUERY_SENSOR_INFO = 2;
    /**
     * 查询传感器数据历史（结果可以用Map存放）
     */
    public static final int REQUEST_TYPE_QUERY_SENSOR_HISTORY = 3;
    /**
     * 查询设备信息（已有的名称啥的）
     */
    public static final int REQUEST_TYPE_QUERY_DEVICE_INFO = 4;
    /**
     * 设置类参数的起始值
     */
    public static final int REQUEST_TYPE_SET_BASE = 100;
    /**
     * 设置控制中心名
     */
    public static final int REQUEST_TYPE_SET_DEVICE_NAME = REQUEST_TYPE_SET_BASE + 1;
    /**
     * 设置传感器的触发器（字符串）需要base64
     */
    public static final int REQUEST_TYPE_SET_SENSOR_TRIGGER = REQUEST_TYPE_SET_BASE + 2;
    /**
     * 设置传感器名
     */
    public static final int REQUEST_TYPE_SET_SENSOR_NAME = REQUEST_TYPE_SET_BASE + 3;

    public static final String JSON_SENSOR_ID_KEY = "sensor_id";
    public static final String JSON_SENSOR_NAME_KEY = "sensor_name";
    public static final String JSON_SENSOR_TYPE_KEY = "sensor_type";
    public static final String JSON_SENSOR_VALUE_KEY = "sensor_value";
    public static final String JSON_SENSOR_DATE_KEY = "sensor_date";

    private static final String JSON_DEVICE_ID_KEY = "device_id";
    private static final String JSON_DEVICE_NAME_KEY = "device_name";
    // private static final String JSON_DEVICE_CREDENTIALS_KEY = "cred"; Not used anywhere
    private static final String JSON_DEVICE_MAN_DATE = "man_date"; // Manufacturing date
    private static final String JSON_DEVICE_REG_DATE = "reg_date"; // Nearest registering date, maybe changed to 1st one

    private static final String URL_DEVICE_ID_KEY = "device_id";
    private static final String URL_DEVICE_TYPE_KEY = "device_type";
    private static final String URL_SENSOR_ID_KEY = "sensor_id";
    private static final String URL_REQUEST_TYPE_KEY = "request_type";
    private static final String URL_SENSOR_NAME_KEY = "name"; // Not sensor_name !
    private static final String URL_DEVICE_NAME_KEY = "name"; // Same as above
    private static final String URL_SENSOR_TRIGGER_KEY = "trigger";
    private static final String URL_COUNT_KEY = "count";

    /**
     * @deprecated PHP端已改为直接返回带数据的列表
     *
     * @param sensor_id 传感器ID
     * @return 此传感器数据的JSON对象
     * 更新指定传感器的值
     */
    public static JSONObject fetchSensorInfoJSON(int sensor_id, ControlCenter device) throws IOException, JSONException {
        Log.v(LOG_TAG, "fetchSensorInfoJSON() entered");
        Map<String, String> paramMap = new HashMap<>(4);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(device.getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_QUERY_SENSOR_INFO));
        paramMap.put(URL_SENSOR_ID_KEY, String.valueOf(sensor_id));

        return new JSONObject(getHttpReturnString(paramMap));
    }


    /**
     * 获取传感器列表
     *
     * @return JSONArray 传感器数组
     */
    public static JSONArray fetchSensorListJSON(ControlCenter device) throws IOException, JSONException {
        Map<String, String> paramMap = new HashMap<>(3);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(device.getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_QUERY_SENSORS_LIST));

        return new JSONArray(getHttpReturnString(paramMap));
    }

    /**
     * 上传传感器名字更新到服务器
     *
     * @param snr 已经改过名字的Sensor实例
     * @return true=成功 false=失败
     */
    public static boolean uploadSensorName(Sensor snr) throws IOException {
        Map<String, String> paramMap = new HashMap<>(5);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(snr.getParentControlCenter().getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_SET_SENSOR_NAME));
        paramMap.put(URL_SENSOR_NAME_KEY, Base64.encodeToString(snr.getSensorName().getBytes(), Base64.URL_SAFE));
        paramMap.put(URL_SENSOR_ID_KEY, String.valueOf(snr.getSensorId()));

        try {
            JSONObject json = new JSONObject(getHttpReturnString(paramMap));
            if (json.getString("result").equals("ok"))
                return true;
        } catch (JSONException e) { // No such key
            Log.e(LOG_TAG, "updateSensorName failed with JSON exp: " + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * 上传控制器名字更新到服务器
     *
     * @param cc 已经改过名字的ControlCenter实例
     * @return true=成功 false=失败
     */
    public static boolean uploadDeviceName(ControlCenter cc) throws IOException {
        Map<String, String> paramMap = new HashMap<>(4);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(cc.getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_SET_SENSOR_NAME));
        paramMap.put(URL_DEVICE_NAME_KEY, Base64.encodeToString(cc.getDeviceName().getBytes(), Base64.URL_SAFE));

        try {
            JSONObject json = new JSONObject(getHttpReturnString(paramMap));
            if (json.getString("result").equals("ok"))
                return true;
        } catch (JSONException e) { // No such key. No need to throw an exception. Just return false
            Log.e(LOG_TAG, "updateSensorName failed with JSON exp: " + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * 上传传感器的触发器到服务器
     *
     * @param snr 已经改过触发器的Sensor实例（可以用Trigger类的toString）
     * @return true=成功 false=失败
     */
    public static boolean uploadSensorTrigger(Sensor snr) throws IOException {
        Map<String, String> paramMap = new HashMap<>(5);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(snr.getParentControlCenter().getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_SET_SENSOR_TRIGGER));
        paramMap.put(URL_SENSOR_TRIGGER_KEY, snr.getTriggerString());
        paramMap.put(URL_SENSOR_ID_KEY, String.valueOf(snr.getSensorId()));

        try {
            JSONObject json = new JSONObject(getHttpReturnString(paramMap));
            if (json.getString("result").equals("ok"))
                return true;
        } catch (JSONException e) { // No such key. No need to throw an exception. Just return false
            Log.e(LOG_TAG, "uploadSensorTrigger failed with JSON exp: " + e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean loadDeviceInfo(ControlCenter cc) throws IOException {
        Map<String, String> paramMap = new HashMap<>(3);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(cc.getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_QUERY_DEVICE_INFO));

        try {
            JSONObject json = new JSONObject(getHttpReturnString(paramMap));
            cc.setDeviceName(json.getString(JSON_DEVICE_NAME_KEY));
            cc.setManDate(json.getInt(JSON_DEVICE_MAN_DATE));
            cc.setRegDate(json.getInt(JSON_DEVICE_REG_DATE));
            return true;
        } catch (JSONException e) { // No such key
            Log.e(LOG_TAG, "loadDeviceInfo failed with JSON exp: " + e.getLocalizedMessage());
        }
        return false;
    }

    public static ArrayList<Sensor.SensorHistory> loadSensorHistory(Sensor snr, int count) throws IOException {
        if (snr.getSensorId() == Sensor.INVALID_SENSOR_ID) return null;

        Map<String, String> paramMap = new HashMap<>(5);
        paramMap.put(URL_DEVICE_ID_KEY, String.valueOf(snr.getParentControlCenter().getDeviceId()));
        paramMap.put(URL_DEVICE_TYPE_KEY, String.valueOf(DEVICE_TYPE));
        paramMap.put(URL_REQUEST_TYPE_KEY, String.valueOf(REQUEST_TYPE_QUERY_SENSOR_HISTORY));
        paramMap.put(URL_SENSOR_ID_KEY, String.valueOf(snr.getSensorId()));
        paramMap.put(URL_COUNT_KEY, String.valueOf(count));

        try {
            String retStr = getHttpReturnString(paramMap);
            if (retStr.charAt(0) == '[') { // Array
                JSONArray jsonArray = new JSONArray(retStr);
                ArrayList<Sensor.SensorHistory> list = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); ++i) {
                    list.add(new Sensor.SensorHistory(
                            jsonArray.getJSONObject(i).getLong(JSON_SENSOR_DATE_KEY),
                            jsonArray.getJSONObject(i).getInt(JSON_SENSOR_VALUE_KEY)));
                }
                return list;
            } else if (retStr.charAt(0) == '{') { // Object (only one entry)
                ArrayList<Sensor.SensorHistory> list = new ArrayList<>(1);
                JSONObject jsonObject = new JSONObject(retStr);
                list.add(new Sensor.SensorHistory(
                        jsonObject.getLong(JSON_SENSOR_DATE_KEY),
                        jsonObject.getInt(JSON_SENSOR_VALUE_KEY)
                ));
                return list;
            }
        } catch (JSONException e) { // No such key
            Log.e(LOG_TAG, "loadSensorHistory failed with JSON exp: " + e.getLocalizedMessage());
        }
        return null;
    }

    private static String buildUrlWithParams(Map map) {
        StringBuilder sb = new StringBuilder(INCONTROL_API_URL);
        Set entryset = map.entrySet();
        Iterator iter = entryset.iterator();

        sb.append("?");
        while (iter.hasNext()) {
            Map.Entry<String, String> me = (Map.Entry<String, String>) iter.next();
            sb.append(me.getKey());
            sb.append("=");
            sb.append(me.getValue());
            sb.append("&");
        }

        sb.deleteCharAt(sb.length() - 1); // Remove trailing "&"
        return sb.toString();
    }

    private static String getHttpReturnString(Map paramMap) throws IOException, JSONException {
        HttpGet httpGet = new HttpGet(buildUrlWithParams(paramMap));
        HttpClient httpClient = new DefaultHttpClient();
        Log.v(LOG_TAG, "fetchSensorListJSON() ready to sent request");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Log.v(LOG_TAG, "fetchSensorListJSON() got response, returning");

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            Log.v(LOG_TAG, "getHttpReturnString() status code = 200");
            return EntityUtils.toString(httpResponse.getEntity());
        } else if (httpResponse.getStatusLine().getStatusCode() == 501) { // "Not implemented"
            Log.e(LOG_TAG, "getHttpReturnString() status code = 501, msg="
                    + EntityUtils.toString(httpResponse.getEntity()));
            throw new IOException("参数错误，请检查是否存在错误的设备ID！ Detail: "
                    + EntityUtils.toString(httpResponse.getEntity()));
        } else {
            Log.e(LOG_TAG, "getHttpReturnString() status code = (unknown) "
                    + String.valueOf(httpResponse.getStatusLine().getStatusCode()));
            throw new IOException("HTTP返回码未知: "
                    + String.valueOf(httpResponse.getStatusLine().getStatusCode()));
        }
    }
}
