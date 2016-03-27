package com.slightech.mynt.sdk.demo.util;

import android.util.Log;

import com.slightech.mynt.sdk.demo.BuildConfig;

public class LogUtils {

    public static final boolean VISIBLE = BuildConfig.DEBUG;

    public static void v(String tag, String msg, Object... args) {
        println(Log.VERBOSE, tag, msg, args);
    }

    public static void d(String tag, String msg, Object... args) {
        println(Log.DEBUG, tag, msg, args);
    }

    public static void i(String tag, String msg, Object... args) {
        println(Log.INFO, tag, msg, args);
    }

    public static void w(String tag, String msg, Object... args) {
        println(Log.WARN, tag, msg, args);
    }

    public static void e(String tag, String msg, Object... args) {
        println(Log.ERROR, tag, msg, args);
    }

    public static void println(int priority, String tag, String msg, Object... args) {
        if (VISIBLE) Log.println(priority, tag, String.format(msg, args));
    }

}
