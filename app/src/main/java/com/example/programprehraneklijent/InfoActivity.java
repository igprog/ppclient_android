package com.example.programprehraneklijent;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        TextView textView = (TextView)findViewById(R.id.text_view);

        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            textView.setText(getIntent().getStringExtra("string"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        WebView myWebView =  findViewById(R.id.wvHomePage);
        myWebView.loadUrl("https://www.programprehrane.com/aplikacija-za-klijente.html");


        loadPreferences();

    }
    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains("color")) {
            int backgroundColor = Color.WHITE;

            String color = prefs.getString("color", "1");
            switch (color) {
                case "0":
                    backgroundColor = getColor(R.color.color0);
                    break;
                case "1":
                    backgroundColor = getColor(R.color.color1);
                    break;
                case "2":
                    backgroundColor = getColor(R.color.color2);
                    break;
                case "3":
                    backgroundColor = getColor(R.color.color3);
                    break;
            }
            getWindow().getDecorView().setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
