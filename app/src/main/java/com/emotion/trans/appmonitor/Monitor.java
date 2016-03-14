package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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

    @Override
    public void handleAppStop(Date endTime) {
        if (mInfo != null) {
            mInfo.save(endTime);
            mInfo = null;
            if(isWiFiConnected()) {
                Intent i = new Intent(MonitoringService.SEND_DATA);
                i.setPackage(mContext.getPackageName());
                mContext.startService(i);
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

        private Handler mHandler = new Handler();

        private JSONArray getJSONData() {
            Cursor cursor = mdb.getSendData();
            String[] columns = cursor.getColumnNames();

            JSONArray jsonArray = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                try {
                    object.put("uuid", mUUID);
                    for (int i = 0; i < columns.length; i++) {
                        object.put(columns[i], cursor.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            cursor.close();
            return jsonArray;
        }

        @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            Log.d("trans", getJSONData().toString());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = getJSONData();

                }
            });

        }

        @Override
        public void addChangeListener(AppChangeListener listener) {

        }
    }

    private class PostJSON {
        private URL mUrl;
        public PostJSON(URL url) {
            mUrl = url;
        }

        public void sendData(JSONArray data) {
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
                os.write(data.toString().getBytes());
                os.flush();

                String response;

                int responseCode = conn.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {

                }
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
