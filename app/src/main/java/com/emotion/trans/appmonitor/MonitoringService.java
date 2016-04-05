package com.emotion.trans.appmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;
import java.util.UUID;


public class MonitoringService extends Service {
    public static final String SEND_DATA = "SEND_DATA";

    private ScreenReceiver mScreenReceiver = new ScreenReceiver();
    private Monitor mMonitor = null;
    private DataBaseHelper mdb;
    private Config mConfig;

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
        mConfig = new Config(this);

        createUUID(mConfig);

        mMonitor = new Monitor(this, mdb, mConfig);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMonitor.handleCommand(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenReceiver();
        mMonitor.clearHandler();
        mdb.close();
    }


    private void createUUID(Config config) {
        String uuid = config.getUUID();
        if (uuid.isEmpty()) {
            uuid = getUUID();
            config.saveUUID(uuid);
        }
    }

    private String getUUID() {
        UUID deviceUuid = new UUID(mConfig.getUserName().hashCode(), ((long)mConfig.getPhoneNumber().hashCode() << 32) | new Date().hashCode());
        return deviceUuid.toString();
    }
}
