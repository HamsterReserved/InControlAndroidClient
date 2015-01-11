package com.hamster.incontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 配置中心，也可以读写数据库
 */


/**
 * Created by Hamster on 2015/1/11.
 * </p>
 * 用来操作数据库，比如读写本机ID，缓存传感器信息等
 */
public class LocalConfigStore {
    public int DB_VERSION = 1;
    public String DEFAULT_DB_FILENAME = "incontrol_android.db";

    public String MYDEVICE_TABLE_NAME = "mydevice";
    public String SENSORS_TABLE_NAME = "sensors";
    public String DEVICE_ID_KEY = "device_id"; // in mydevice
    public String DEVICE_NAME_KEY = "device_name"; // in mydevice
    public String DEVICE_CREDENTIALS_KEY = "user_credentials"; // mydevice
    public String SENSOR_ID_KEY = "sensor_id"; // sensors table
    public String SENSOR_NAME_KEY = "sensor_name"; //sensors table
    public String SENSOR_TYPE_KEY = "sensor_type"; //sensors table
    public String SENSOR_CACHED_VALUE_KEY = "sensor_cached_value"; //sensors table
    public String SENSOR_UPDATE_DATE_KEY = "sensor_update_date"; //sensors table

    private Context mContext;
    private DatabaseHelper dbhelper;
    private SQLiteDatabase db;

    LocalConfigStore(Context context) {
        this.mContext = context;
        dbhelper = new DatabaseHelper(this.mContext);
    }

    public LocalConfigStore open() {
        db = dbhelper.getWritableDatabase();
        return this; // 这个是为了能实现.open().write().close()这种的吧？
    }

    public void close() {
        db.close();
        db = null;
        dbhelper.close();
    }

    /**
     * 添加或修改主机ID
     *
     * @param add_new       是否添加新ID，否的话要设置下面的org_id来做改变，device_id可以不要，是的话需要device_id，org可以不要
     * @param device_id     要添加或改变为的ID
     * @param org_device_id 原始ID
     * @return 如果add_new是1，则返回新行ID，否则0失败其他成功
     */
    public long setDeviceId(boolean add_new, int device_id, int org_device_id) {
        ContentValues cv = new ContentValues();

        cv.put(DEVICE_ID_KEY, device_id);

        if (add_new) {
            return db.insert(MYDEVICE_TABLE_NAME, null, cv);
        } else {
            return db.update(MYDEVICE_TABLE_NAME, cv, DEVICE_ID_KEY + "=" + String.valueOf(org_device_id), null);
        }
    }

    /**
     * 设置主机名称（仅本地）
     *
     * @param device_id   要修改名称的ID
     * @param device_name 目标名称
     * @return 0失败其他成功
     */
    public int setDeviceName(int device_id, String device_name) {
        if (device_id <= HomeDevice.INVALID_DEVICE_ID)
            return 0;

        ContentValues cv = new ContentValues();
        cv.put(DEVICE_NAME_KEY, device_name);

        return db.update(MYDEVICE_TABLE_NAME, cv, DEVICE_ID_KEY + "=" + String.valueOf(device_id), null);
    }

    /**
     * 设置密码
     *
     * @param device_id   目标ID
     * @param credentials 密码
     * @return 0失败其他成功
     */
    public int setCredentials(int device_id, String credentials) {
        ContentValues cv = new ContentValues();
        cv.put(DEVICE_CREDENTIALS_KEY, credentials);

        return db.update(MYDEVICE_TABLE_NAME, cv, DEVICE_ID_KEY + "=" + String.valueOf(device_id), null);
    }

    /**
     * 根据新的HomeDevice类来更新或增加现有的数据，就是以上三个函数的综合
     * 注意！如果你需要修改ID，请务必使用单独的setDeviceId！
     * 而且名称仅存储在本地
     *
     * @param mydevice     你猜
     * @param change_to_id 要改为的ID，不用的话设为HomeCenter.INVALID
     * @return 0失败其他成功
     */
    public boolean updateDevice(HomeDevice mydevice, int change_to_id) {
        int device_id = mydevice.getDeviceId();
        int result = 0;

        Cursor cursor = db.query(MYDEVICE_TABLE_NAME, new String[]{DEVICE_ID_KEY}, null, null, null, null, null);
        // 查不到原来的ID就说明要新增
        boolean add_new = cursor.getCount() == 0;

        // 如果1.指定了新ID 且 2.原ID能查到 就说明要修改，change就保持传入的样子不变
        // 下面这个条件是上述的反面，因为正面叙述说不清楚……以上的反面就是不用修改，把change短路掉即可
        // 用add_new代替cursor.getCount可以节约计算资源，反正就是那个结果
        if (change_to_id == HomeDevice.INVALID_DEVICE_ID || add_new)
            change_to_id = device_id;
        result |= this.setDeviceId(add_new, change_to_id, device_id);
        result |= this.setDeviceName(device_id, mydevice.getDeviceName());
        result |= this.setCredentials(device_id, mydevice.getCredentials());
        return result == 0 ? false : true;
    }

    public boolean updateSensor(Sensor snr) {
        // TODO
        return false;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private String CREATE_TABLE_MYDEVICE = "CREATE TABLE mydevice(" +
                "device_id integer primary key," +
                "device_name text not null," +
                "user_credentials text)";
        private String CREATE_TABLE_SENSORS = "CREATE TABLE sensors(" +
                "sensor_id integer primary key, " +
                "sensor_type integer not null," +
                "sensor_name text not null," +
                "sensor_cached_value integer," +
                "sensor_update_date integer)";

        DatabaseHelper(Context context) {
            super(context, DEFAULT_DB_FILENAME, null, DB_VERSION);
        }

        /**
         * 只在首次创建文件完毕后调用（自动）
         *
         * @param db 系统自己调用，跟你有毛线关系
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_MYDEVICE);
            db.execSQL(CREATE_TABLE_SENSORS);
        }

        /**
         * 升级用，现在用不到（系统自动调用）
         *
         * @param db         自动
         * @param oldVersion 自动
         * @param newVersion 自动
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 目前只有1个版本……这里就不写了
            //if (oldVersion >= DB_VERSION)
        }
    }
}


