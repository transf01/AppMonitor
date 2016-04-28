package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

/**
 * Created by trans on 2016-02-02.
 */
public class AppInfo {
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String SETTINGS = "com.android.settings";

    private String mAppName;
    private String mPackageName;

    public AppInfo(String appName, String packageNage) {
        mAppName = appName;
        mPackageName = packageNage;
    }

    @Override
    public String toString() {
        return mAppName+ "(" + mPackageName+")";
    }

    public String getAppName() {
        return mAppName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    private boolean isHomeApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null)
            return false;

        if (mPackageName.equals(res.activityInfo.packageName))
            return true;

        return false;
    }

    public boolean isCheckable(Context context) {
        if (isHomeApp(context))
            return false;
        if (SYSTEM_UI.equals(mPackageName))
            return false;

        if (SETTINGS.equals(mPackageName))
            return false;

        return true;
    }

    private boolean isValid() {
        if (mAppName == null || mPackageName == null)
            return false;
        return true;
    }

    public boolean isDifferent(AppInfo info) {
        if (info == null)
            return true;

        if (info.isValid()) {
            if (!this.mAppName.equals(info.mAppName) || !this.mPackageName.equals(info.mPackageName))
                return true;
        }
        return false;
    }
}
