package com.emotion.trans.appmonitor;

import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public interface AppChangeListener {

    public void handleChangedAppName(String appName);
    public void handleChangedAppStartTime(Date startTime);
    public void handleAppStop(Date endTime);
}
