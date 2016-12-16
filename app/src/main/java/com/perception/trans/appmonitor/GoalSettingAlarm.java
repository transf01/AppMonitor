package com.perception.trans.appmonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.Calendar;

/**
 * Created by trans on 2016-12-15.
 */

public class GoalSettingAlarm {

    private static GoalSettingAlarm mInstance = null;
    private final static String NOTI_HOUR = "NOTI_HOUR";
    private final static String NOTI_MIN = "NOTI_MIN";

    private GoalSettingAlarm() {

    }

    public static GoalSettingAlarm getInstance() {
        if (mInstance == null) {
            mInstance = new GoalSettingAlarm();
        }
        return mInstance;
    }

    public void setTime(Context context, int hourOfDay, int minute) {
        SharedPreferences pref = context.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(NOTI_HOUR, hourOfDay);
        editor.putInt(NOTI_MIN, minute);
        editor.commit();
        start(context, hourOfDay, minute);
    }

    public Pair<Integer, Integer> getTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        return Pair.create(pref.getInt(NOTI_HOUR, -1), pref.getInt(NOTI_MIN, -1));
    }


    public boolean start(Context context) {
        Pair<Integer, Integer> time = getTime(context);
        if (time.first < 0 || time.second < 0) {
            return false;
        }
        start(context, time.first, time.second);
        return true;
    }

    private void start(Context context, int hourOfDay, int minute) {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP
                , getTriggerTime(System.currentTimeMillis(), hourOfDay, minute)
                , AlarmManager.INTERVAL_DAY
                , PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private long getTriggerTime(long currentMillis, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        long targetMillis = calendar.getTimeInMillis();
        if (currentMillis > targetMillis) {
            targetMillis += AlarmManager.INTERVAL_DAY;
        }
        return targetMillis;
    }
}
