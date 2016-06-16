package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by trans on 2016-03-16.
 */
public class Config {

    public static final int REQUEST_CODE = 100;

//    public static final String HOST = "http://192.168.0.217:8000/api/";
//    //public static final String HOST = "http://155.230.192.46:8000/api/";
//
//    public static final String HISTORY_URL = HOST + "history";
//    public static final String USER_URL = HOST + "user";
//    public static final String EXCLUDED_PACKAGE_URL = HOST + "excluded_package/";


    private final String PREF_NAME = "pref";
    private final String BASE_URL = "BASE_URL";
    private final String KEY_UUID = "UUID";
    private final String KEY_USER_NAME = "NAME";
    private final String KEY_USER_PHONE = "PHONE";
    private final String EXP_CODE = "EXP_CODE";
    private final String EXCLUDED_PACKAGE = "EXCLUDED_PACKAGE";
    private final String KEY_IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    private final String KEY_ACCESSIBILITY_WARNING = "accessibility_warning";
    private final String DEFAULT_BASE_URL = "http://192.168.0.217:8000/api/";
    private final String QUERY_BASE_URL_STRING_URL = "http://transf01.github.io/app_monitor_rest/";
    private final String DEFAULT_EXCLUDE_PACKAGE = "[{\"package_name\":\"com.android.settings\"}," +
            "{\"package_name\":\"com.android.keyguard\"}," +
            "{\"package_name\":\"android\"}," +
            "{\"package_name\":\"com.cashslide\"}," +
            "{\"package_name\":\"com.nexon.nxplay\"}," +
            "{\"package_name\":\"com.android.systemui\"}]\n";

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

    public String getExpCode() {
        return mPref.getString(EXP_CODE, "0");
    }

    public void setExpCode(String expCode) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(EXP_CODE, expCode);
        editor.commit();
    }

    public void setExcludedPackage(JSONArray packags) {
        StringBuffer strPackags = new StringBuffer();

        if (packags.length() <=0)
            return;

        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(EXCLUDED_PACKAGE, packags.toString());
        editor.commit();
    }

    public void setBaseURL(JSONObject host) {
        try {
            String hostString = host.getString("host");
            if (hostString != null && !hostString.isEmpty()) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString(BASE_URL, hostString);
                editor.commit();
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private String getBaseURLString() {
        return mPref.getString(BASE_URL, DEFAULT_BASE_URL);
    }

    public URL getHistoryURL() throws  MalformedURLException{
        return new URL(getBaseURLString() + "history/");
    }

    public URL getUserHistoryURL(String startDate, String startTime) throws  MalformedURLException{
        return new URL(getBaseURLString() + getUUID() + "/date/" + startDate + "/time/" + startTime);
    }

    public URL getExcludedPackageURL() throws  MalformedURLException{
        return new URL(getBaseURLString() + "excluded_package/");
    }

    public URL getQueryBaseURLStringURL() throws MalformedURLException {
        return new URL(QUERY_BASE_URL_STRING_URL);
    }

    public boolean isExcludedPackage(CharSequence packageName) {
        if (packageName == null || packageName.toString().isEmpty()) {
            return true;
        }

        String excludePackages = mPref.getString(EXCLUDED_PACKAGE, DEFAULT_EXCLUDE_PACKAGE);
        try {
            JSONArray array = new JSONArray(excludePackages);
            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("package_name").equals(packageName))
                    return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
