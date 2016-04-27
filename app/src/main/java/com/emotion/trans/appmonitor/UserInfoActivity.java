package com.emotion.trans.appmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

public class UserInfoActivity extends AppCompatActivity {

    private EditText mNameText, mPhoneText;
    private CheckBox mExpCode;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mConfig = new Config(this);

        mPhoneText = (EditText)findViewById(R.id.phoneText);
        mNameText = (EditText)findViewById(R.id.nameText);
        mExpCode = (CheckBox)findViewById(R.id.total_test);
        String name = mConfig.getUserName();
        if (!name.isEmpty())
            mNameText.setText(name);

        Button button = (Button)findViewById(R.id.ok_button);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = mNameText.getText().toString();
                String phone = mPhoneText.getText().toString();
                if (!name.isEmpty() && !phone.isEmpty()) {
                    mConfig.setUserName(name);
                    mConfig.setPhoneNumber(phone);
                    mConfig.saveUUID(getUUID());
                    mConfig.setExpCode(mExpCode.isChecked()?"0":"1");
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    private String getUUID() {
        UUID deviceUuid = new UUID(mConfig.getUserName().hashCode(), ((long)mConfig.getPhoneNumber().hashCode() << 32) | new Date().hashCode());
        return deviceUuid.toString();
    }


}
