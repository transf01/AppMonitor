package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;
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
    private AppInfo mCurrentAppInfo;
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
        addCommandHandler(MonitoringService.SEND_DATA, new SendData());
    }

    private void startMonitoring(){
        if (!mCurrentAppInfo.isHomeApp(mContext)) {
            mInfo = new MonitorInfo(mContext, mCurrentAppInfo, mAppStartTime);
            Log.d("trans", "### start : " + mInfo.toString());
        }
    }

    @Override
    public void handleChangedAppInfo(AppInfo appInfo) {
        mCurrentAppInfo = appInfo;
    }

    @Override
    public void handleChangedAppStartTime(Date startTime) {
        mAppStartTime = startTime;
    }

    @Override
    public void handleAppStop(Date endTime) {
        if (mInfo != null) {
            mInfo.save(endTime);
            mInfo = null;
        }
    }

    public void handleCommand(Intent intent) {

        if (intent == null)   return;
        String action = intent.getAction();

        CommandHandler handler = mCommandHandlerMap.get(action);
        if (handler != null) {
            handler.handle(intent, mHandler, mRunnable);
        }
    }

    public void clearHandler() {
        mHandler.removeCallbacks(mRunnable);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOffCommandHandler implements CommandHandler{
        private AppChangeListener mListener;

        @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            handler.removeCallbacks(runnable);
            mListener.handleAppStop(Calendar.getInstance().getTime());
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {
            mListener = listener;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOnCommandHandler implements CommandHandler {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class WindowChangeCommandHandler implements CommandHandler{

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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class SendData implements CommandHandler {
        @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            Log.d("trans", "-----------------send data---------------------!!");
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {

        }
    }
}
