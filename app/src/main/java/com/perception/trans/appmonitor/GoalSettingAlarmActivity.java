package com.perception.trans.appmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class GoalSettingAlarmActivity extends AppCompatActivity {
    TimePicker mTimePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_setting_alarm);
        mTimePicker = (TimePicker)findViewById(R.id.timePicker);

        Pair<Integer, Integer> savedTime = GoalSettingAlarm.getInstance().getTime(this);
        Log.d("trans", "---------------------- " + savedTime.first + ":" + savedTime.second);
        if (savedTime.first >=0 && savedTime.second >= 0) {
            mTimePicker.setCurrentHour(savedTime.first);
            mTimePicker.setCurrentMinute(savedTime.second);
        }


        Button button = (Button)findViewById(R.id.goal_setting_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoalSettingAlarm.getInstance().setTime(GoalSettingAlarmActivity.this, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                GoalSettingAlarmActivity.this.finish();
            }
        });
    }
}
