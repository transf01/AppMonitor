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
    private Context mContext;

    public MonitorInfo(Context context, AppInfo appInfo, Date startTime) {
        mContext = context;
        mAppInfo = appInfo;
        mStartTime = startTime;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    private long getUseTime(Date endTime) {
        return (endTime.getTime() - mStartTime.getTime())/1000;
    }

    public void save(Date endTime) {
        DataBaseHelper dbHelper = new DataBaseHelper(mContext);
        dbHelper.open();
        dbHelper.addData(mAppInfo.getAppName(), mAppInfo.getPackageName(), mStartTime, getUseTime(endTime));
        dbHelper.close();
    }

    @Override
    public String toString() {
        return "[MonitorInfo] App:" + mAppInfo.toString()+"StartTime:"+mStartTime.toString();
    }
}
