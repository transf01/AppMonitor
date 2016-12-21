package com.perception.trans.appmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.net.MalformedURLException;
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
        mConfig.saveExpStartDateIfNeed();
        //mConfig.testSetExpStartDate("2016-12-13");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("trans", "onStart");

        StatCursorTreeAdapter adapter = new StatCursorTreeAdapter(mDB.getDates(), this, mDB);
        ExpandableListView list = (ExpandableListView)findViewById(R.id.stat_list);
        list.setAdapter(adapter);

    }

    private void checkNotiAlarm(Config config, DataBaseHelper db) {
        GoalSettingAlarm alarm = GoalSettingAlarm.getInstance();
        Log.d("trans", "-------------checkNotiAlarm");
        if (!config.isValidNotiAlarmPeriod()) {
            return;
        }

        if (alarm.isNeedSettingTime(this)) {
            startGoalSettingAlarmActivity();
        } else if (config.isNeedSetTodayGoal(alarm, db)) {
            startTodayGoal();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("trans", "onResume");
        if (mConfig.isNeedUserInfo()) {
            startActivity(new Intent(this, UserInfoActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        } else if (!mConfig.isCompletePreSurvey()){
            startPreSurvey();
        }  else if (!mConfig.isAccessibilityEnabled()) {
            startAccessibilityConfirmDialog();
        } else {
            checkNotiAlarm(mConfig, mDB);
        }
    }

    private void startPreSurvey() {
        try {
            startActivity(new Intent(this, WebViewActivity.class)
                    .setAction(WebViewActivity.LOAD_URL).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .putExtra("DATA", mConfig.getPreSurveyURLString()));
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void startUsagePattern() {
        try {
            startActivity(new Intent(this, WebViewActivity.class)
                    .setAction(WebViewActivity.LOAD_URL).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .putExtra("DATA", mConfig.getUsagePatternURLString()));
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("trans", "onNewIntent");
        setIntent(intent);
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
            case R.id.goal_setting_alarm:
                startGoalSettingAlarmActivity();
                return true;
            case R.id.today_goal:
                startTodayGoal();
                return true;
            case R.id.usage_pattern:
                startUsagePattern();
                return true;
            case R.id.information:
                activieInformation();
                 return true;
            case R.id.presurvey:
                presurvey();
                return true;
            case R.id.postsurvey:
                postsurvey();
                return true;
        }
        return false;
    }

    private void displayAlreadyAnswer() {
        Toast.makeText(this, R.string.already_answer, Toast.LENGTH_SHORT).show();
    }

    private void presurvey() {
        if (mConfig.isCompletePreSurvey()) {
            displayAlreadyAnswer();
            return;
        }

        mConfig.startPresurvey();
    }

    private void postsurvey() {
        if (!mConfig.isExpExpired()) {
            Toast.makeText(this, R.string.leave_expire, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mConfig.isCompletePostsurvey()) {
            displayAlreadyAnswer();
            return;
        }

        mConfig.startPostsurvey();
    }

    private void activieInformation() {
        mConfig.startInformation();
    }

    private void clear(DataBaseHelper db) {
        db.clear();
    }

    private void testAdd(DataBaseHelper db) {
        long millis = new GregorianCalendar(2016, Calendar.JUNE, 22).getTimeInMillis();

        for (int i = 0; i < 1000; i++) {
            db.addData(String.format("test-%1$d", i), "test", new AppRuntimeInfo(new Date(millis+i*1000), i));
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


    private void startAccessibilityConfirmDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        String alertMessage = String.format(getResources().getString(R.string.accessibility_alert),
                getResources().getString(R.string.app_name), getResources().getString(R.string.confirm));
        alertBuilder.setMessage(alertMessage).setCancelable(false);
        alertBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveAccessibilitySetting();
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle(R.string.warning);
        alert.show();
    }

    private void startGoalSettingAlarmActivity() {
        if (mConfig.isValidNotiAlarmPeriod()) {
            startActivity(new Intent(this, GoalSettingAlarmActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        } else {
            displayNOTAlarmPeriod();
        }
    }


    private void startTodayGoal() {
        if (mConfig.isValidNotiAlarmPeriod()) {
            startActivity(new Intent(this, GoalActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        } else {
            displayNOTAlarmPeriod();
        }
    }

    private void displayNOTAlarmPeriod() {
        Toast.makeText(this, R.string.not_valid_alram_period, Toast.LENGTH_SHORT).show();
    }
}


