package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    private final String URL_INFO = "URL_INFO";
    private final String KEY_UUID = "UUID";
    private final String KEY_USER_NAME = "NAME";
    private final String KEY_USER_PHONE = "PHONE";
    private final String EXP_CODE = "EXP_CODE";
    private final String EXCLUDED_PACKAGE = "EXCLUDED_PACKAGE";
    private final String KEY_IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    private final String KEY_ACCESSIBILITY_WARNING = "accessibility_warning";
    private final String QUERY_BASE_URL_STRING_URL = "http://transf01.github.io/app_monitor_rest/";

    private final String DEFAULT_URL_INFO = "{ \"host\" : \"http://192.168.0.217:8000/\", " +
            "\"api_base\" : \"api/\", " +
            "\"history\" : \"history/\", " +
            "\"user_history\" : \"%s/date/%s/time/%s/\", " +
            "\"user\" : \"user/\", " +
            "\"excluded_package\": \"excluded_package/\", " +
            "\"pre_survey\" : \"survey/1/\", " +
            "\"post_survey\" : \"survey/2/\" }";

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

    private boolean isValid(String string) {
        return string != null && !string.isEmpty();
    }

    public void setExcludedPackage(String packags) {
        if (!isValid(packags))
            return;

        StringBuffer strPackags = new StringBuffer();

        try {
            JSONArray array = new JSONArray(packags);
            if (packags.length() <=0)
                return;

            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(EXCLUDED_PACKAGE, packags.toString());
            editor.commit();

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setURLInfo(String jsonString) {
        if (!isValid(jsonString))
            return;

        try {
            JSONObject json = new JSONObject(jsonString);
            String host = json.getString("host");
            if (isValid(host)) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString(URL_INFO, jsonString);
                editor.commit();
                Log.d("trans", jsonString);
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getURLInfo() throws MalformedURLException{
        try {
            return new JSONObject(mPref.getString(URL_INFO, DEFAULT_URL_INFO));
        }catch (JSONException e) {
            throw new MalformedURLException();
        }
    }

    private String getURLInfoValue(JSONObject object, String key) throws MalformedURLException {
        try {
            return object.getString(key);
        }catch (JSONException e) {
            throw new MalformedURLException();
        }
    }

    private String getBaseURLString(JSONObject object) throws MalformedURLException {
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "api_base");
    }

    public URL getHistoryURL() throws  MalformedURLException{
        JSONObject object = getURLInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "history"));
    }

    public URL getUserHistoryURL(String startDate, String startTime) throws  MalformedURLException{
        JSONObject object = getURLInfo();
        return new URL(getBaseURLString(object) + String.format(getURLInfoValue(object, "user_history"), getUUID(), startDate, startTime));
    }

    public URL getExcludedPackageURL() throws  MalformedURLException{
        JSONObject object = getURLInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "excluded_package"));
    }

    public URL getPresurveyURL() throws  MalformedURLException{
        JSONObject object = getURLInfo();
        return new URL(getURLInfoValue(object, "host") + getURLInfoValue(object, "pre_survey"));
    }

    public URL getPostsruveyURL() throws  MalformedURLException{
        JSONObject object = getURLInfo();
        return new URL(getURLInfoValue(object, "host") + getURLInfoValue(object, "post_survey"));
    }

    public URL getUserURL() throws MalformedURLException {
        JSONObject object = getURLInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "user"));
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
