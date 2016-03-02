package com.slightech.sdkdemo.manager;

import com.slightech.ble.mynt.MyntManager;
import com.slightech.sdkdemo.MyApplication;

/**
 * Created by Willard  on 2015/9/22.
 */
public class MyMyntManger extends MyntManager {

    private MyMyntManger() {
        super(MyApplication.getInstance());
        //initParameters();
    }

    public static MyMyntManger getInstance() {
        return MyBleMangerHolder.instance;
    }

    /*private void initParameters() {
        //set mynt scan params
        final MyntManager.Parameters params = getParameters();
        params.searchDuration = 10000;
        params.searchInterval = 15000;
        params.searchMinimumInterval = 5000;
        params.foundTimeout = 15000;
        params.foundTimeoutInterval = params.foundTimeout / 2;
        setParameters(params);
    }*/

    private static class MyBleMangerHolder {
        static final MyMyntManger instance = new MyMyntManger();
    }

}
