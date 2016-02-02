package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by trans on 2016-02-02.
 */
public class Monitor implements AppChangeListener{

    private Context mContext;
    private Handler mHandler;
    private Runnable mRunnable ;
    private HashMap<String, CommandHandler> mCommandHandlerMap = new HashMap<>();
    private String mCurrentAppName;
    private Date mAppStartTime;
    private MonitorInfo mInfo = null;

    public Monitor(Context context) {
        mContext = context;
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                startMonitoring();
            }
        };
        initCommandHandlerMap();
    }

    private void addCommandHandler(String key, CommandHandler handler) {
        handler.addChangeListener(this);
        mCommandHandlerMap.put(key, handler);
    }

    private void initCommandHandlerMap() {
        addCommandHandler("startMonitoring", new WindowChangeCommandHandler());
        addCommandHandler("screenOn", new ScreenOnCommandHandler());
        addCommandHandler("screenOff", new ScreenOffCommandHandler());
    }

    private void startMonitoring(){
        mInfo = new MonitorInfo(mCurrentAppName, mAppStartTime);
        Log.d("trans", "### start : " + mInfo.toString());
    }


    private void saveRunningTime(Date startTime, Date endTime) {
        long diff = (endTime.getTime() - startTime.getTime())/1000;
    }


    @Override
    public void handleChangedAppName(String appName) {
        mCurrentAppName = appName;
    }

    @Override
    public void handleChangedAppStartTime(Date startTime) {
        mAppStartTime = startTime;
    }

    @Override
    public void handleAppStop() {
        Log.d("trans", "### AppStop");
        if (mInfo != null) {
            mInfo.save();
            mInfo = null;
        }
    }

    public void handleCommand(Intent intent) {
        String action = intent.getAction();
        CommandHandler handler = mCommandHandlerMap.get(action);
        handler.handle(intent, mHandler, mRunnable);
    }

    public void clearHandler() {
        mHandler.removeCallbacks(mRunnable);
    }

}
