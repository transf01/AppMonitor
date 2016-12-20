package com.perception.trans.appmonitor;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

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
class Config {
//    static final String HOST = "http://192.168.0.217:8000/api/";
//    //static final String HOST = "http://155.230.192.46:8000/api/";
//
//    static final String HISTORY_URL = HOST + "history";
//    static final String USER_URL = HOST + "user";
//    static final String EXCLUDED_PACKAGE_URL = HOST + "excluded_package/";

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    static final String PREF_NAME = "pref";
    private static final String URL_INFO = "URL_INFO";
    private static final String UUID = "UUID";
    private static final String USER_NAME = "NAME";
    private static final String USER_PHONE = "PHONE";
    private static final String EXP_CODE = "EXP_CODE";
    private static final String EXP_INFO = "EXP_INFO";
    private static final String EXP_START_DATE = "EXP_START_DATE";
    private static final String EXCLUDED_PACKAGE = "EXCLUDED_PACKAGE";
    private static final String IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    private static final String IS_COMPLETE_PRESURVEY = "IS_COMPLETE_PRESURVEY";
    private static final String IS_COMPLETE_POSTSURVEY = "IS_COMPLETE_POSTSURVEY";
    private static final String HOST_INFO_URL = "http://transf01.github.io/app_monitor_rest/";

    private static final String DEFAULT_URL_INFO = "{ \"host\" : \"http://155.230.192.46:8001/\", " +
            "\"api_base\" : \"api/\", " +
            "\"history\" : \"history/\", " +
            "\"user_history\" : \"%s/date/%s/time/%s/\", " +
            "\"user\" : \"user/\", " +
            "\"goal\" : \"goal/\", " +
            "\"excluded_package\": \"excluded_package/\", " +
            "\"pre_survey\" : \"survey/2/\", " +
            "\"post_survey\" : \"survey/3/\", " +
            "\"exp_info\" : \"exp_info/2016_winter\", " +
            "\"privacy\" : \"privacy/\" }";

    private static final String DEFAULT_EXCLUDE_PACKAGE = "[{\"package_name\":\"com.android.settings\"}," +
            "{\"package_name\":\"com.android.keyguard\"}," +
            "{\"package_name\":\"android\"}," +
            "{\"package_name\":\"com.cashslide\"}," +
            "{\"package_name\":\"com.nexon.nxplay\"}," +
            "{\"package_name\":\"com.android.systemui\"}]\n";

    private static final String DEFAULT_EXP_INFO = "{\n" +
            "    \"type\": \"2016_winter\",\n" +
            "    \"period\": 28\n" +
            "}";

    private final static int DEFAULT_PERIOD=7;
    private final static int DAY_OF_GATHERING_PERIOD = 7;

    private SharedPreferences mPref;
    private Context mContext;
    private AccessibilityManager mAccessibilityManager;

    Config(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mContext = context;
        mAccessibilityManager = (AccessibilityManager)mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    boolean isSendUserInfo() {
        return mPref.getBoolean(IS_SEND_USER_INFO, false);
    }

    void saveSuccessSendUserInfo() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_SEND_USER_INFO, true);
        editor.commit();
    }

    boolean isCompletePreSurvey() {
        return mPref.getBoolean(IS_COMPLETE_PRESURVEY, false);
    }

    void setCompletePresurvey() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_COMPLETE_PRESURVEY, true);
        editor.commit();
    }

    boolean isCompletePostsurvey() {
        return mPref.getBoolean(IS_COMPLETE_POSTSURVEY, false);
    }

    void setCompletePostsurvey() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(IS_COMPLETE_POSTSURVEY, true);
        editor.commit();
    }

    String getExpStartDate() {
        return mPref.getString(EXP_START_DATE, "");
    }

    void testSetExpStartDate(String date) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(EXP_START_DATE, date);
        editor.commit();
    }

    void saveExpStartDateIfNeed() {
        String startDate = getExpStartDate();
        if (startDate.isEmpty()) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(EXP_START_DATE, DATE_FORMAT.format(Calendar.getInstance().getTime()));
            editor.commit();
        }
    }

    private boolean isAfter(int day){
        Calendar endDay = Calendar.getInstance();
        boolean result = false;
        try {
            endDay.setTime(DATE_FORMAT.parse(getExpStartDate()));
            endDay.add(Calendar.DATE, day);
            result =  Calendar.getInstance().getTime().after(endDay.getTime());
            Log.d("trans", "today is"+ (result?" ":" NOT ")+"after " + day + " day(s) from " + getExpStartDate());
        }catch (ParseException e) {
            Log.e("trans", e.getStackTrace().toString());
        }
        return result;
    }

    boolean isExpExpired(){
        return isAfter(getExpDays());
    }

    boolean isValidNotiAlarmPeriod() {
        return (isAfter(DAY_OF_GATHERING_PERIOD) && !isExpExpired());
    }

    String getUUID() {
        return mPref.getString(UUID, "");
    }

    void saveUUID(String uuid) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(UUID, uuid);
        editor.commit();
    }

    String getUserName() {
        return mPref.getString(USER_NAME, "");
    }

    void setUserName(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(USER_NAME, name);
        editor.commit();
    }

    String getPhoneNumber() {
        return mPref.getString(USER_PHONE, "");
    }

    void setPhoneNumber(String name) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(USER_PHONE, name);
        editor.commit();
    }

    boolean isNeedUserInfo() {
        return getUserName().isEmpty() || getPhoneNumber().isEmpty();
    }

    String getExpCode() {
        return mPref.getString(EXP_CODE, "0");
    }

    void setExpCode(String expCode) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(EXP_CODE, expCode);
        editor.commit();
    }

    private boolean isValid(String string) {
        return string != null && !string.isEmpty();
    }

    void setExcludedPackage(String packags) {
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

    void setExpInfo(String response) {
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

    private int getExpDays() {
        try {
            JSONObject object = new JSONObject(mPref.getString(EXP_INFO, DEFAULT_EXP_INFO));
            return object.getInt("period");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return DEFAULT_PERIOD;
    }

    void setURLInfo(String jsonString) {
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

    URL getHistoryURL() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "history"));
    }

    URL getUserHistoryURL(String startDate, String startTime) throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + String.format(getURLInfoValue(object, "user_history"), getUUID(), startDate, startTime));
    }

    URL getExcludedPackageURL() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "excluded_package"));
    }

    String getPreSurveyURLString() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "pre_survey") + "?user_id=" + getUUID();
    }

    private String getPostsurveyURLString() throws  MalformedURLException{
        JSONObject object = getHostInfo();
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "post_survey") + "?user_id=" + getUUID();
    }

    private String getPrivacyURLString() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return getURLInfoValue(object, "host") + getURLInfoValue(object, "privacy");
    }

    URL getUserURL() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "user"));
    }

    URL getGoalURL() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "goal"));
    }

    URL getExpInfoURL() throws MalformedURLException {
        JSONObject object = getHostInfo();
        return new URL(getBaseURLString(object) + getURLInfoValue(object, "exp_info"));
    }

    URL getHostInfoURL() throws MalformedURLException {
        return new URL(HOST_INFO_URL);
    }

    String getUsagePatternURLString() throws MalformedURLException {
        JSONObject object = getHostInfo();
        Log.d("trans", "**************************"+object.toString());
        return getURLInfoValue(object, "host") +"graph/?user_id=" + getUUID();
    }

    boolean isHomeApp(AppInfo info) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);
        return res.activityInfo != null && info.getPackageName().equals(res.activityInfo.packageName);

    }

    boolean isExcludedPackage(CharSequence packageName) {
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

    boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void startWebView(String url) {
        mContext.startActivity(new Intent(mContext, WebViewActivity.class)
                .setAction(WebViewActivity.LOAD_URL)
                .putExtra("DATA", url));
    }

    private void showMalforedUrlError() {
        Toast.makeText(mContext, R.string.bad_url_warning, Toast.LENGTH_SHORT).show();
    }

    void startPresurvey() {
        try {
           startWebView(getPostsurveyURLString());
        }catch (MalformedURLException e) {
            showMalforedUrlError();
        }
    }

    void startPostsurvey() {
        try {
            startWebView( getPostsurveyURLString());
        }catch (MalformedURLException e) {
            showMalforedUrlError();
        }
    }

    void startInformation() {
        try {
            startWebView( getPrivacyURLString());
        }catch (MalformedURLException e) {
            showMalforedUrlError();
        }
    }

    boolean isAccessibilityEnabled() {
        List<AccessibilityServiceInfo> infoList = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : infoList) {
            if (info.eventTypes == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                return true;
            }
        }
        return false;
    }

    boolean isNeedSetTodayGoal(GoalSettingAlarm alarm, DataBaseHelper db) {
        Calendar now = Calendar.getInstance();
        Pair<Integer, Integer> time = alarm.getTime(mContext);
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, time.first);
        alarmTime.set(Calendar.MINUTE, time.second);

        if (now.after(alarmTime)) {
            Cursor cursor = db.getGoalByDate(Config.DATE_FORMAT.format(now.getTime()));
            int count = cursor.getCount();
            cursor.close();
            return count == 0;
        }
        return false;
    }

}
