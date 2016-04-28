package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public class MonitorInfo {

    private AppInfo mAppInfo;
    private Date mStartTime;
    private DataBaseHelper mdb;

    public MonitorInfo(AppInfo appInfo, Date startTime, long startNanotime, DataBaseHelper db) {
        mAppInfo = appInfo;
        mStartTime = startTime;
        mdb = db;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    private long getUseTime(Date endTime) {
        Log.d("trans", "start:" + mStartTime.toString() + " end:" + endTime.toString() + " usetime:" + (endTime.getTime() - mStartTime.getTime())/1000);
        return (endTime.getTime() - mStartTime.getTime())/1000;
    }

    public long save(Date endTime) {
        return mdb.addData(mAppInfo.getAppName(), mAppInfo.getPackageName(), mStartTime, getUseTime(endTime));
    }

    @Override
    public String toString() {
        return "[MonitorInfo] App:" + mAppInfo.toString()+"StartTime:"+mStartTime.toString();
    }
}
