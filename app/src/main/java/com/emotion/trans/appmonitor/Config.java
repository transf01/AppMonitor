package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by trans on 2016-03-16.
 */
public class Config {

    public static final int REQUEST_CODE = 100;

    public static final String HOST = "http://192.168.0.13:8000/api/";
    //public static final String HOST = "http://155.230.192.46:8000/api/";

    public static final String HISTORY_URL = HOST + "history";
    public static final String USER_URL = HOST + "user";

    private final String PREF_NAME = "pref";

    private final String KEY_UUID = "UUID";
    private final String KEY_USER_NAME = "NAME";
    private final String KEY_USER_PHONE = "PHONE";
    private final String KEY_IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    private final String KEY_ACCESSIBILITY_WARNING = "accessibility_warning";

    private SharedPreferences mPref;

    public Config(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isPassAccessbilityWarning() {
        return mPref.getBoolean(KEY_ACCESSIBILITY_WARNING, false);
    }

    public void passAccessbilityWarning() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_ACCESSIBILITY_WARNING, true);
        editor.commit();
    }

    public boolean isSendUserInfo() {
        return mPref.getBoolean(KEY_IS_SEND_USER_INFO, false);
    }

    public void saveSuccessSendUserInfo() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_IS_SEND_USER_INFO, true);
        editor.commit();
    }

    public String getUUID() {
        return mPref.getString(KEY_UUID, "");
    }

    public void saveUUID(String uuid) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_UUID, uuid);
        editor.commit();
    }

    public String getUserName() {
        return mPref.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.commit();
    }

    public String getPhoneNumber() {
        return mPref.getString(KEY_USER_PHONE, "");
    }

    public void setPhoneNumber(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_USER_PHONE, name);
        editor.commit();
    }

}
