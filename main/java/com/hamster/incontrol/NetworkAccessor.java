package com.hamster.incontrol;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hamster on 2014/12/14.
 * <p/>
 * 用来从云端读取主机数据的类
 */
public class NetworkAccessor {
    /**
     * API地址
     */
    public static final String INCONTROL_API_URL = "http://incontrol.sinaapp.com/incontrol_api.php";

    /**
     * 查询所有传感器
     */
    public int REQUEST_TYPE_QUERY_SENSORS = 1;
    /**
     * 查询某一传感器的具体值
     */
    public int REQUEST_TYPE_QUERY_SENSOR_INFO = 2;

    private final int DEVICE_TYPE = 2; // 设备类型是客户端，不可修改
    private HomeDevice device;

    NetworkAccessor(HomeDevice mydevice) {
        super();
        this.device = mydevice;
    }

    /**
     * 更新指定传感器的值
     *
     * @param sensor_id 传感器ID
     * @return 此传感器数据的JSON对象
     */
    public JSONObject updateSensorInfoJSON(int sensor_id) throws IOException, JSONException {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("device_id", String.valueOf(device.getDeviceId()));
        paramMap.put("device_type", String.valueOf(DEVICE_TYPE));
        paramMap.put("request_type", String.valueOf(REQUEST_TYPE_QUERY_SENSOR_INFO));
        paramMap.put("sensor_id", String.valueOf(sensor_id));

        HttpGet httpGet = new HttpGet(buildUrlWithParams(paramMap));
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String retStr = EntityUtils.toString(httpResponse.getEntity());

            JSONObject json = new JSONObject(retStr);
            return json;
        } else if (httpResponse.getStatusLine().getStatusCode() == 501) { // "Not implemented"
            throw new IOException("URL Parameters Error! Detail: "
                    + EntityUtils.toString(httpResponse.getEntity()));
        } else {
            throw new IOException("HTTP Return Code unknown: "
                    + String.valueOf(httpResponse.getStatusLine().getStatusCode()));
        }
    }


    /**
     * 获取传感器列表
     *
     * @return JSONArray，传感器数组
     */
    public JSONArray updateSensorListJSON() throws IOException, JSONException {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("device_id", String.valueOf(device.getDeviceId()));
        paramMap.put("device_type", String.valueOf(DEVICE_TYPE));
        paramMap.put("request_type", String.valueOf(REQUEST_TYPE_QUERY_SENSORS));

        HttpGet httpGet = new HttpGet(buildUrlWithParams(paramMap));
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String retStr = EntityUtils.toString(httpResponse.getEntity());

            JSONArray jsonArray = new JSONArray(retStr);
            return jsonArray;
        } else if (httpResponse.getStatusLine().getStatusCode() == 501) { // "Not implemented"
            throw new IOException("URL Parameters Error! Detail: "
                    + EntityUtils.toString(httpResponse.getEntity()));
        } else {
            throw new IOException("HTTP Return Code unknown: "
                    + String.valueOf(httpResponse.getStatusLine().getStatusCode()));
        }
    }

    private String buildUrlWithParams(Map map) {
        StringBuilder sb = new StringBuilder(INCONTROL_API_URL);
        Set entryset = map.entrySet();
        Iterator iter = entryset.iterator();
        boolean isFirstParam = true;

        while (iter.hasNext()) {
            if (isFirstParam) {
                sb.append("?");
                isFirstParam = false;
            }
            Map.Entry<String, String> me = (Map.Entry<String, String>) iter.next();
            sb.append(me.getKey());
            sb.append("=");
            sb.append(me.getValue());
            sb.append("&");
        }

        sb.deleteCharAt(sb.length() - 1); // Remove trailing "&"
        return sb.toString();
    }
}
