package com.perception.trans.appmonitor;

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

    private void addTimeString(StringBuffer buffer, long data, String unit) {
        if (data > 0) {
            buffer.append(data);
            buffer.append(unit);
            buffer.append(" ");
        }
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        TextView textView1 = (TextView)view.findViewById(android.R.id.text1);
        textView1.setText(cursor.getString(1));
        TextView textView2 = (TextView)view.findViewById(android.R.id.text2);
        long sum = cursor.getLong(2);

        StringBuffer buffer = new StringBuffer();
        addTimeString(buffer, sum/3600, "시간");
        addTimeString(buffer, (sum%3600)/60, "분");
        addTimeString(buffer, sum % 60, "초");

        textView2.setText(buffer.toString());


    }
}
