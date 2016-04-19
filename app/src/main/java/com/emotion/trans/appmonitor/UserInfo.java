package com.emotion.trans.appmonitor;

import android.app.Activity;
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


    private Config mConfig;

    public UserInfo(Config config) {
        mConfig = config;

    }

    private String getJSONData() {
        JSONObject object = new JSONObject();
        try {
            object.put("uuid", mConfig.getUUID());
            object.put("name", mConfig.getUserName());
            object.put("cellphone", mConfig.getPhoneNumber());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public void send() {
        try {
            PostUser postUser = new PostUser(new URL(Config.USER_URL));
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
            }
        }
    }
}
