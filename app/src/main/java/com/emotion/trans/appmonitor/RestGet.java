package com.emotion.trans.appmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by trans on 2016-06-22.
 */
public class RestGet {
    private RestGetHandler mHandler;

    public RestGet(RestGetHandler handler){
        mHandler = handler;
    }


    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection)mHandler.getURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);
        return conn;
    }

    private String getResponse(HttpURLConnection conn) throws IOException {
        if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        }
        return null;
    }

    public void handle() {
        HttpURLConnection connection = null;
        try {
            connection = getConnection();
            if (mHandler != null) {
                mHandler.handle(getResponse(connection));
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
