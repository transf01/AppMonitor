package com.emotion.trans.appmonitor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.TextView;

/**
 * Created by trans on 2016-02-03.
 */
public class StatCursorTreeAdapter extends CursorTreeAdapter{

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private DataBaseHelper mDB;

    public StatCursorTreeAdapter(Cursor cursor, Context context, DataBaseHelper db) {
        super(cursor, context);
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mDB = db;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {

        String date = groupCursor.getString(groupCursor.getColumnIndex(DataBaseHelper.START_DATE));
        return mDB.getAppsByDate(date);
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return mLayoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, null, false);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView textView = (TextView)view.findViewById(android.R.id.text1);
        textView.setText(cursor.getString(cursor.getColumnIndex(DataBaseHelper.START_DATE)));
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        return mLayoutInflater.inflate(android.R.layout.simple_expandable_list_item_2, null, false);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        TextView textView1 = (TextView)view.findViewById(android.R.id.text1);
        textView1.setText(cursor.getString(1));
        TextView textView2 = (TextView)view.findViewById(android.R.id.text2);
        long sum = cursor.getLong(2);
        String time;
        if (sum > 3600) {
            time = String.format("%1$d시간 %2$d분 %3$d초", sum / 3600, (sum%3600)/60, sum % 60);
        }else if (sum > 60) {
            time = String.format("%1$d분 %2$d초", sum / 60, sum % 60);
        } else {
            time = String.format("%1$d초", sum);
        }
        textView2.setText(time);


    }
}
