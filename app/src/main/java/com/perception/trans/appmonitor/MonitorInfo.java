package com.perception.trans.appmonitor;

import android.util.Log;

/**
 * Created by trans on 2016-02-02.
 */
public class MonitorInfo {

    private AppInfo mAppInfo;
    private DataBaseHelper mdb;

    public MonitorInfo(AppInfo appInfo, DataBaseHelper db) {
        mAppInfo = appInfo;
        mdb = db;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    public long save(RuntimeInfo runtimeInfo) {
        Log.d("trans", runtimeInfo.toString());
        return mdb.addData(mAppInfo.getAppName(), mAppInfo.getPackageName(), runtimeInfo);
    }

    @Override
    public String toString() {
        return "[MonitorInfo] App:" + mAppInfo.toString();
    }
}
