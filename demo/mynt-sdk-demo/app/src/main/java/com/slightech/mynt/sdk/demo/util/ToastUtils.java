package com.slightech.mynt.sdk.demo.util;

import android.content.Context;
import android.support.annotation.StringRes;

public class ToastUtils {

    public static void show(Context context, @StringRes int resId) {
        if (context == null) return;
        android.widget.Toast.makeText(context, resId, android.widget.Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, CharSequence text) {
        if (context == null) return;
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, @StringRes int resId) {
        if (context == null) return;
        android.widget.Toast.makeText(context, resId, android.widget.Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context context, CharSequence text) {
        if (context == null) return;
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG).show();
    }
}
