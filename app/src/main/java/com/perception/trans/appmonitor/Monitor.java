package com.perception.trans.appmonitor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import java.util.HashMap;

/**
 * Created by trans on 2016-02-02.
 */
public class Monitor{

    private static final int NONE = 0;
    private static final int SENDING = 1;
    private static final int SENT = 2;

    private Context mContext;
    private Handler mHandler;
    private Runnable mMonitoringRunnable;
    private HashMap<String, CommandHandler> mCommandHandlerMap = new HashMap<>();
    private AppInfo mCurrentAppInfo;
    private RuntimeInfo mRuntimeInfo;
    private MonitorInfo mInfo = null;
    private DataBaseHelper mdb;
    private Config mConfig;

    public Monitor(Context context, DataBaseHelper db) {
        mContext = context;
        mHandler = new Handler();
        mMonitoringRunnable = new Runnable() {
            @Override
            public void run() {
                startMonitoring();
            }
        };
        mdb = db;
        mConfig = new Config(context);
        initCommandHandlerMap();
    }

    private void addCommandHandler(String key, CommandHandler handler) {
        mCommandHandlerMap.put(key, handler);
    }

    private void initCommandHandlerMap() {
        addCommandHandler(MonitoringService.START_MONITORING, new WindowChangeCommandHandler());
        addCommandHandler("screenOn", new ScreenOnCommandHandler());
        addCommandHandler("screenOff", new ScreenOffCommandHandler());
        addCommandHandler(MonitoringService.SEND_DATA, new SendHandler());
    }

    private void startMonitoring(){
        if (mCurrentAppInfo != null && mCurrentAppInfo.isCheckable()) {
            mInfo = new MonitorInfo(mCurrentAppInfo, mdb);
            Log.d("trans", "### start : " + mInfo.toString());
        }
    }

    private void handleChangedAppInfo(AppInfo appInfo) {
        mCurrentAppInfo = appInfo;
        if (mConfig.isExpExpired() && mCurrentAppInfo.isHomeApp()) {
            Log.d("trans", "----------------expired at home---------------");
            if (!mConfig.isCompletePostsurvey()) {
                mContext.startActivity(new Intent(mContext, TranslucentActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        }
    }

    private void handleChangedAppStartTime() {
        mRuntimeInfo = new RuntimeInfo();
    }

    private void updateInfo(RestGetTaskPostHandler handler) {
        RestGet hostInfo = new RestGet(new RestGetHandler() {
            @Override
            public URL getURL() throws MalformedURLException {
                return mConfig.getHostInfoURL();
            }

            @Override
            public void handleResponse(String response) {
                Log.d("trans", response);
                mConfig.setURLInfo(response);
            }
        });

        RestGet excludedPackageInfo = new RestGet(new RestGetHandler() {
            @Override
            public URL getURL() throws MalformedURLException {
                return mConfig.getExcludedPackageURL();
            }

            @Override
            public void handleResponse(String response) {
                Log.d("trans", response);
                mConfig.setExcludedPackage(response);
            }
        });

        RestGet expInfo = new RestGet(new RestGetHandler() {
            @Override
            public URL getURL() throws MalformedURLException {
                return mConfig.getExpInfoURL();
            }

            @Override
            public void handleResponse(String response) {
                Log.d("trans", response);
                mConfig.setExpInfo(response);
            }
        });

        RestGetTask restGetTask = new RestGetTask(handler);
        restGetTask.execute(hostInfo, excludedPackageInfo, expInfo);
    }

    private void sendSavedHistory() {
        if (mConfig.isWiFiConnected()) {
            updateInfo(new RestGetTaskPostHandler() {
                @Override
                public void handlePostProcess() {
                    mContext.startService(new Intent(mContext, MonitoringService.class).setAction(MonitoringService.SEND_DATA));
                }
            });
        }
    }

    private void handleAppStop() {
        if (mConfig.isExpExpired()) {
            return;
        }

        if (mInfo != null) {
            mInfo.save(mRuntimeInfo.stop());
            mInfo = null;
            sendSavedHistory();
        }
    }

    public void handleCommand(Intent intent) {
        if (intent == null)   return;
        String action = intent.getAction();

        CommandHandler handler = mCommandHandlerMap.get(action);
        if (handler != null) {
            handler.handle(intent, mHandler, mMonitoringRunnable);
        }
    }

    public void clearHandler() {
        mHandler.removeCallbacks(mMonitoringRunnable);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOffCommandHandler implements CommandHandler{

        @Override
        public void handle(Intent intent, Handler handler, Runnable startMonitoring) {
            handler.removeCallbacks(startMonitoring);
            handleAppStop();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreenOnCommandHandler implements CommandHandler {

        @Override
        public void handle(Intent intent, Handler handler, Runnable startMonitoring) {
            handler.removeCallbacks(startMonitoring);
            handler.postDelayed(startMonitoring, MONITORING_JUDGE_TIME);
            handleChangedAppStartTime();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class WindowChangeCommandHandler implements CommandHandler{
        AppInfo mPreviousAppInfo = null;

        public void handle(Intent intent, Handler handler, Runnable startMonitoring) {

            AppInfo appInfo = new AppInfo(intent.getStringExtra("AppName"), intent.getStringExtra("PackageName"), mConfig);

            if (appInfo.isDifferent(mPreviousAppInfo)) {
                handler.removeCallbacks(startMonitoring);
                handler.postDelayed(startMonitoring, MONITORING_JUDGE_TIME);
                handleAppStop();
                handleChangedAppStartTime();
                handleChangedAppInfo(appInfo);
                mPreviousAppInfo = appInfo;
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class SendHandler implements CommandHandler {

        private void handleAmbiguousHistory(){
            Cursor cursor = mdb.getAmbiguousSendData();
            if (cursor.getCount() > 0) {
                GetAppHistory get = new GetAppHistory();
                get.execute(cursor);
            }else {
                cursor.close();
            }
        }

        private void sendData() {
            if (!mConfig.isSendUserInfo()) {
                new UserInfo(mContext, mConfig).send();
            } else {
                Cursor cursor = mdb.getSendData();
                if (cursor.getCount() > 0) {
                    PostAppHistory post = new PostAppHistory();
                    post.execute(cursor);
                } else {
                    cursor.close();
                    handleAmbiguousHistory();
                }
            }
        }

        @Override
        public void handle(Intent intent, Handler handler, Runnable startMonitoring) {
            if(mConfig.isWiFiConnected()) {
                sendData();
            }
        }
    }

    private interface RestGetTaskPostHandler {
        public void handlePostProcess() ;
    }

   ///////////////////////////////////////////////////////////////////////////////////////////////

    private class RestGetTask extends AsyncTask<RestGet, Void, Void> {

        private RestGetTaskPostHandler mPostHandler;

        public RestGetTask(RestGetTaskPostHandler postHandler) {
            mPostHandler = postHandler;
        }

        @Override
        protected Void doInBackground(RestGet... params) {
            for (int i = 0; i < params.length; i++) {
                params[i].handle();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mPostHandler != null) {
                mPostHandler.handlePostProcess();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class GetAppHistory extends AsyncTask<Cursor, Void, Void> {

        private URL getURL(Cursor cursor) throws  MalformedURLException{
            cursor.moveToNext();
            return mConfig.getUserHistoryURL(cursor.getString(cursor.getColumnIndex(DataBaseHelper.START_DATE)),
                    cursor.getString(cursor.getColumnIndex(DataBaseHelper.START_TIME)));
        }

        private HttpURLConnection getConnection(URL url) throws  IOException{
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            return conn;
        }

        private void checkResponse(HttpURLConnection conn, Cursor cursor) throws IOException{
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer response = new StringBuffer();
                String line;
                while ((line=reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                try {
                    JSONArray array = new JSONArray(response.toString());
                    if (array.length() > 0) {
                        Log.d("trans", "[update] this history can be checked from server. update to sent state");
                        mdb.updateSendFlag(cursor.getString(0), SENT);
                        return;
                    }
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("trans", "[update] this history update to init state");
            mdb.updateSendFlag(cursor.getString(0), NONE);
            return;
         }

        @Override
        protected Void doInBackground(Cursor... params) {
            HttpURLConnection connection = null;
            try {
                connection = getConnection(getURL(params[0]));
                checkResponse(connection, params[0]);
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            params[0].close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mContext.startService(new Intent(mContext, MonitoringService.class).setAction(MonitoringService.SEND_DATA));
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PostAppHistory extends AsyncTask<Cursor, Void, SendData> {

        private HttpURLConnection getConnection(URL url) throws  IOException{
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            return conn;
        }

        private void request(HttpURLConnection conn, SendData data) throws IOException{
            OutputStream os = conn.getOutputStream();
            os.write(data.getAppHistoryJSONData());
            os.flush();
        }

        private StringBuffer checkResponse(HttpURLConnection conn, SendData data) throws IOException{
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

            if (HttpURLConnection.HTTP_CREATED == conn.getResponseCode()) {
                data.setStatus(SENT);
            }

            return response;
        }

        private void debugResponse(StringBuffer response) {
            Log.d("trans", "-------server response------");
            Log.d("trans", response.toString());
//            Intent i = new Intent(mContext, WebViewActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            i.putExtra("DATA", response.toString());
//            mContext.startActivity(i);
        }

        @Override
        protected SendData doInBackground(Cursor... params) {
            SendData data = new SendData(params[0]);
            HttpURLConnection conn = null;
            try{
                conn = getConnection(mConfig.getHistoryURL());
                request(conn,data);
                StringBuffer response = checkResponse(conn, data);
                debugResponse(response);
            }catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(SendData sendData) {
            super.onPostExecute(sendData);
            if (sendData != null) {
                sendData.updateFlag();
                if (sendData.isSent())
                    sendRemainData(mContext);
            }
        }
        private void sendRemainData(Context context) {
            context.startService(new Intent(context, MonitoringService.class).setAction(MonitoringService.SEND_DATA));
        }
    }

    private class SendData {
        int mSendStatus = NONE;

        Cursor mCursor;
        public SendData(Cursor cursor) {
            mCursor = cursor;
        }

        private void setSendState(Cursor cursor, int state) {
            mdb.updateSendFlag(cursor.getString(0), state);
        }

        public byte[] getAppHistoryJSONData() {
            JSONArray jsonArray = new JSONArray();
            while (mCursor.moveToNext()) {
                setSendState(mCursor, SENDING);
                JSONObject object = new JSONObject();
                try {
                    object.put("uuid", mConfig.getUUID());
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
            Log.d("trans", "json[" + mCursor.getCount() + "]"+jsonArray.toString());
            return jsonArray.toString().getBytes();
        }

        public void setStatus(int status){
            mSendStatus = status;
        }

        public boolean isSent() {
            return mSendStatus == SENT;
        }

        public void updateFlag() {
            Log.d("trans", "sent histories[" + mCursor.getCount() + "] : " + mSendStatus);
            if (isSent()) {
                while (mCursor.moveToPrevious()) {
                    setSendState(mCursor, mSendStatus);
                }
            }
            mCursor.close();
        }
    }
}
