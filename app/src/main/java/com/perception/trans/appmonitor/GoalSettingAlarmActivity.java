package com.perception.trans.appmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class GoalSettingAlarmActivity extends AppCompatActivity {
    private TimePicker mTimePicker;
    private GoalSettingAlarm mGoalSettingAlarm;
    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_setting_alarm);
        mTimePicker = (TimePicker)findViewById(R.id.timePicker);
        mGoalSettingAlarm = GoalSettingAlarm.getInstance();

        if (mGoalSettingAlarm.isValid(this)) {
            Pair<Integer, Integer> savedTime = mGoalSettingAlarm.getTime(this);
            Log.d("trans", "---------------------- " + savedTime.first + ":" + savedTime.second);
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
