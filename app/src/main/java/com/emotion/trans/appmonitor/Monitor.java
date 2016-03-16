package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by trans on 2016-02-02.
 */
public class Monitor implements AppChangeListener{

    private Context mContext;
    private Handler mHandler;
    private Runnable mRunnable ;
    private HashMap<String, CommandHandler> mCommandHandlerMap = new HashMap<>();
    private AppInfo mCurrentAppInfo;
    private Date mAppStartTime;
    private MonitorInfo mInfo = null;
    private DataBaseHelper mdb;
    private String mUUID;

    public Monitor(Context context, DataBaseHelper db, String uuid) {
        mContext = context;
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                startMonitoring();
            }
        };
        mdb = db;
        mUUID = uuid;
        initCommandHandlerMap();
    }

    private void addCommandHandler(String key, CommandHandler handler) {
        handler.addChangeListener(this);
        mCommandHandlerMap.put(key, handler);
    }

    private void initCommandHandlerMap() {
        addCommandHandler("startMonitoring", new WindowChangeCommandHandler());
        addCommandHandler("screenOn", new ScreenOnCommandHandler());
        addCommandHandler("screenOff", new ScreenOffCommandHandler());
        addCommandHandler(MonitoringService.SEND_DATA, new SendData());
    }

    private void startMonitoring(){
        if (!mCurrentAppInfo.isHomeApp(mContext)) {
            mInfo = new MonitorInfo(mCurrentAppInfo, mAppStartTime, mdb);
            Log.d("trans", "### start : " + mInfo.toString());
        }
    }

    @Override
    public void handleChangedAppInfo(AppInfo appInfo) {
        mCurrentAppInfo = appInfo;
    }

    @Override
    public void handleChangedAppStartTime(Date startTime) {
        mAppStartTime = startTime;
    }

    private boolean isWiFiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected());
    }

    private String getURLString() {
        return Config.HISTORY_URL;
    }

    @Override
    public void handleAppStop(Date endTime) {
        if (mInfo != null) {
            long rowid = mInfo.save(endTime);
            mInfo = null;
            if(isWiFiConnected()) {
                try {
                    PostAppHistory post = new PostAppHistory(new URL(getURLString()));
                    post.execute(mdb.getSendDataByRowID(rowid));
                } catch (MalformedURLException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public void handleCommand(Intent intent) {

        if (intent == null)   return;
        String action = intent.getAction();

        CommandHandler handler = mCommandHandlerMap.get(action);
        if (handler != null) {
            handler.handle(intent, mHandler, mRunnable);
        }
    }

    public void clearHandler() {
        mHandler.removeCallbacks(mRunnable);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOffCommandHandler implements CommandHandler{
        private AppChangeListener mListener;

        @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            handler.removeCallbacks(runnable);
            mListener.handleAppStop(Calendar.getInstance().getTime());
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {
            mListener = listener;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOnCommandHandler implements CommandHandler {
        private AppChangeListener mListener;

        @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, MONITORING_JUDGE_TIME);
            mListener.handleChangedAppStartTime(Calendar.getInstance().getTime());
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {
            mListener = listener;
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class WindowChangeCommandHandler implements CommandHandler{

        AppChangeListener mListener;
        AppInfo mPreviousAppInfo = null;

        public void handle(Intent intent, Handler handler, Runnable runnable) {

            AppInfo appInfo = new AppInfo(intent.getStringExtra("AppName"), intent.getStringExtra("PackageName"));

            if (appInfo.isDifferent(mPreviousAppInfo)) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, MONITORING_JUDGE_TIME);
                notifyAppChangeInfo(appInfo);
                mPreviousAppInfo = appInfo;
            }
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {
            mListener = listener;
        }

        private void notifyAppChangeInfo(AppInfo appInfo) {
            if (mListener == null)
                return;

            mListener.handleAppStop(Calendar.getInstance().getTime());
            mListener.handleChangedAppStartTime(Calendar.getInstance().getTime());
            mListener.handleChangedAppInfo(appInfo);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class SendData implements CommandHandler {

         @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            try {
                PostAppHistory post = new PostAppHistory(new URL(getURLString()));
                post.execute(mdb.getSendData());
            } catch (MalformedURLException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PostAppHistory extends AsyncTask<Cursor, Void, Cursor> {
        private URL mUrl;
        public PostAppHistory(URL url) {
            mUrl = url;
        }

        private byte[] getAppHistoryJSONData(Cursor cursor) {
            Log.d("trans", "make json items["+cursor.getCount()+"]");
            JSONArray jsonArray = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                try {
                    object.put("uuid", mUUID);
                    object.put(DataBaseHelper.APP_NAME, cursor.getString(cursor.getColumnIndex(DataBaseHelper.APP_NAME)));
                    object.put(DataBaseHelper.PACKAGE_NAME, cursor.getString(cursor.getColumnIndex(DataBaseHelper.PACKAGE_NAME)));
                    object.put(DataBaseHelper.START_DATE, cursor.getString(cursor.getColumnIndex(DataBaseHelper.START_DATE)));
                    object.put(DataBaseHelper.START_TIME, cursor.getString(cursor.getColumnIndex(DataBaseHelper.START_TIME)));
                    object.put(DataBaseHelper.USE_TIME, cursor.getString(cursor.getColumnIndex(DataBaseHelper.USE_TIME)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            return jsonArray.toString().getBytes();
        }

        @Override
        protected Cursor doInBackground(Cursor... params) {
            try{
                HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
//                conn.setConnectTimeout(CONN_TIMEOUT * 1000);
//                conn.setReadTimeout(READ_TIMEOUT * 1000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                Log.d("trans", "-------------send data");
                os.write(getAppHistoryJSONData(params[0]));
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
                Log.d("trans", "-------server response------");
                Log.d("trans", response.toString());
//                Intent i = new Intent(mContext, WebViewActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//                i.putExtra("DATA", response.toString());
//                mContext.startActivity(i);

                if (HttpURLConnection.HTTP_CREATED == conn.getResponseCode()) {
                    return params[0];
                }

            }catch (IOException e) {
                e.printStackTrace();
            }

            params[0].close();
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null) {
                Log.d("trans", "success send app histories : " + cursor.getCount());
                while (cursor.moveToPrevious()) {
                    mdb.updateSendFlag(cursor.getString(0));
                }
                cursor.close();
            }
        }
    }
}
