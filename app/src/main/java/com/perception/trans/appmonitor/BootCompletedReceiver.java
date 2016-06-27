package com.perception.trans.appmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Config config = new Config(context);
            if (!config.isAccessibilityEnabled()) {
                context.startActivity(new Intent(context, MainActivity.class)
                        .setAction(MainActivity.CHECK_ACCESSIBILITY)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
}
