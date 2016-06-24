package com.emotion.trans.appmonitor;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by trans on 2016-03-16.
 */
public class Config {

    public static final int USERINFO_CODE = 0x100;
    public static final int PRESURVEY_CODE = 0x200;

//    public static final String HOST = "http://192.168.0.217:8000/api/";
//    //public static final String HOST = "http://155.230.192.46:8000/api/";
//
//    public static final String HISTORY_URL = HOST + "history";
//    public static final String USER_URL = HOST + "user";
//    public static final String EXCLUDED_PACKAGE_URL = HOST + "excluded_package/";


    private final String PREF_NAME = "pref";
    private final String URL_INFO = "URL_INFO";
    private final String UUID = "UUID";
    private final String USER_NAME = "NAME";
    private final String USER_PHONE = "PHONE";
    private final String EXP_CODE = "EXP_CODE";
    private final String EXP_INFO = "EXP_INFO";
    private final String EXP_START_DATE = "EXP_START_DATE";
    private final String EXCLUDED_PACKAGE = "EXCLUDED_PACKAGE";
    private final String IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    private final String IS_COMPLETE_PRESURVEY = "IS_COMPLETE_PRESURVEY";
    private final String IS_COMPLETE_POSTSURVEY = "IS_COMPLETE_POSTSURVEY";
    private final String HOST_INFO_URL = "http://transf01.github.io/app_monitor_rest/";

    private final String DEFAULT_URL_INFO = "{ \"host\" : \"http://192.168.0.217:8000/\", " +
            "\"api_base\" : \"api/\", " +
            "\"history\" : \"history/\", " +
            "\"user_history\" : \"%s/date/%s/time/%s/\", " +
            "\"user\" : \"user/\", " +
            "\"excluded_package\": \"excluded_package/\", " +
            "\"pre_survey\" : \"survey/1/\", " +
            "\"post_survey\" : \"survey/2/\", " +
            "\"exp_info\" : \"exp_info/2016_summer\" }";

    private final String DEFAULT_EXCLUDE_PACKAGE = "[{\"package_name\":\"com.android.settings\"}," +
            "{\"package_name\":\"com.android.keyguard\"}," +
            "{\"package_name\":\"android\"}," +
            "{\"package_name\":\"com.cashslide\"}," +
            "{\"package_name\":\"com.nexon.nxplay\"}," +
            "{\"package_name\":\"com.android.systemui\"}]\n";

    private final String DEFAULT_EXP_INFO = "{\n" +
            "    \"type\": \"2016_summer\",\n" +
            "    \"period\": 7\n" +
            "}";

    private final int DEFAULT_PERIOD=7;

    private SharedPreferences mPref;
    private Context mContext;
    private AccessibilityManager mAccessibilityManager;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Config(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mContext = context;
        mAccessibilityManager = (AccessibilityManager)mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public boolean isSendUserInfo() {
        return mPref.getBoolean(IS_SEND_USER_INFO, false);
    }

    public void saveSuccessSendUserInfo() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_SEND_USER_INFO, true);
        editor.commit();
    }

    public boolean isCompletePresurvey() {
        return mPref.getBoolean(IS_COMPLETE_PRESURVEY, false);
    }

    public void setCompletePresurvey() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_COMPLETE_PRESURVEY, true);
        editor.commit();
    }

    public boolean isCompletePostsurvey() {
        return mPref.getBoolean(IS_COMPLETE_POSTSURVEY, false);
    }

    public void setCompletePostsurvey() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_COMPLETE_POSTSURVEY, true);
        editor.commit();
    }

    public String getExpStartDate() {
        return mPref.getString(EXP_START_DATE, "");
    }

    public void testSetExpStartDate(String date) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(EXP_START_DATE, date);
        editor.commit();
    }

    public void saveExpStartDateIfNeed() {
        String startDate = getExpStartDate();
        if (startDate.isEmpty()) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(EXP_START_DATE, mDateFormat.format(Calendar.getInstance().getTime()));
            editor.commit();
        }
    }

    public boolean isExpExpired(){
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(mDateFormat.parse(getExpStartDate()));
        }catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DATE, getExpDays());
        return Calendar.getInstance().getTime().after(calendar.getTime());
    }

    public String getUUID() {
        return mPref.getString(UUID, "");
    }

    public void saveUUID(String uuid) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(UUID, uuid);
        editor.commit();
    }

    public String getUserName() {
        return mPref.getString(USER_NAME, "");
    }

    public void setUserName(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(USER_NAME, name);
        editor.commit();
    }

    public String getPhoneNumber() {
        return mPref.getString(USER_PHONE, "");
    }

    public void setPhoneNumber(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(USER_PHONE, name);
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

        try {
            JSONArray array = new JSONArray(packags);
            if (packags.length() <=0)
                return;

            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(EXCLUDED_PACKAGE, packags);
            editor.commit();

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setExpInfo(String response) {
        if (!isValid(response))
            return;

        try {
            JSONObject object = new JSONObject(response);

            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(EXP_INFO, response);
            editor.commit();

        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public int getExpDays() {
        try {
            JSONObject object = new JSONObject(mPref.getString(EXP_INFO, DEFAULT_EXP_INFO));
            return object.getInt("period");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return DEFAULT_PERIOD;
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
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getHostInfo() throws MalformedURLException{
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
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "history"));
    }

    public URL getUserHistoryURL(String startDate, String startTime) throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + String.format(getURLInfoValue(object, "user_history"), getUUID(), startDate, startTime));
    }

    public URL getExcludedPackageURL() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "excluded_package"));
    }

    public String getPresurveyURLString() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "pre_survey") + "?user_id=" + getUUID();
    }

    public String getPostsurveyURLString() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "post_survey") + "?user_id=" + getUUID();
    }

    public URL getUserURL() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "user"));
    }

    public URL getExpInfoURL() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "exp_info"));
    }

    public URL getHostInfoURL() throws MalformedURLException {
        return new URL(HOST_INFO_URL);
    }

    public String getUsagePatternURLString() throws MalformedURLException {
        JSONObject object = getHostInfo();
        Log.d("trans", "**************************"+object.toString());
        return getURLInfoValue(object, "host") +"graph/?user_id=" + getUUID();
    }

    public boolean isHomeApp(String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null)
            return false;

        return packageName.equals(res.activityInfo.packageName);

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

    public boolean isWiFiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected());
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void startSurvey(String url) {
        mContext.startActivity(new Intent(mContext, WebViewActivity.class)
                .setAction(WebViewActivity.LOAD_URL)
                .putExtra("DATA", url));
    }

    public void startPresurvey() {
        try {
           startSurvey(getPostsurveyURLString());
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void startPostsurvey() {
        try {
            startSurvey( getPostsurveyURLString());
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAccessibilityEnabled() {
        List<AccessibilityServiceInfo> infos = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : infos) {
            if (info.eventTypes == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                return true;
            }
        }
        return false;
    }
}
