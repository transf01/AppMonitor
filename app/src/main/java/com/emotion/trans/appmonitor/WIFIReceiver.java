package com.emotion.trans.appmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WIFIReceiver extends BroadcastReceiver {
    public WIFIReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.d("trans", "-------------------"+networkInfo.toString());
            if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d("trans", "-------------------------wifi --------------------");
                Intent i = new Intent(context, MonitoringService.class);
                i.setAction(MonitoringService.SEND_DATA);
                context.startService(i);
            }
        }
    }
}
