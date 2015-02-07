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
 * 用前open，用后close
 */
public class LocalConfigStore {
    public static int DB_VERSION = 1;
    public static String DEFAULT_DB_FILENAME = "incontrol_android.db";

    public static String MYDEVICE_TABLE_NAME = "mydevice";
    public static String SENSORS_TABLE_NAME = "sensors";
    public static String DEVICE_ID_KEY = "device_id"; // in mydevice
    public static String DEVICE_NAME_KEY = "device_name";
    public static String DEVICE_CREDENTIALS_KEY = "user_credentials";
    public static String SENSOR_ID_KEY = "sensor_id"; // sensors table
    public static String SENSOR_NAME_KEY = "sensor_name";
    public static String SENSOR_TYPE_KEY = "sensor_type";
    public static String SENSOR_CACHED_VALUE_KEY = "sensor_cached_value";
    public static String SENSOR_UPDATE_DATE_KEY = "sensor_update_date";
    public static String SENSOR_PARENT_CONTROL_ID_KEY = "sensor_parent_control_id";

    private Context mContext;
    private DatabaseHelper dbhelper;
    private SQLiteDatabase db;

    LocalConfigStore(Context context) {
        this.mContext = context;
        dbhelper = new DatabaseHelper(this.mContext);
    }

    LocalConfigStore open() {
        db = dbhelper.getWritableDatabase();
        return this; // 这个是为了能实现.open().write().close()这种的吧？
    }

    public void close() {
        db.close();
        db = null;
        dbhelper.close();
    }

    /**
     * 设置指定表中指定列的值
     *
     * @param table_name 目标表名
     * @param req_id     要修改的ID
     * @param id_key     ID所在列名
     * @param column     目标列
     * @param upd_data   要改为的数值
     * @return 0失败其他成功
     */
    public int updateColumn(String table_name, int req_id, String id_key, String column, long upd_data) {
        if (req_id <= Sensor.INVALID_SENSOR_ID) // They are the same...
            return 0;

        ContentValues cv = new ContentValues();
        cv.put(column, upd_data);

        return db.update(table_name, cv, id_key + "=" + String.valueOf(req_id), null);
    }

    /**
     * 通用：设置指定表中指定列的值
     *
     * @param table_name 目标表名
     * @param req_id     要修改的ID
     * @param id_key     ID所在列名
     * @param column     目标列
     * @param upd_data   要改为的字符串
     * @return 0失败其他成功
     */
    public int updateColumn(String table_name, int req_id, String id_key, String column, String upd_data) {
        if (req_id <= Sensor.INVALID_SENSOR_ID) // They are the same...
            return 0;

        ContentValues cv = new ContentValues();
        cv.put(column, upd_data);

        return db.update(table_name, cv, id_key + "=" + String.valueOf(req_id), null);
    }

    /**
     * 添加或修改主机ID
     *
     * @param add_new       是否添加新ID，否的话要设置下面的org_id来做改变，device_id可以不要，是的话需要device_id，org可以不要
     * @param device_id     要添加或改变为的ID
     * @param org_device_id 原始ID
     * @return 如果add_new是1，则返回新行ID，否则0失败其他成功
     */
    public long updateDeviceId(boolean add_new, int device_id, int org_device_id) {
        if (!add_new && device_id == org_device_id) return 0;

        ContentValues cv = new ContentValues();

        cv.put(DEVICE_ID_KEY, device_id);

        if (add_new) {
            return db.insert(MYDEVICE_TABLE_NAME, null, cv);
        } else {
            return db.update(MYDEVICE_TABLE_NAME, cv, DEVICE_ID_KEY + "=" + String.valueOf(org_device_id), null);
        }
    }

    /**
     * 添加或修改传感器ID
     *
     * @param add_new       是否添加新ID，否的话要设置下面的org_id来做改变，sensor_id可以不要，是的话需要sensor_id，org可以不要
     * @param sensor_id     要添加或改变为的ID
     * @param org_sensor_id 原始ID
     * @return 如果add_new是1，则返回新行ID，否则0失败其他成功
     */
    public long updateSensorId(boolean add_new, int sensor_id, int org_sensor_id) {
        if (sensor_id == org_sensor_id) return 0;

        ContentValues cv = new ContentValues();

        cv.put(SENSOR_ID_KEY, sensor_id);

        if (add_new) {
            return db.insert(SENSORS_TABLE_NAME, null, cv);
        } else {
            return db.update(SENSORS_TABLE_NAME, cv, SENSOR_ID_KEY + "=" + String.valueOf(org_sensor_id), null);
        }
    }

    /**
     * 根据新的HomeDevice类来*更新*或*增加*现有的数据，就是以上三个函数的综合
     * 而且名称仅存储在本地
     *
     * @param new_device   你猜
     * @param change_to_id 要改为的ID，不用的话设为ControlCenter.INVALID
     * @return 0失败其他成功
     */
    public boolean updateDevice(ControlCenter new_device, int change_to_id) {
        int device_id = new_device.getDeviceId();
        int result = 0;

        Cursor cursor = db.query(MYDEVICE_TABLE_NAME,
                new String[]{DEVICE_ID_KEY},
                DEVICE_ID_KEY + "=" + String.valueOf(device_id),
                null, null, null, null);
        // 查不到原来的ID就说明要新增
        boolean add_new = cursor.getCount() == 0;

        // 如果1.指定了新ID 且 2.原ID能查到 就说明要修改，change就保持传入的样子不变
        // 下面这个条件是上述的反面，因为正面叙述说不清楚……以上的反面就是不用修改，把change短路掉即可
        // 用add_new代替cursor.getCount可以节约计算资源，反正就是那个结果
        if (change_to_id == ControlCenter.INVALID_DEVICE_ID || add_new)
            change_to_id = device_id;
        result |= updateDeviceId(add_new, change_to_id, device_id);
        result |= updateColumn(MYDEVICE_TABLE_NAME, device_id, DEVICE_ID_KEY,
                DEVICE_NAME_KEY, new_device.getDeviceName());
        result |= updateColumn(MYDEVICE_TABLE_NAME, device_id, DEVICE_ID_KEY,
                DEVICE_CREDENTIALS_KEY, new_device.getCredentials());
        return result != 0;
    }

    /**
     * 根据新的HomeDevice类来*更新*或*增加*现有的数据
     * 而且名称仅存储在本地
     *
     * @param snr          你猜
     * @param change_to_id 要改为的ID，不用的话设为Sensor.INVALID
     * @return 0失败其他成功
     */
    public boolean updateSensor(Sensor snr, int change_to_id) {
        int sensor_id = snr.getSensorId();
        int result = 0;

        Cursor cursor = db.query(SENSORS_TABLE_NAME,
                new String[]{SENSOR_ID_KEY},
                SENSOR_ID_KEY + "=" + String.valueOf(sensor_id),
                null, null, null, null);
        // 查不到原来的ID就说明要新增
        boolean add_new = cursor.getCount() == 0;

        // 如果1.指定了新ID 且 2.原ID能查到 就说明要修改，change就保持传入的样子不变
        // 下面这个条件是上述的反面，因为正面叙述说不清楚……以上的反面就是不用修改，把change短路掉即可
        // 用add_new代替cursor.getCount可以节约计算资源，反正就是那个结果
        // TODO: Insert them with one ContentValues!
        if (change_to_id == Sensor.INVALID_SENSOR_ID || add_new)
            change_to_id = sensor_id;
        result |= this.updateSensorId(add_new, change_to_id, sensor_id);
        result |= this.updateColumn(SENSORS_TABLE_NAME, sensor_id, SENSOR_ID_KEY,
                SENSOR_NAME_KEY, snr.getSensorName());
        result |= this.updateColumn(SENSORS_TABLE_NAME, sensor_id, SENSOR_ID_KEY,
                SENSOR_UPDATE_DATE_KEY, snr.getLastUpdateDate());
        result |= this.updateColumn(SENSORS_TABLE_NAME, sensor_id, SENSOR_ID_KEY,
                SENSOR_CACHED_VALUE_KEY, snr.getSensorCachedValue());
        result |= this.updateColumn(SENSORS_TABLE_NAME, sensor_id, SENSOR_ID_KEY,
                SENSOR_PARENT_CONTROL_ID_KEY, snr.getParentControlCenter().getDeviceId());
        return result != 0;
    }

    /**
     * 获取数据库中的所有ControlCenter，注意数据库应该先初始化（已经由dbhelper保证）
     *
     * @return 获取到的ControlCenter们
     */
    public ControlCenter[] getControlCenters() {
        Cursor cursor = db.query(MYDEVICE_TABLE_NAME,
                new String[]{DEVICE_ID_KEY, DEVICE_NAME_KEY, DEVICE_CREDENTIALS_KEY},
                null, null, null, null, null);
        int device_count = cursor.getCount();
        if (device_count == 0) {
            cursor.close();
            return null;
        }

        ControlCenter[] ccs = new ControlCenter[device_count];
        int i = 0;
        cursor.moveToFirst();
        while (i < device_count) {
            ccs[i] = new ControlCenter(mContext);
            ccs[i].setDeviceId(cursor.getInt(0));
            ccs[i].setDeviceName(cursor.getString(1));
            ccs[i].setCredentials(cursor.getString(2));
            i++;
            cursor.moveToNext();
        }
        cursor.close();
        return ccs;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private String CREATE_TABLE_MYDEVICE = "CREATE TABLE mydevice(" +
                "device_id integer," +
                "device_name text," +
                "user_credentials text)";
        private String CREATE_TABLE_SENSORS = "CREATE TABLE sensors(" +
                "sensor_id integer, " +
                "sensor_type integer," +
                "sensor_name text," +
                "sensor_cached_value integer," +
                "sensor_update_date integer," +
                "sensor_parent_control_id integer)";

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


