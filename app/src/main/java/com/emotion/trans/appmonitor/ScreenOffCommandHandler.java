package com.emotion.trans.appmonitor;

import android.content.Intent;

import android.os.Handler;

/**
 * Created by trans on 2016-02-01.
 */
public class ScreenOffCommandHandler implements CommandHandler{
    private AppChangeListener mListener;

    @Override
    public void handle(Intent intent, Handler handler, Runnable runnable) {
        handler.removeCallbacks(runnable);
        mListener.handleAppStop();
    }

    @Override
    public void addChangeListener(AppChangeListener listener) {
        mListener = listener;
    }
}
