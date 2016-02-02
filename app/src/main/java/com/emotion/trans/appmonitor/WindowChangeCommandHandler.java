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
    String previousAppName = null;

    public void handle(Intent intent, Handler handler, Runnable runnable) {
        String AppName = intent.getStringExtra("AppName");
        if (AppName != null && !AppName.equals(previousAppName)) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, MONITORING_JUDGE_TIME);
            notifyAppChangeInfo(AppName);
            previousAppName = AppName;
        }
    }

    @Override
    public void addChangeListener(AppChangeListener listener) {
        mListener = listener;
    }

    private void notifyAppChangeInfo(String AppName) {
        if (mListener == null)
            return;

        mListener.handleAppStop(Calendar.getInstance().getTime());
        mListener.handleChangedAppStartTime(Calendar.getInstance().getTime());
        mListener.handleChangedAppName(AppName);
    }
}
