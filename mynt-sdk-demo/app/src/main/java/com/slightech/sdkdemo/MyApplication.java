package com.slightech.sdkdemo;

import android.app.Application;

/**
 * My application.
 */
public class MyApplication extends Application {


    //-----------------------------------------------------------------------

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
    }
}
