package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

/**
 * Created by trans on 2016-02-02.
 */
public class AppInfo {
    private String mAppName;
    private String mPackageName;
    private Config mConfig;

    public AppInfo(String appName, String packageNage, Config config) {
        mAppName = appName;
        mPackageName = packageNage;
        mConfig = config;
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

    public boolean isHomeApp() {
        return mConfig.isHomeApp(mPackageName);
    }

    public boolean isCheckable() {
        return !(isHomeApp() || mConfig.isExcludedPackage(mPackageName));
    }

    private boolean isValid() {
        return !(mAppName == null || mPackageName == null);
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
