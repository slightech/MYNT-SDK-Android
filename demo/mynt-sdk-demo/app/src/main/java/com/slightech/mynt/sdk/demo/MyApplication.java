package com.slightech.mynt.sdk.demo;

import android.app.Application;
import android.os.StrictMode;

import com.slightech.mynt.api.MyntManager;

public class MyApplication extends Application {

    private static MyApplication mInstance;

    private MyntManager mMyntManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if (BuildConfig.DEBUG) {
            setupStrictMode();
        }
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public static MyntManager getMyntManager() {
        final MyApplication app = getInstance();
        if (app.mMyntManager == null) {
            app.mMyntManager = new MyntManager(app);
        }
        return app.mMyntManager;
    }

    private void setupStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

}
