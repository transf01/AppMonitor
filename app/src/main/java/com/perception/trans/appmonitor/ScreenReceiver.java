package com.perception.trans.appmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
            sendIntent(context, MonitoringService.SCREEN_OFF);
        }else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            sendIntent(context, MonitoringService.SCREEN_ON);
        }
    }

    private void sendIntent(Context context, String action){
        Intent intent = new Intent(action);
        intent.setPackage("com.perception.trans.appmonitor");
        context.startService(intent);
    }
}
