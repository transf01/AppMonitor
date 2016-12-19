package com.perception.trans.appmonitor;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


public class MonitoringService extends Service {
    public static final String SEND_DATA = "SEND_DATA";
    public static final String START_MONITORING = "startMonitoring";
    public static final String SCREEN_ON= "screenOn";
    public static final String SCREEN_OFF= "screenOff";
    public static final String ALARM= "ALARM";

    private ScreenReceiver mScreenReceiver = new ScreenReceiver();
    private Monitor mMonitor = null;
    private DataBaseHelper mdb;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterScreenReceiver() {
        unregisterReceiver(mScreenReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenReceiver();
        mdb = new DataBaseHelper(this);
        mdb.open();
        mMonitor = new Monitor(this, mdb);
        GoalSettingAlarm.getInstance().start(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMonitor.checkCondition();
        mMonitor.handleCommand(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenReceiver();
        mdb.close();
    }


}

