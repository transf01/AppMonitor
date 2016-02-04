package com.emotion.trans.appmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by trans on 2016-02-02.
 */
public class DataBaseHelper{

    private final String TABLE_NAME = "app_stat";
    private final String APP_NAME = "app_name";
    private final String PACKAGE_NAME = "package_name";
    public static final String START_DATE = "start_date";
    private final String START_TIME = "start_time";
    private final String USE_TIME = "use_time";
    private final String IS_SEND = "is_send";

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HHmmss");

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

    public long addData(String appName, String packageName, Date startTime, long useTime) {
        ContentValues values = new ContentValues();
        values.put(APP_NAME, appName);
        values.put(PACKAGE_NAME, packageName);
        values.put(START_DATE, mDateFormat.format(startTime));
        values.put(START_TIME, mTimeFormat.format(startTime));
        values.put(USE_TIME, useTime);
        values.put(IS_SEND, 0);
        return mdb.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllColumns(){
        return mdb.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor getDates() {
        String query = String.format("select rowid, %1$s from %2$s group by %1$s", START_DATE, TABLE_NAME);
        return mdb.rawQuery(query, new String[]{});
    }

    public Cursor getAppsByDate(String date) {
        String query = String.format("select rowid, %1$s, sum(%2$s) from %3$s where %4$s=\"%5$s\" group by %1$s order by 3 desc",
                APP_NAME, USE_TIME, TABLE_NAME, START_DATE, date);
        return mdb.rawQuery(query, new String[]{});
    }

    private class DataBaseOpenHelper extends SQLiteOpenHelper{

        public DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("create table %1$s " +
                            "(_id integer primary key autoincrement, " +
                            "%2$s text not null, " +
                            "%3$s text not null, " +
                            "%4$s text not null, " +
                            "%5$s text not null, " +
                            "%6$s integer, " +
                            "%7$s integer);",
                    TABLE_NAME,APP_NAME, PACKAGE_NAME, START_DATE, START_TIME, USE_TIME, IS_SEND);
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
