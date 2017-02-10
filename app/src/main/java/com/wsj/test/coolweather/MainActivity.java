package com.wsj.test.coolweather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wsj.test.coolweather.util.LogUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.i(TAG,"程序启动");
        ;
    }
}
