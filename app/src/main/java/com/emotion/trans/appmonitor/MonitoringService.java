package com.emotion.trans.appmonitor;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class MonitoringService extends Service {
    Handler mHandler;
    AppInfoRunnable mRunnable ;
    ScreenReceiver mScreenReceiver = new ScreenReceiver();

    public MonitoringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void registerScreenReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterScreenReceiver()
    {
        unregisterReceiver(mScreenReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mRunnable = new AppInfoRunnable();
        registerScreenReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler.removeCallbacks(mRunnable);
        String action = intent.getAction();

        Log.d("trans", action);

        String AppName = intent.getStringExtra("AppName");
        if (AppName != null) {
            mRunnable.setAppInfo(AppName);
            mHandler.postDelayed(mRunnable, 10000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenReceiver();
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
