package com.emotion.trans.appmonitor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;

/**
 * Created by trans on 2016-02-03.
 */
public class StatCursorTreeAdapter extends CursorTreeAdapter{

    LayoutInflater mLayoutInflater;
    public StatCursorTreeAdapter(Cursor cursor, Context context) {
        super(cursor, context);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return null;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        return null;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

    }
}
