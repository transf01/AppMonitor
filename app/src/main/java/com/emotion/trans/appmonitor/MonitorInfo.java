package com.emotion.trans.appmonitor;

import android.util.Log;

import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public class MonitorInfo {

    private String mAppName;
    private Date mStartTime;

    public MonitorInfo(String appName, Date startTime) {
        mAppName = appName;
        mStartTime = startTime;
    }

    public void save(Date endTime) {
        Log.d("trans", toString());
    }

    @Override
    public String toString() {
        return "[MonitorInfo] App:" + mAppName+"StartTime:"+mStartTime.toString();
    }
}
