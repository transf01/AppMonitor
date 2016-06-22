package com.emotion.trans.appmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by trans on 2016-03-15.
 */
public class UserInfo {

    private Context mContext;
    private Config mConfig;

    public UserInfo(Context context, Config config) {
        mConfig = config;
        mContext = context;
    }

    private String getJSONData() {
        JSONObject object = new JSONObject();
        try {
            object.put("uuid", mConfig.getUUID());
            object.put("name", mConfig.getUserName());
            object.put("cellphone", mConfig.getPhoneNumber());
            object.put("experiment_code", mConfig.getExpCode());
            object.put("experiment_start_date", mConfig.getExpStartDate());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public void send() {
        try {
            PostUser postUser = new PostUser(mConfig.getUserURL());
            postUser.execute(getJSONData());
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PostUser extends AsyncTask<String, Void, Boolean> {
        private URL mUrl;
        public PostUser(URL url) {
            mUrl = url;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            try{
                Log.d("trans", "---------connecting to " + mUrl.toString());
                HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                os.write(params[0].getBytes());
                os.flush();


                InputStream in;
                if(conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
                    in = conn.getErrorStream();
                else
                    in = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuffer response = new StringBuffer();
                String line;
                while ((line=reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d("trans", response.toString());

                if (HttpURLConnection.HTTP_CREATED == conn.getResponseCode()) {
                    return true;
                }

            }catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean resule) {
            super.onPostExecute(resule);
            Log.d("trans", "--------user save result : " + resule);
            if (resule) {
                mConfig.saveSuccessSendUserInfo();
                mContext.startService(new Intent(mContext, MonitoringService.class).setAction(MonitoringService.SEND_DATA));
            }
        }
    }
}
