package com.emotion.trans.appmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private final String KEY_ACCESSIBILITY_WARNING = "accessibility_warning";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewAccessibilityConfirmDialog();
    }


    private void viewAccessibilityConfirmDialog() {
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
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, 0);
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle(R.string.warning);
        alert.show();
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_ACCESSIBILITY_WARNING, true);
        editor.commit();
    }
}
