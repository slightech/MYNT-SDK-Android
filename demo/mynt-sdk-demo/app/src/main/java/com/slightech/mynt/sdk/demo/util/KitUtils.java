package com.slightech.mynt.sdk.demo.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class KitUtils {

    @SuppressWarnings({ "unchecked", "UnusedDeclaration" })
    public static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }

    @SuppressWarnings({ "unchecked", "UnusedDeclaration" })
    public static <T extends View> T findById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    /**
     * Return the handle to a system-level service by name.
     *
     * @see Context#getSystemService(String)
     */
    @SuppressWarnings({ "unchecked", "UnusedDeclaration" })
    public static <T> T getService(Context context, String name) {
        return (T) context.getSystemService(name);
    }
}
