package com.emotion.trans.appmonitor;

import android.content.Intent;

import java.util.Calendar;
import android.os.Handler;
import android.util.Log;

/**
 * Created by trans on 2016-02-01.
 */
public class WindowChangeCommandHandler implements CommandHandler{

    AppChangeListener mListener;
    AppInfo mPreviousAppInfo = null;

    public void handle(Intent intent, Handler handler, Runnable runnable) {

        AppInfo appInfo = new AppInfo(intent.getStringExtra("AppName"), intent.getStringExtra("PackageName"));

        if (appInfo.isDifferent(mPreviousAppInfo)) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, MONITORING_JUDGE_TIME);
            notifyAppChangeInfo(appInfo);
            mPreviousAppInfo = appInfo;
        }
    }

    @Override
    public void addChangeListener(AppChangeListener listener) {
        mListener = listener;
    }

    private void notifyAppChangeInfo(AppInfo appInfo) {
        if (mListener == null)
            return;

        mListener.handleAppStop(Calendar.getInstance().getTime());
        mListener.handleChangedAppStartTime(Calendar.getInstance().getTime());
        mListener.handleChangedAppInfo(appInfo);
    }
}
