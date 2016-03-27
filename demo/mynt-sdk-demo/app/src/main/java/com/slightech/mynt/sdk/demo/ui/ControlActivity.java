package com.slightech.mynt.sdk.demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.slightech.mynt.api.MyntManager;
import com.slightech.mynt.api.model.Device;
import com.slightech.mynt.sdk.demo.R;
import com.slightech.mynt.sdk.demo.ui.base.BaseActivity;
import com.slightech.mynt.sdk.demo.util.KitUtils;
import com.slightech.mynt.sdk.demo.util.LogUtils;

public class ControlActivity extends BaseActivity {

    static final String TAG = "SearchActivity";

    public static final String EXTRA_DEVICE = "device";

    public static void start(Activity activity, Device device) {
        Intent intent = new Intent(activity, ControlActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ControlActivity.EXTRA_DEVICE, device);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private MyntManager mMyntManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setTitle(R.string.title_control);
        initViews();
        intObjects();
    }

    private void initViews() {
        Toolbar bar = KitUtils.findById(this, R.id.bar);
        if (bar != null) {
            setSupportActionBar(bar);
        }
    }

    private void intObjects() {
        Device device = getIntent().getParcelableExtra(EXTRA_DEVICE);
        assert device != null : "Must give a device to control";
        LogUtils.i(TAG, "device: %s", device.sn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
