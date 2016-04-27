package com.emotion.trans.appmonitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class WindowChangeDetectingService extends AccessibilityService {
    public WindowChangeDetectingService() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("trans", "-----------------------------------");
        try {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                PackageManager pm = getApplicationContext().getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo((String) event.getPackageName(), 0);
                    Intent intent = new Intent(this, MonitoringService.class).setAction(MonitoringService.START_MONITORING);
                    intent.putExtra("AppName", pm.getApplicationLabel(ai));
                    intent.putExtra("PackageName", event.getPackageName());
                    startService(intent);
                Log.d("trans",pm.getApplicationLabel(ai) + "(" + event.getPackageName() + " / " + event.getClassName() + ")");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
