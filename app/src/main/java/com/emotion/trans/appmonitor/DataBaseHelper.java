package com.emotion.trans.appmonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by trans on 2016-02-02.
 */
public class DataBaseHelper{

    private Context mContext;
    private DataBaseOpenHelper mOpenHelper;

    public DataBaseHelper(Context context) {
        mContext = context;
    }

    public void open() {
        mOpenHelper = new DataBaseOpenHelper(mContext, "appstatis.db", null, 1);
    }

    private class DataBaseOpenHelper extends SQLiteOpenHelper{

        public DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table app_statis ("
                    +"app_name text not null , "
                    +"start_time text not null , "
                    +"use_time integer , "
                    +"is_send integer );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists app_statis");
            onCreate(db);
        }
    }

}
