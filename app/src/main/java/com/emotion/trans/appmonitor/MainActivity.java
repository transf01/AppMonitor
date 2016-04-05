package com.emotion.trans.appmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private DataBaseHelper mDB;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDB = new DataBaseHelper(this);
        mDB.open();
        mConfig = new Config(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        StatCursorTreeAdapter adapter = new StatCursorTreeAdapter(mDB.getDates(), this, mDB);
        ExpandableListView list = (ExpandableListView)findViewById(R.id.stat_list);
        list.setAdapter(adapter);
        if (isNeedUserInfo()) {
            Intent i = new Intent(this, UserInfoActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(i, Config.REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Config.REQUEST_CODE) {
            activeAccessibilityConfirmDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDB.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.accessibility_setting:
                moveAccessibilitySetting();
                return true;
            case R.id.information:
                activeInformationDialog();
                 return true;
        }
        return false;
    }

    private boolean isNeedUserInfo() {
        return mConfig.getUserName().isEmpty() || mConfig.getPhoneNumber().isEmpty();
    }

    private void clear(DataBaseHelper db) {
        db.clear();
    }

    private void testAdd(DataBaseHelper db) {
        long millis = new GregorianCalendar(2015, Calendar.FEBRUARY, 17).getTimeInMillis();

        for (int i = 0; i < 1000; i++) {
            db.addData(String.format("test-%1$d", i), "test", new Date(millis+i*1000), i);
        }
    }

    private void debugAppListPrint(DataBaseHelper db) {
        Cursor cursor = db.getAppsByDate("20160204");
        while (cursor.moveToNext()) {
            Log.d("trans", "app : " + cursor.getString(1) + " sum : " + cursor.getInt(2));
        }
        cursor.close();
    }

    private void debugDatesPrint(DataBaseHelper db) {
        Cursor cursor = db.getDates();
        while (cursor.moveToNext()) {
            Log.d("trans", "date : " + cursor.getString(cursor.getColumnIndex("start_date")));
        }
        cursor.close();
    }

    private void debugAllPrint(DataBaseHelper db) {
        Cursor cursor = db.getAllColumns();
        while (cursor.moveToNext()) {
            Log.d("trans", "id:" + cursor.getString(cursor.getColumnIndex("_id"))
                            +" app:" + cursor.getString(cursor.getColumnIndex("app_name"))
                            +" package:" + cursor.getString(cursor.getColumnIndex("package_name"))
                            +" start_date:"+cursor.getString(cursor.getColumnIndex("start_date"))
                            +" start_time:"+cursor.getString(cursor.getColumnIndex("start_time"))
                            +" time:" + cursor.getInt(cursor.getColumnIndex("use_time"))
                            +" isSend:" + cursor.getInt(cursor.getColumnIndex("is_send"))
            );
        }
        cursor.close();
    }

    private void moveAccessibilitySetting(){
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 0);
    }


    private void activeAccessibilityConfirmDialog() {
        if (mConfig.isPassAccessbilityWarning())
            return;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        String alertMessage = String.format(getResources().getString(R.string.accessibility_alert),
                getResources().getString(R.string.app_name), getResources().getString(R.string.confirm));
        alertBuilder.setMessage(alertMessage).setCancelable(false);
        alertBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveAccessibilitySetting();
                        mConfig.passAccessbilityWarning();
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle(R.string.warning);
        alert.show();
    }

    private void activeInformationDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
        alert.setMessage(R.string.app_information);
        AlertDialog dialog = alert.show();
        TextView view = (TextView)dialog.findViewById(android.R.id.message);
        view.setGravity(Gravity.CENTER);
        dialog.show();
    }

}


