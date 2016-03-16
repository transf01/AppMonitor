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

        if (!mConfig.isSendUserInfo()) {
            sendUserInfo(mConfig.getUserName(), mConfig.getPhoneNumber());
        }

        mMonitor = new Monitor(this, mdb, mConfig.getUUID());
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


    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private void sendUserInfo(String name, String phone) {
        if (!mConfig.isSendUserInfo() && isConnected()) {
            new UserInfo(mConfig.getUUID(), name, phone, mConfig).send();
        }
    }

    private void createUUID(Config config) {
        String uuid = config.getUUID();
        if (uuid.isEmpty()) {
            uuid = getUUID();
            config.saveUUID(uuid);
        }
    }

    private String getUUID() {
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }
}
