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
        addCommandHandler(MonitoringService.SEND_DATA, new SendHandler());
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
    private class SendHandler implements CommandHandler {

         @Override
        public void handle(Intent intent, Handler handler, Runnable runnable) {
            try {
                Cursor cursor = mdb.getSendData();
                if (cursor.getCount() > 0) {
                    PostAppHistory post = new PostAppHistory(new URL(getURLString()));
                    post.execute(mdb.getSendData());
                }
            } catch (MalformedURLException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void addChangeListener(AppChangeListener listener) {

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PostAppHistory extends AsyncTask<Cursor, Void, SendData> {
        private URL mUrl;
        public PostAppHistory(URL url) {
            mUrl = url;
        }
        @Override
        protected SendData doInBackground(Cursor... params) {
            SendData data = new SendData(params[0]);
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
                os.write(data.getAppHistoryJSONData());
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
                    data.setResult(true);
                    return data;
                }

            }catch (IOException e) {
                e.printStackTrace();
            }
            data.setResult(false);
            return data;
        }

        @Override
        protected void onPostExecute(SendData data) {
            super.onPostExecute(data);
            if (data != null) {
                data.updateFlag();
                sendRemainData(mContext);
            }
        }

        private void sendRemainData(Context context) {
            Intent i = new Intent(mContext, MonitoringService.class);
            i.setAction(MonitoringService.SEND_DATA);
            mContext.startService(i);
        }
    }

    private class SendData {
        boolean mResult = false;
        Cursor mCursor;
        public SendData(Cursor cursor) {
            mCursor = cursor;
        }

        private void setSentState(Cursor cursor) {
            mdb.updateSendFlag(cursor.getString(0), 2);
        }

        private void setSendingState(Cursor cursor) {
            mdb.updateSendFlag(cursor.getString(0), 1);
        }

        private void setUnsendState(Cursor cursor) {
            mdb.updateSendFlag(cursor.getString(0), 0);
        }

        public byte[] getAppHistoryJSONData() {
            Log.d("trans", "make json items[" + mCursor.getCount() + "]");
            JSONArray jsonArray = new JSONArray();
            while (mCursor.moveToNext()) {
                setSendingState(mCursor);
                JSONObject object = new JSONObject();
                try {
                    object.put("uuid", mUUID);
                    object.put(DataBaseHelper.APP_NAME, mCursor.getString(mCursor.getColumnIndex(DataBaseHelper.APP_NAME)));
                    object.put(DataBaseHelper.PACKAGE_NAME, mCursor.getString(mCursor.getColumnIndex(DataBaseHelper.PACKAGE_NAME)));
                    object.put(DataBaseHelper.START_DATE, mCursor.getString(mCursor.getColumnIndex(DataBaseHelper.START_DATE)));
                    object.put(DataBaseHelper.START_TIME, mCursor.getString(mCursor.getColumnIndex(DataBaseHelper.START_TIME)));
                    object.put(DataBaseHelper.USE_TIME, mCursor.getString(mCursor.getColumnIndex(DataBaseHelper.USE_TIME)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            return jsonArray.toString().getBytes();
        }

        public void setResult(boolean result){
            mResult = result;
        }

        public void updateFlag() {
            Log.d("trans", "sent histories[" + mCursor.getCount() + "] : " + mResult);
            while (mCursor.moveToPrevious()) {
                if (mResult) {
                    setSentState(mCursor);
                }else {
                    setUnsendState(mCursor);
                }
            }
            mCursor.close();
        }
    }
}
