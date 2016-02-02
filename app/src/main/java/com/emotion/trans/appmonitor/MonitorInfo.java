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

    public MonitorInfo(AppInfo appInfo, Date startTime) {
        mAppInfo = appInfo;
        mStartTime = startTime;
    }

    public void save(Date endTime) {
        Log.d("trans", toString());
    }

    @Override
    public String toString() {
        return "[MonitorInfo] App:" + mAppInfo.toString()+"StartTime:"+mStartTime.toString();
    }
}
