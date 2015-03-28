package com.hamster.incontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * 配置中心，也可以读写数据库
 */


/**
 * Created by Hamster on 2015/1/11.
 * </p>
 * 用来操作数据库，比如读写本机ID，缓存传感器信息等
 * 用前会自动open，用后要手动close
 */
class LocalConfigStore {
    private static final int DB_VERSION = 1;
    private static final String LOG_TAG = "InControl_LCS";
    private static final String DEFAULT_DB_FILENAME = "incontrol_android.db";

    private static final String MYDEVICE_TABLE_NAME = "mydevice";
    private static final String SENSORS_TABLE_NAME = "sensors";
    private static final String DEVICE_ID_KEY = "device_id"; // in mydevice
    private static final String DEVICE_NAME_KEY = "device_name";
    private static final String DEVICE_CREDENTIALS_KEY = "user_credentials";
    private static final String SENSOR_ID_KEY = "sensor_id"; // sensors table
    private static final String SENSOR_NAME_KEY = "name";
    private static final String SENSOR_TYPE_KEY = "type";
    private static final String SENSOR_CACHED_VALUE_KEY = "value";
    private static final String SENSOR_UPDATE_DATE_KEY = "date";
    private static final String SENSOR_PARENT_CONTROL_ID_KEY = "control_id";
    private static final String SENSOR_TRIGGER_KEY = "trigger";

    private Context mContext;
    private DatabaseHelper dbhelper;
    private SQLiteDatabase db;

    LocalConfigStore(Context context) {
        this.mContext = context;
        dbhelper = new DatabaseHelper(this.mContext);
        this.open();
    }

    void open() {
        if (db == null) db = dbhelper.getWritableDatabase();
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
    public long updateDeviceId(boolean add_new, int device_id, int org_device_id) {
        if (!add_new && device_id == org_device_id) return 0;

        ContentValues cv = new ContentValues();

        cv.put(DEVICE_ID_KEY, device_id);

        if (add_new) {
            return db.insert(MYDEVICE_TABLE_NAME, null, cv); // TODO
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
        if (!add_new && sensor_id == org_sensor_id) return 0;

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
        Log.v(LOG_TAG, "updateDevice enter");
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

        // 更新name和cred，上面只更新了ID
        ContentValues cv = new ContentValues();
        cv.put(DEVICE_NAME_KEY, new_device.getDeviceName());
        cv.put(DEVICE_CREDENTIALS_KEY, new_device.getCredentials());

        result |= db.update(MYDEVICE_TABLE_NAME, cv,
                DEVICE_ID_KEY + " = ?",
                new String[]{String.valueOf(change_to_id)});
        Log.v(LOG_TAG, "updateDevice return");
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
        if (change_to_id == Sensor.INVALID_SENSOR_ID || add_new)
            change_to_id = sensor_id;
        result |= this.updateSensorId(add_new, change_to_id, sensor_id);

        // 更新剩下的项目
        ContentValues cv = new ContentValues();
        cv.put(SENSOR_NAME_KEY, snr.getSensorName());
        cv.put(SENSOR_TYPE_KEY, snr.getSensorTypeInt());
        cv.put(SENSOR_UPDATE_DATE_KEY, snr.getLastUpdateDate());
        cv.put(SENSOR_CACHED_VALUE_KEY, snr.getSensorCachedValue());
        cv.put(SENSOR_PARENT_CONTROL_ID_KEY, snr.getParentControlCenter().getDeviceId());
        cv.put(SENSOR_TRIGGER_KEY, snr.getTriggerInstance().toString());

        result |= db.update(SENSORS_TABLE_NAME, cv,
                SENSOR_ID_KEY + " = ?",
                new String[]{String.valueOf(change_to_id)});
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

    /**
     * 获取指定ControlCenter下属的所有缓存的Sensor
     *
     * @param cc 要查询的ControlCenter
     * @return 查到的Sensors
     */
    public Sensor[] getSensors(ControlCenter cc) {
        Cursor cursor = db.query(SENSORS_TABLE_NAME,
                new String[]{SENSOR_ID_KEY,
                        SENSOR_NAME_KEY,
                        SENSOR_TYPE_KEY,
                        SENSOR_UPDATE_DATE_KEY,
                        SENSOR_CACHED_VALUE_KEY,
                        SENSOR_TRIGGER_KEY},
                SENSOR_PARENT_CONTROL_ID_KEY + " = ?",
                new String[]{String.valueOf(cc.getDeviceId())},
                null, null, null);

        int sensor_count = cursor.getCount();
        if (sensor_count == 0) {
            cursor.close();
            return null;
        }

        Sensor[] sensors = new Sensor[sensor_count];
        cursor.moveToFirst();
        int i = 0;
        while (i < sensor_count) {
            sensors[i] = new Sensor(cc, mContext);
            sensors[i].setSensorId(cursor.getInt(0));
            sensors[i].setSensorName(cursor.getString(1));
            sensors[i].setSensorType(Sensor.convertIntToType(cursor.getInt(2)));
            sensors[i].setLastUpdateDate(cursor.getInt(3));
            sensors[i].setSensorCachedValue(cursor.getString(4));
            sensors[i].setTriggerString(cursor.getString(5));
            cursor.moveToNext();
            i++;
        }
        cursor.close();
        return sensors;
    }

    public Sensor getSensorById(int sensor_id) {
        return getSensorById(sensor_id, false);
    }

    /**
     * The long-waited function is finally here!
     *
     * @param sensor_id       to query
     * @param require_trigger Whether trigger info should be fetched. DO NOT ENABLE in Trigger related things
     *                        because this can cause a loop (setTriggerString -> (Init) -> this function)
     * @return the found sensor
     */
    public Sensor getSensorById(int sensor_id, boolean require_trigger) {
        if (sensor_id == Sensor.INVALID_SENSOR_ID) return null;

        Cursor cursor = db.query(SENSORS_TABLE_NAME,
                new String[]{SENSOR_ID_KEY,
                        SENSOR_NAME_KEY,
                        SENSOR_TYPE_KEY,
                        SENSOR_UPDATE_DATE_KEY,
                        SENSOR_CACHED_VALUE_KEY,
                        SENSOR_PARENT_CONTROL_ID_KEY,
                        SENSOR_TRIGGER_KEY},
                SENSOR_ID_KEY + " = ?",
                new String[]{String.valueOf(sensor_id)},
                null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        ControlCenter cc = new ControlCenter(mContext);

        Cursor cc_cursor = db.query(MYDEVICE_TABLE_NAME,
                new String[]{DEVICE_NAME_KEY,
                        DEVICE_ID_KEY,
                        DEVICE_CREDENTIALS_KEY},
                DEVICE_ID_KEY + " = ?",
                new String[]{String.valueOf(cursor.getInt(5))},
                null, null, null);

        if (cc_cursor.getCount() == 0) {
            cc_cursor.close();
            return null;
        }

        cc_cursor.moveToFirst(); // Need this after getCount ... Why?
        cc.setDeviceName(cc_cursor.getString(0));
        cc.setDeviceId(cc_cursor.getInt(1));
        cc.setCredentials(cc_cursor.getString(2));

        Sensor snr = new Sensor(cc, mContext);
        snr.setSensorId(cursor.getInt(0));
        snr.setSensorName(cursor.getString(1));
        snr.setSensorType(Sensor.convertIntToType(cursor.getInt(2)));
        snr.setLastUpdateDate(cursor.getInt(3));
        snr.setSensorCachedValue(cursor.getString(4));
        if (require_trigger)
            snr.setTriggerString(cursor.getString(6)); // 5 is parent control id

        cursor.close();
        return snr;
    }

    /**
     * 删除一个设备
     *
     * @param device_id 要删除的ID
     * @return true成功 false失败
     */
    public int removeDevice(int device_id) {
        int result = 0;
        result |= db.delete(MYDEVICE_TABLE_NAME, "device_id = ?", new String[]{String.valueOf(device_id)});
        result |= db.delete(SENSORS_TABLE_NAME, "control_id = ?", new String[]{String.valueOf(device_id)});
        return result;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private String CREATE_TABLE_MYDEVICE = "CREATE TABLE mydevice(" +
                "device_id integer," +
                "device_name text," +
                "user_credentials text)";
        private String CREATE_TABLE_SENSORS = "CREATE TABLE sensors(" +
                "sensor_id integer, " +
                "type integer," +
                "name text," +
                "value text," +
                "date integer," +
                "control_id integer," +
                "trigger text)";

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


