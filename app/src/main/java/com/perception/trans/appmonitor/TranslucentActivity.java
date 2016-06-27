package com.perception.trans.appmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class TranslucentActivity extends Activity {

    private Dialog mDialog;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new Config(this);
        mDialog = createDialog();

    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.request_postsurvey)
                .setCancelable(false)
                .setTitle(R.string.expired_exp)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mConfig.startPostsurvey();
                        finish();
                    }
                });
        return builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialog.dismiss();
    }
}
