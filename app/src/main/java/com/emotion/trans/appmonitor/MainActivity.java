package com.emotion.trans.appmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String KEY_ACCESSIBILITY_WARNING = "accessibility_warning";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activeAccessibilityConfirmDialog();
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

    private void moveAccessibilitySetting(){
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 0);
    }


    private void activeAccessibilityConfirmDialog() {
        SharedPreferences pref = getSharedPreferences("Pref", MODE_PRIVATE);
        boolean isWarnAccessibility = pref.getBoolean(KEY_ACCESSIBILITY_WARNING, false);

        if (isWarnAccessibility)
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
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle(R.string.warning);
        alert.show();
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_ACCESSIBILITY_WARNING, true);
        editor.commit();
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


