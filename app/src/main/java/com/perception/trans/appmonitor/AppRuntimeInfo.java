package com.perception.trans.appmonitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by inspiration on 2016-04-27.
 */
public class AppRuntimeInfo {

    private long mRunTime;
    private Date mStartDate;
    private long mStartSystemTime;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss");

    public AppRuntimeInfo() {
        mStartDate = Calendar.getInstance().getTime();
        mStartSystemTime = System.nanoTime();
    }

    public AppRuntimeInfo(Date startDate, long runTime) {
        mStartDate = startDate;
        mRunTime = runTime;
    }

    public void stop() {
        mRunTime = (System.nanoTime() - mStartSystemTime)/1000000000;
    }

    public String getStartDateString() {
        return mDateFormat.format(mStartDate);
    }

    public String getStartTimeString() {
        return mTimeFormat.format(mStartDate);
    }

    public long getRunTime() {
        return mRunTime;
    }

    @Override
    public String toString() {
        return "Start: "+getStartDateString()+" " + getStartTimeString() + " runtime:"+getRunTime();
    }
}