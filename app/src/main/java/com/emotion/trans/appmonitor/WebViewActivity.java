package com.emotion.trans.appmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    public static final String LOAD_DATA = "LOAD_DATA";
    public static final String LOAD_URL = "LOAD_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WebView web = (WebView)findViewById(R.id.webView);
        Log.d("trans", "test***************");
        Intent i = getIntent();
        if (LOAD_DATA.equals(i.getAction())) {
            web.loadData(i.getStringExtra("DATA"), "text/html", null);
        } else if (LOAD_URL.equals(i.getAction())) {
            web.setWebViewClient(new WebViewClient());
            WebSettings set = web.getSettings();
            set.setJavaScriptEnabled(true);
            web.loadUrl(i.getStringExtra("DATA"));
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
