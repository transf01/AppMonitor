package com.emotion.trans.appmonitor;

import android.content.Intent;

import android.os.Handler;

/**
 * Created by trans on 2016-02-01.
 */
public interface CommandHandler {
    final int MONITORING_JUDGE_TIME = 10000;

    public void handle(Intent intent, Handler handler, Runnable runnable);
}
