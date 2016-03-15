package com.emotion.trans.appmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
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

    private String mUUID, mName, mPhone;
    private SharedPreferences mPref;

    public UserInfo(String uuid, String name, String phone, SharedPreferences pref) {
        mUUID = uuid;
        mName = name;
        mPhone = phone==null?"None":phone;
        mPref = pref;
    }

    private String getJSONData() {
        JSONObject object = new JSONObject();
        try {
            object.put("uuid", mUUID);
            object.put("name", mName);
            object.put("cellphone", mPhone);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public void send() {
        try {
            PostUser postUser = new PostUser(new URL(MonitoringService.HOST+"user"));
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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("trans", "--------user save result : " + aBoolean);
            SharedPreferences.Editor editor = mPref.edit();
            editor.putBoolean(MonitoringService.IS_SEND_USER_INFO, aBoolean);
            editor.commit();
        }
    }
}
