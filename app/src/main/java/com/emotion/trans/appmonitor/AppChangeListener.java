package com.emotion.trans.appmonitor;

import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public interface AppChangeListener {

    public void handleChangedAppInfo(AppInfo appInfo);
    public void handleChangedAppStartTime();
    public void handleAppStop();
}
