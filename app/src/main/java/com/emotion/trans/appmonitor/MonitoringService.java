package com.emotion.trans.appmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class MonitoringService extends Service {
    Handler mHandler;
    AppInfoRunnable mRunnable ;

    public MonitoringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mRunnable = new AppInfoRunnable();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler.removeCallbacks(mRunnable);
        String AppName = intent.getStringExtra("AppName");
        if (AppName != null) {
            mRunnable.setAppInfo(AppName);
            mHandler.postDelayed(mRunnable, 10000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class AppInfoRunnable implements Runnable {

        private String mAppInfo;

        public void setAppInfo(String info){
            mAppInfo = info;
        }

        public String getAppInfo(){
            return mAppInfo;
        }

        @Override
        public void run() {
            Log.d("trans", "-----------monitoring-----------"+mAppInfo);
        }
    }
}
