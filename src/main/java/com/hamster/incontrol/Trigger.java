package com.hamster.incontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Hamster on 2015/2/20.
 * 记录触发器的，也可以执行显示通知的操作。有网络操作，请单独开一个线程！
 */
class Trigger {
    private Context mContext;
    private static final String TAG = "InControl_TRG";
    private ArrayList<Condition> mConditions;
    private ArrayList<Action> mActions;
    private Sensor mAssociatedSensor;

    public static enum ActionType {
        ACTION_SHOW_NOTIFICATION,
        ACTION_TRIGGER_SENSOR,
        ACTION_SEND_SMS,
        ACTION_UNKNOWN
    }

    public static enum ConditionType {
        COND_EQUAL,
        COND_GREATER_THAN,
        COND_SMALLER_THAN,
        COND_CHANGED_OUT_OF_RANGE,
        COND_UNKNOWN
    }

    public static ActionType convertIntToActionType(int conv) {
        try {
            return ActionType.values()[conv];
        } catch (Exception e) {
            return ActionType.ACTION_UNKNOWN;
        }
    }

    public static ConditionType convertIntToConditionType(int conv) {
        try {
            return ConditionType.values()[conv];
        } catch (Exception e) {
            return ConditionType.COND_UNKNOWN;
        }
    }

    public class Action implements Describable {
        private String mActionTarget; // Receiver phone number / target sensor(switch) ID etc
        private String mActionContent; // "Your house is on fire" etc
        private ActionType mActionType;

        Action(String mActionTarget, String mActionContent, ActionType mActionType) {
            this.mActionTarget = mActionTarget;
            this.mActionContent = mActionContent;
            this.mActionType = mActionType;
        }

        Action(String savedString) {
            String[] strs = savedString.split(",");

            this.mActionType = convertIntToActionType(Integer.parseInt(strs[0]));
            this.mActionTarget = strs[1];
            this.mActionContent = strs[2];
        }

        public String getActionTarget() {
            return mActionTarget;
        }

        public String getActionContent() {
            return mActionContent;
        }

        public ActionType getActionType() {
            return mActionType;
        }

        /**
         * In fact this only shows a notification.
         *
         * @param notificationBody ALternative content text. Original one will be ignored if this is set.
         */
        public void doAction(String notificationBody) {
            switch (mActionType) {
                case ACTION_SHOW_NOTIFICATION:
                    String notifBody = mActionContent;
                    if (notificationBody != null) {
                        notifBody = notificationBody;
                    }
                    Notification.Builder builder = new Notification.Builder(mContext);
                    builder.setContentTitle(mContext.getResources().getString(R.string.app_name));
                    builder.setContentText(notifBody);
                    Notification notif = builder.getNotification(); // For compat on Android 4.0

                    NotificationManager nm = (NotificationManager)
                            mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(0, notif);
            }
        }

        @Override
        public String toString() {
            return String.valueOf(mActionType.ordinal()) + "," + mActionTarget + "," + mActionContent;
        }

        @Override
        public String getDescription() {
            switch (mActionType) {
                case ACTION_SHOW_NOTIFICATION:
                    return "Show a notification";
                case ACTION_SEND_SMS:
                    return "Send a message to " + mActionTarget + " saying " + mActionContent;
                case ACTION_TRIGGER_SENSOR:
                    return "Toggle switch (ID) " + mActionTarget;
                default:
                    return "";
            }
        }
    }

    /**
     * Note that this should be implemented in Center device as well.
     * Phone only shows a notification.
     */
    public class Condition implements Describable {
        private ConditionType mCondition;
        private String mComparingValue; // percents(with %, use the sensor below) or literal (w/o %)
        private Sensor mOriginatingSensor;
        private Sensor mSensorToCompare;

        Condition(String initString) {
            LocalConfigStore lcs = new LocalConfigStore(mContext);
            String[] strs = initString.split(",");

            mCondition = convertIntToConditionType(Integer.parseInt(strs[0]));
            mComparingValue = strs[1];
            mOriginatingSensor = lcs.getSensorById(Integer.parseInt(strs[2]));
            mSensorToCompare = lcs.getSensorById(Integer.parseInt(strs[3]));
            lcs.close();
        }

        private boolean isRelative() {
            return mComparingValue.contains("%");
        }

        public boolean doCheck() {
            if (isRelative() && mSensorToCompare == null) // Uses relative number but no sensor to compare
                return false;
            if (mSensorToCompare != null) {
                try {
                    mSensorToCompare.getParentControlCenter().updateSensors();
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred when checking condition/refreshing target sensors: "
                            + e.getLocalizedMessage());
                    return false;
                }
            }

            try {
                mOriginatingSensor.getParentControlCenter().updateSensors();
            } catch (Exception e) {
                Log.e(TAG, "Error occurred when checking condition/refreshing origin sensors: "
                        + e.getLocalizedMessage());
                return false;
            }

            switch (mCondition) {
                case COND_EQUAL:
                    if (isRelative()) {
                        double percent = Double.parseDouble(mComparingValue.substring(0, mComparingValue.indexOf("%")));
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                - Integer.parseInt(mSensorToCompare.getSensorCachedValue()) * percent
                                == 0;
                    } else {
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                == Integer.parseInt(mComparingValue);
                    }
                case COND_GREATER_THAN:
                    if (isRelative()) {
                        double percent = Double.parseDouble(mComparingValue.substring(0, mComparingValue.indexOf("%")));
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                - Integer.parseInt(mSensorToCompare.getSensorCachedValue()) * percent
                                > 0;
                    } else {
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                > Integer.parseInt(mComparingValue);
                    }
                case COND_SMALLER_THAN:
                    if (isRelative()) {
                        double percent = Double.parseDouble(mComparingValue.substring(0, mComparingValue.indexOf("%")));
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                - Integer.parseInt(mSensorToCompare.getSensorCachedValue()) * percent
                                < 0;
                    } else {
                        return Integer.parseInt(mOriginatingSensor.getSensorCachedValue())
                                < Integer.parseInt(mComparingValue);
                    }
                case COND_CHANGED_OUT_OF_RANGE:
                    // We want to hold until finish
                    ArrayList<Sensor.SensorHistory> list;
                    try {
                        list = NetworkAccessor.loadSensorHistory(mOriginatingSensor, 50);
                    } catch (IOException e) {
                        Log.e(TAG, "Error when loading sensor history:" + e.getLocalizedMessage());
                        return false;
                    }
                    Collections.sort(list);
                    // Take last one as sample
                    int lastValue = list.get(list.size() - 1).getValue();
                    int delta = Integer.parseInt(mOriginatingSensor.getSensorCachedValue()) - lastValue;
                    double percent_allowed = Double.parseDouble(mComparingValue.substring(0, mComparingValue.indexOf("%")));
                    if (Math.abs((double) delta / lastValue) < percent_allowed)
                        return false;
                    else
                        return true;
            }
            return false;
        }

        @Override
        public String toString() {
            if (mOriginatingSensor == null || mSensorToCompare == null)
                return ""; // Incomplete or wrong info. null is shown as "null"
            return String.valueOf(mCondition.ordinal()) + ","
                    + mComparingValue + ","
                    + String.valueOf(mOriginatingSensor.getSensorId()) + ","
                    + String.valueOf(mSensorToCompare.getSensorId());
        }

        @Override
        public String getDescription() {
            switch (mCondition) {
                case COND_EQUAL:
                    return "When value of " + mOriginatingSensor.getSensorName() + " is equal to "
                            + mSensorToCompare.getSensorName();
                case COND_SMALLER_THAN:
                    return "When value of " + mOriginatingSensor.getSensorName() + " is smaller than "
                            + mSensorToCompare.getSensorName();
                case COND_GREATER_THAN:
                    return "When value of " + mOriginatingSensor.getSensorName() + " is greater than "
                            + mSensorToCompare.getSensorName();
                case COND_CHANGED_OUT_OF_RANGE:
                    return "When value of " + mOriginatingSensor.getSensorName() + " has changed out of " +
                            mComparingValue;
                default:
                    return "";
            }
        }
    }

    Trigger(Context ctx, String initString, Sensor snr) {
        this.mAssociatedSensor = snr;
        mActions = new ArrayList<>();
        mConditions = new ArrayList<>(); // Do these first to avoid NPE

        this.mContext = ctx;
        if (initString == null) return;
        String[] strsCat = initString.split("&"); // Take care of this fxxking regex

        if (strsCat.length == 1) { // Split error, original string is returned
            return;
        }

        String[] conds = strsCat[0].split(";");
        String[] acts = strsCat[1].split(";");

        mActions.ensureCapacity(acts.length);
        mConditions.ensureCapacity(conds.length);

        for (String cond : conds) {
            mConditions.add(new Condition(cond));
        }

        for (String act : acts) {
            mActions.add(new Action(act));
        }
    }

    Trigger(Context ctx, Sensor snr) {
        this(ctx, snr.getTriggerString(), snr);
    }

    @Override
    public String toString() {
        String ret = "";
        if (mConditions.size() != 0 && mActions.size() != 0) { // Otherwise it's not complete
            for (Condition cond : mConditions) {
                if (cond != null) {// I'm fed up with this. Invalid inputs are ignored, but left a null in array
                    ret = ret + cond.toString() + ";";
                }
            }
            ret = ret.substring(0, ret.length() - 1); // Remove trailing ;

            ret = ret + "&";

            for (Action act : mActions) {
                if (act != null) {
                    ret = ret + act.toString() + ";";
                }
            }
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    public void checkAndRun() {
        for (Condition cond : mConditions) {
            if (!cond.doCheck()) return;
        }
        for (Action act : mActions) {
            act.doAction("Sensor " + mAssociatedSensor.getSensorName()
                    + " has satisified given conditions. Value is "
                    + String.valueOf(mAssociatedSensor.getSensorCachedValue()));
        }
    }

    public void addAction(Action act) {
        mActions.add(act);
    }

    public void addCondition(Condition cond) {
        mConditions.add(cond);
    }

    public ArrayList<Condition> getAllConditions() {
        return mConditions;
    }

    public ArrayList<Action> getAllActions() {
        return mActions;
    }
}
