package com.emotion.trans.appmonitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String logString;
            if (event.getPackageName() == null)
            {
                logString = "package name is null";
            }
            else
            {
                PackageManager pm = getApplicationContext().getPackageManager();
                try {
                    ApplicationInfo ai = pm.getApplicationInfo((String) event.getPackageName(), 0);
                    Intent intent = new Intent("startMonitoring");
                    intent.setPackage("com.emotion.trans.appmonitor");
                    intent.putExtra("AppName", pm.getApplicationLabel(ai));
                    intent.putExtra("PackageName", event.getPackageName());
                    startService(intent);
                    logString = (String)pm.getApplicationLabel(ai) + "(" + event.getPackageName()+")";
                }catch (PackageManager.NameNotFoundException e){
                    logString = "App name is null";
                }
            }

            Log.d("trans", logString);

//            ComponentName componentName = new ComponentName(
//                    event.getPackageName().toString(),
//                    event.getClassName().toString()
//            );
//
//            ActivityInfo activityInfo = tryGetActivity(componentName);
//            boolean isActivity = activityInfo != null;
//            if (isActivity)
//                Log.d("trans", componentName.flattenToShortString());
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
