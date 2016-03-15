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
    ScreenReceiver mScreenReceiver = new ScreenReceiver();
    Monitor mMonitor = null;
    public static final String SEND_DATA = "SEND_DATA";
    private DataBaseHelper mdb;
    private SharedPreferences mPref;
    private final String UUID = "UUID";
    public static final String IS_SEND_USER_INFO = "IS_SEND_USER_INFO";
    public static final String HOST = "http://192.168.0.63:8000/api/";

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

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void sendUserInfo() {
        if (!mPref.getBoolean(IS_SEND_USER_INFO, false) && isConnected()) {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            UserInfo info = new UserInfo(getUUID(), "test_name", tm.getLine1Number(), mPref);
            info.send();
        }
    }

    private String getUUID() {
        String uuid = mPref.getString(UUID, "");
        if ("".equals(uuid)) {
            uuid = createUUID();
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(UUID, uuid);
        }
        return uuid;
    }

    private String createUUID() {
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }


    private void unregisterScreenReceiver() {
        unregisterReceiver(mScreenReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenReceiver();
        mPref = getSharedPreferences("pref", MODE_PRIVATE);
        mdb = new DataBaseHelper(this);
        mdb.open();
        mMonitor = new Monitor(this, mdb, getUUID());
        sendUserInfo();
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

}
