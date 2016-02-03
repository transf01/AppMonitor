package com.emotion.trans.appmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public class DataBaseHelper{

    private final String TABLE_NAME = "app_stat";
    private final String APP_NAME = "app_name";
    private final String START_TIME = "start_time";
    private final String USE_TIME = "use_time";
    private final String IS_SEND = "is_send";

    private Context mContext;
    private DataBaseOpenHelper mOpenHelper;
    private SQLiteDatabase mdb;

    public DataBaseHelper(Context context) {
        mContext = context;
    }

    public void open() {
        mOpenHelper = new DataBaseOpenHelper(mContext, "appstat.db", null, 1);
        mdb = mOpenHelper.getWritableDatabase();
    }

    public void close() {
        mdb.close();
    }

    public long addData(String appName, Date startTime, long useTime) {
        ContentValues values = new ContentValues();
        values.put(APP_NAME, appName);
        values.put(START_TIME, startTime.toString());
        values.put(USE_TIME, useTime);
        values.put(IS_SEND, 0);
        return mdb.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllColumns(){
        return mdb.query(TABLE_NAME, null, null, null, null, null, null);
    }

    private class DataBaseOpenHelper extends SQLiteOpenHelper{

        public DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("create table %1$s (%2$s text not null, %3$s text not null, %4$s integer, %5$s integer);",
                    TABLE_NAME,APP_NAME, START_TIME, USE_TIME, IS_SEND);
            Log.d("trans", sql);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists" + TABLE_NAME);
            onCreate(db);
        }
    }

}
