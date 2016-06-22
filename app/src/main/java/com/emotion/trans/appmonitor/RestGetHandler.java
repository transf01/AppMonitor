package com.emotion.trans.appmonitor;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by trans on 2016-06-22.
 */
public interface RestGetHandler {
    public URL getURL() throws MalformedURLException;
    public void handle(String response);
}
