package com.perception.trans.appmonitor;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class GoalActivity extends AppCompatActivity {
    private SeekBar mConfidence, mImportance;
    private EditText mAnswer;
    private DataBaseHelper mDB;
    private final static String ZERO_TIME="0시간";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);
        mDB = new DataBaseHelper(this);
        mDB.open();
        mAnswer = (EditText)findViewById(R.id.goalAnswer);
        initConfidenceSeekBar();
        initImportanceSeekBar();
        initAnswerButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFeedback();
        restore();
    }

    private String getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return Config.DATE_FORMAT.format(cal.getTime());
    }

    private void addTimeString(StringBuffer buffer, long data, String unit) {
        if (data > 0) {
            buffer.append(data);
            buffer.append(unit);
            buffer.append(" ");
        }
    }

    private String getFormattedTimeString(long sec) {
        if (sec == 0)
            return ZERO_TIME;

        StringBuffer buffer = new StringBuffer();
        addTimeString(buffer, sec/3600, "시간");
        addTimeString(buffer, (sec%3600)/60, "분");
        addTimeString(buffer, sec % 60, "초");
        return buffer.toString().trim();
    }

    private String getYesterdayUsage() {
        String returnValue = ZERO_TIME;
        Cursor cursor = mDB.getTotalUseTimeByDate(getYesterday());
        if (cursor.moveToLast()) {
            returnValue = getFormattedTimeString(cursor.getLong(0));
        }
        cursor.close();

        return returnValue;
    }

    private void setFeedback() {
        Cursor cursor = mDB.getGoalByDate(getYesterday());
        StringBuffer feedback = new StringBuffer(getResources().getString(R.string.yesterday));
        if (cursor.moveToLast()) {
            feedback.append(String.valueOf(getFormattedTimeString(cursor.getInt(4)*60)));
            feedback.append(getResources().getString(R.string.set_goal));
            feedback.append(getYesterdayUsage());
            feedback.append(getResources().getString(R.string.real_usage));
        } else {
            feedback.append(getResources().getString(R.string.did_not_set));

        }
        cursor.close();
        ((TextView)findViewById(R.id.feedbackText)).setText(feedback.toString());
    }

    private void restore() {
        Cursor cursor = mDB.getGoalByDate(Config.DATE_FORMAT.format(Calendar.getInstance().getTime()));
        if (cursor.moveToLast()) {
            mConfidence.setProgress(cursor.getInt(2));
            mImportance.setProgress(cursor.getInt(3));
            mAnswer.setText(String.valueOf(cursor.getInt(4)/60.0));
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDB.close();
    }

    private void initConfidenceSeekBar() {
        mConfidence = (SeekBar)findViewById(R.id.confidenceSeekBar);
        mConfidence.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)findViewById(R.id.confidenceValue)).setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initImportanceSeekBar() {
        mImportance = (SeekBar)findViewById(R.id.importantSeekBar);
        mImportance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)findViewById(R.id.importantValue)).setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initAnswerButton() {
        ((Button)findViewById(R.id.goal_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = mAnswer.getText().toString();
                if (answer == null || answer.isEmpty()) {
                    return;
                }

                mDB.addTodayGoal(Config.DATE_FORMAT.format(Calendar.getInstance().getTime())
                        , mConfidence.getProgress()
                        , mImportance.getProgress()
                        , (int)(Float.valueOf(answer)*60));
                GoalActivity.this.finish();
            }
        });
    }
}
