package com.perception.trans.appmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    public static final String LOAD_DATA = "LOAD_DATA";
    public static final String LOAD_URL = "LOAD_URL";

    private Handler mHandler = new Handler();
    private WebView mWebView;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);
        mConfig = new Config(this);

        mWebView = (WebView)findViewById(R.id.webView);
        Intent i = getIntent();
        if (LOAD_DATA.equals(i.getAction())) {
            mWebView.loadData(i.getStringExtra("DATA"), "text/html", null);
        } else if (LOAD_URL.equals(i.getAction())) {
            mWebView.setWebViewClient(new WebViewClient());
            WebSettings set = mWebView.getSettings();
            set.setJavaScriptEnabled(true);
            mWebView.loadUrl(i.getStringExtra("DATA"));
            mWebView.addJavascriptInterface(new AndroidBridge(), "android");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private class AndroidBridge {

        @JavascriptInterface
        public void confirmPresurvey() {
            mConfig.setCompletePresurvey();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("trans", "confirm pre");
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }

        @JavascriptInterface
        public void confirmPostsurvey() {
            mConfig.setCompletePostsurvey();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("trans", "confirm post");
                    finish();
                }
            });
        }

        @JavascriptInterface
        public void close() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

}
