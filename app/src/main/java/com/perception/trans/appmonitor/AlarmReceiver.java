package com.perception.trans.appmonitor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("trans", "Alarm receive");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(context.getResources().getString(R.string.alarm_noti_title))
                .setContentText(context.getResources().getString(R.string.alarm_noti_text))
                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                .setPriority( android.support.v4.app.NotificationCompat.PRIORITY_MAX )
                .setSound(RingtoneManager.getActualDefaultRingtoneUri( context, RingtoneManager.TYPE_NOTIFICATION ))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, GoalActivity.class), PendingIntent.FLAG_ONE_SHOT));


        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, builder.build());
    }
}
