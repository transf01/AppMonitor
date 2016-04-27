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

    private final int MAX_SEND_ITEM_COUNT = 30;
    private final String TABLE_NAME = "app_stat";
    public static final String APP_NAME = "app_name";
    public static final String PACKAGE_NAME = "package_name";
    public static final String START_DATE = "start_date";
    public static final String START_TIME = "start_time";
    public static final String USE_TIME = "use_time";
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

    public long addData(String appName, String packageName, RuntimeInfo runtimeInfo) {
        ContentValues values = new ContentValues();
        values.put(APP_NAME, appName);
        values.put(PACKAGE_NAME, packageName);
        values.put(START_DATE, runtimeInfo.getStartDateString());
        values.put(START_TIME, runtimeInfo.getStartTimeString());
        values.put(USE_TIME, runtimeInfo.getRunTime());
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

    public void updateSendFlag(String rowid, int flag) {
        ContentValues value = new ContentValues();
        value.put(IS_SEND, flag);
        mdb.update(TABLE_NAME, value, "rowid=?", new String[]{rowid});
    }

    public Cursor getAppsByDate(String date) {
        String query = String.format("select rowid, %1$s, sum(%2$s) from %3$s where %4$s=\"%5$s\" group by %1$s order by 3 desc",
                APP_NAME, USE_TIME, TABLE_NAME, START_DATE, date);
        return mdb.rawQuery(query, new String[]{});
    }

    public Cursor getSendData() {
        String query = String.format("select rowid, * from %1$s where is_send = 0  ORDER BY %2$s, %3$s ASC limit %4$d",
                TABLE_NAME, START_DATE, START_TIME, MAX_SEND_ITEM_COUNT);
        return mdb.rawQuery(query, new String[]{});
    }

    public Cursor getAmbiguousSendData() {
        String query = String.format("select rowid, * from %1$s where is_send = 1  ORDER BY %2$s, %3$s ASC limit 1",
                TABLE_NAME, START_DATE, START_TIME);
        return mdb.rawQuery(query, new String[]{});
    }

    public Cursor getSendDataByRowID(long rowId) {
        String query = String.format("select rowid, * from %1$s where rowid = %2$d", TABLE_NAME, rowId);
        return mdb.rawQuery(query, new String[]{});
    }

    public void clear() {
        mdb.delete(TABLE_NAME, null, null);
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
