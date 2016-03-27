package com.slightech.mynt.sdk.demo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightech.mynt.api.MyntManager;
import com.slightech.mynt.api.callback.EventCallback;
import com.slightech.mynt.api.callback.PairCallback;
import com.slightech.mynt.api.event.ActionEvent;
import com.slightech.mynt.api.event.ClickEvent;
import com.slightech.mynt.api.model.Device;
import com.slightech.mynt.sdk.demo.MyApplication;
import com.slightech.mynt.sdk.demo.R;
import com.slightech.mynt.sdk.demo.ui.base.BaseActivity;
import com.slightech.mynt.sdk.demo.util.KitUtils;
import com.slightech.mynt.sdk.demo.util.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ControlActivity extends BaseActivity implements PairCallback, EventCallback {

    static final String TAG = "SearchActivity";

    public static final String EXTRA_DEVICE = "device";

    public static void start(Activity activity, Device device) {
        Intent intent = new Intent(activity, ControlActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ControlActivity.EXTRA_DEVICE, device);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private RecyclerView mRecyclerView;
    private ViewAdapter mViewAdapter;

    private MyntManager mMyntManager;
    private String mDeviceSn;

    public enum State {
        Disconnected, Connecting, Connected,
    }

    private State mState = State.Disconnected;
    private boolean mAlarmOn = false;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

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

        mRecyclerView = KitUtils.findById(this, R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mViewAdapter = new ViewAdapter();
        mRecyclerView.setAdapter(mViewAdapter);
    }

    private void intObjects() {
        Device device = getIntent().getParcelableExtra(EXTRA_DEVICE);
        assert device != null : "Must give a device to control";
        LogUtils.i(TAG, "device: %s", device.sn);

        mMyntManager = MyApplication.getMyntManager();
        mMyntManager.setPairCallback(this);
        mMyntManager.setEventCallback(this);

        mDeviceSn = device.sn;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        menu.findItem(R.id.connect).setVisible(mState == State.Disconnected);
        boolean connected = (mState == State.Connected);
        menu.findItem(R.id.disconnect).setVisible(connected);
        menu.findItem(R.id.toggle_alarm).setVisible(connected);
        menu.findItem(R.id.request_rssi).setVisible(connected);
        menu.findItem(R.id.request_battery).setVisible(connected);
        menu.findItem(R.id.request_info).setVisible(connected);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                connect();
                break;
            case R.id.disconnect:
                disconnect();
                break;
            case R.id.toggle_alarm:
                mAlarmOn = !mAlarmOn;
                mMyntManager.alarmLong(mDeviceSn, mAlarmOn);
                break;
            case R.id.request_rssi:
                mMyntManager.requestRssi(mDeviceSn);
                break;
            case R.id.request_battery:
                mMyntManager.requestBattery(mDeviceSn);
                break;
            case R.id.request_info:
                mMyntManager.requestInfo(mDeviceSn);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connect() {
        pStrong("connect " + mDeviceSn);
        // should not use the input device directly whose state will be incorrect
        mMyntManager.connect(mDeviceSn);
    }

    private void disconnect() {
        // keepSystemBond false
        pStrong("disconnect " + mDeviceSn);
        mMyntManager.disconnect(mDeviceSn, false);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        connect();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disconnect();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void pairConnectStart(Device device) {
        pInfo("pairConnectStart");
        mState = State.Connecting;
        invalidateOptionsMenu();
    }

    @Override
    public void pairConnectOver(Device device, int errorCode, int status) {
        if (errorCode == 0) {
            // success
            pInfo("pairConnectOver: %d, %d", errorCode, status);
            mState = State.Connected;
            invalidateOptionsMenu();
        } else {
            // failed, then will disconnect
            pError("pairConnectOver: %d, %d", errorCode, status);
        }
    }

    @Override
    public void pairServicesDiscovered(Device device, boolean success) {
        if (success) {
            pInfo("pairServicesDiscovered: true");
        } else {
            pError("pairServicesDiscovered: false");
        }
    }

    @Override
    public void pairServicesDiscoverTimeout(Device device) {
        pWarn("pairServicesDiscoverTimeout");
    }

    @Override
    public void pairBindOver(Device device) {
        pInfo("pairBindOver");
    }

    @Override
    public void pairDisconnect(Device device, boolean fromUser) {
        pInfo("pairDisconnect: %s", fromUser);
        mState = State.Disconnected;
        invalidateOptionsMenu();
    }

    @Override
    public void pairDisconnectError(Device device, int errorCode, int status) {
        pWarn("pairDisconnectError: %d, %d", errorCode, status);
    }

    @Override
    public void eventFired(Device device, ClickEvent event) {
        pInfo("eventFired: %s", event.name());
    }

    @Override
    public void eventFired(Device device, ActionEvent event) {
        pInfo("eventFired: %s", event.name());
    }

    @Override
    public void eventInfoChanged(Device device, Device.Info info) {
        pInfo("eventInfoChanged:\n"
                + "  firmware: %s\n"
                + "  hardware: %s\n"
                + "  software: %s",
                info.firmwareVersion(),
                info.hardwareVersion(),
                info.softwareVersion());
    }

    @Override
    public void eventActionChanged(Device device, Device.Action action) {
        pInfo("eventActionChanged:\n"
                + "  click: %s\n"
                + "  doubleClick: %s\n"
                + "  tripleClick: %s\n"
                + "  longClick: %s\n"
                + "  clickHold: %s",
                action.click,
                action.doubleClick,
                action.tripleClick,
                action.longClick,
                action.clickHold);
    }

    @Override
    public void eventRssiChanged(Device device, int rssi) {
        pInfo("eventRssiChanged: %s", rssi);
    }

    @Override
    public void eventBatteryChanged(Device device, int battery) {
        pInfo("eventBatteryChanged: %s", battery);
    }

    @Override
    public void eventAlarmChanged(Device device, boolean on) {
        pInfo("eventAlarmChanged: %s", on);
    }

    private void pInfo(String msg, Object... args) {
        LogUtils.i(TAG, msg, args);
        pushInfo(Color.GRAY, msg, args);
    }

    private void pStrong(String msg, Object... args) {
        LogUtils.i(TAG, msg, args);
        pushInfo(getResources().getColor(R.color.blue), msg, args);
    }

    private void pWarn(String msg, Object... args) {
        LogUtils.w(TAG, msg, args);
        pushInfo(getResources().getColor(R.color.orange), msg, args);
    }

    private void pError(String msg, Object... args) {
        LogUtils.e(TAG, msg, args);
        pushInfo(getResources().getColor(R.color.red), msg, args);
    }

    private void pushInfo(@ColorInt int color, String msg, Object... args) {
        mViewAdapter.pushInfo(color, mDateFormat.format(new Date()) + "  " + String.format(msg, args));
    }

    public static class Info {

        public int color;
        public String text;

        public Info(int color, String text) {
            this.color = color;
            this.text = text;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            textInfo = KitUtils.findById(itemView, R.id.tv_info);
        }
    }

    public static class ViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        private LinkedList<Info> mInfos;

        public ViewAdapter() {
            mInfos = new LinkedList<>();
        }

        public void pushInfo(@ColorInt int color, String text) {
            mInfos.push(new Info(color, text));
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.item_info, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Info info = mInfos.get(position);
            holder.textInfo.setText(info.text);
            holder.textInfo.setTextColor(info.color);
        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }
    }
}
