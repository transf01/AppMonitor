package com.perception.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
 * Created by trans on 2016-12-20.
 */

public class GoalInfo {

    private Context mContext;
    private Config mConfig;

    public GoalInfo(Context context, Config config) {
        mContext = context;
        mConfig = config;
    }

    private String getJSONData(Cursor cursor) {
        JSONObject object = new JSONObject();
        try {
            object.put("uuid", mConfig.getUUID());
            object.put("goal_date", cursor.getString(1));
            object.put("confidence", cursor.getInt(2));
            object.put("importance", cursor.getInt(3));
            object.put("goal", cursor.getInt(4)*60);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("trans", "[goal]"+object.toString());
        return object.toString();
    }

    public void send(Cursor cursor) {
        try {
            GoalInfo.PostGoal postUser = new GoalInfo.PostGoal(mConfig.getGoalURL());
            postUser.execute(getJSONData(cursor), String.valueOf(cursor.getInt(0)));
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PostGoal extends AsyncTask<String, Void, String> {
        private URL mUrl;
        public PostGoal(URL url) {
            mUrl = url;
        }
        @Override
        protected String doInBackground(String... params) {
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
                    return params[1];
                }

            }catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String rowID) {
            super.onPostExecute(rowID);
            if (rowID != null) {
                mContext.startService(new Intent(mContext, MonitoringService.class)
                        .setAction(MonitoringService.SENT_TODAY_GOAL)
                        .putExtra(MonitoringService.EXTRA_GOAL_ID, rowID));
            }

            mContext.startService(new Intent(mContext, MonitoringService.class).setAction(MonitoringService.SEND_DATA));

        }
    }
}
