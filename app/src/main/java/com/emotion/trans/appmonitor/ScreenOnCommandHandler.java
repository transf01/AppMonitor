package com.emotion.trans.appmonitor;

import android.content.Intent;

import android.os.Handler;

import java.util.Calendar;

/**
 * Created by trans on 2016-02-01.
 */
public class ScreenOnCommandHandler implements CommandHandler {
    private AppChangeListener mListener;

    @Override
    public void handle(Intent intent, Handler handler, Runnable runnable) {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, MONITORING_JUDGE_TIME);
        mListener.handleChangedAppStartTime(Calendar.getInstance().getTime());
    }

    @Override
    public void addChangeListener(AppChangeListener listener) {
        mListener = listener;
    }

}
