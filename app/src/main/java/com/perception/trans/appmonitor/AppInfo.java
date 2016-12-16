package com.perception.trans.appmonitor;

import android.util.Log;

/**
 * Created by trans on 2016-02-02.
 */
public class AppInfo {
    private String mAppName;
    private String mPackageName;
    private AppRuntimeInfo mRuntimeInfo = null;

    public AppInfo(String appName, String packageName) {
        mAppName = appName;
        mPackageName = packageName;
    }

    @Override
    public String toString() {
        return mAppName+ "(" + mPackageName+")";
    }

    public String getPackageName() {
        return mPackageName;
    }

    public boolean isCheckable(Config config) {
        return !(config.isHomeApp(this) || config.isExcludedPackage(mPackageName));
    }

    public boolean isOnMonitoring() {
        return mRuntimeInfo != null;
    }

    public void startRuntime() {
        mRuntimeInfo = new AppRuntimeInfo();
    }

    public void stopRuntime() {
        if (mRuntimeInfo != null) {
            mRuntimeInfo.stop();
            Log.d("trans", mRuntimeInfo.toString());
        }
    }

    public void save(DataBaseHelper db) {
        db.addData(mAppName, mPackageName, mRuntimeInfo);
        mRuntimeInfo = null;
    }

    private boolean isValid() {
        return mAppName != null && mPackageName != null;
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
