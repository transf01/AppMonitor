package com.emotion.trans.appmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
            sendIntent(context, "screenOff");
        }else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            sendIntent(context, "screenOn");
        }
    }

    private void sendIntent(Context context, String action){
        Intent intent = new Intent(action);
        intent.setPackage("com.emotion.trans.appmonitor");
        context.startService(intent);
    }
}
