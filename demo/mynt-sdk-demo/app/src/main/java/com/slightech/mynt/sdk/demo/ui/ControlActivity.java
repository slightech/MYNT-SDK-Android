package com.slightech.mynt.sdk.demo.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightech.mynt.api.MyntManager;
import com.slightech.mynt.api.callback.EventCallback;
import com.slightech.mynt.api.callback.KeyCallback;
import com.slightech.mynt.api.callback.PairCallback;
import com.slightech.mynt.api.event.ActionEvent;
import com.slightech.mynt.api.event.ClickEvent;
import com.slightech.mynt.api.mode.ConnectMode;
import com.slightech.mynt.api.mode.ControlMode;
import com.slightech.mynt.api.model.Device;
import com.slightech.mynt.api.oad.OADProgInfo;
import com.slightech.mynt.api.oad.OADUpdater;
import com.slightech.mynt.sdk.demo.MyApplication;
import com.slightech.mynt.sdk.demo.R;
import com.slightech.mynt.sdk.demo.ui.base.BaseActivity;
import com.slightech.mynt.sdk.demo.ui.dialog.UpdateDialogFragment;
import com.slightech.mynt.sdk.demo.util.KitUtils;
import com.slightech.mynt.sdk.demo.util.LogUtils;
import com.slightech.mynt.sdk.demo.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ControlActivity extends BaseActivity implements PairCallback, EventCallback, KeyCallback,
        UpdateDialogFragment.OnUpdateSelectListener {

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
    private TextView mTextView;

    private MyntManager mMyntManager;
    private String mDeviceSn;

    public enum State {
        Disconnected, Connecting, Connected, Binding, Bound,
    }

    private State mState = State.Disconnected;
    private boolean mAlarmOn = false;
    private boolean mUpdating = false;

    @SuppressLint("SimpleDateFormat")
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

        mTextView = KitUtils.findById(this, R.id.text);
    }

    private void updateInfo(Device device) {
        mTextView.setText(String.format("%s\n%s",
                device.connectMode.name(), device.controlMode.name()));
    }

    private void updateInfo(String sn, ControlMode mode) {
        // update control mode directly
        final Device device = mMyntManager.findDevice(sn);
        if (device == null) return;
        device.controlMode = mode;
        updateInfo(device);
    }

    private void intObjects() {
        Device device = getIntent().getParcelableExtra(EXTRA_DEVICE);
        assert device != null : "Must give a device to control";
        LogUtils.i(TAG, "device: %s", device.sn);

        mMyntManager = MyApplication.getMyntManager();
        // don't forget to remove callbacks in `onDestroy`
        mMyntManager.addPairCallback(this);
        mMyntManager.addEventCallback(this);

        // If support the lower version firmware with key pairing, unnecessary now
        mMyntManager.setKeyCallback(this);

        mDeviceSn = device.sn;

        setTitle(mDeviceSn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);

        menu.findItem(R.id.connect).setVisible(mState == State.Disconnected);

        boolean connected = (mState == State.Connected);
        boolean bound = (mState == State.Bound);

        final Device device = mMyntManager.findDevice(mDeviceSn);
        boolean bleMode = device != null && device.connectMode == ConnectMode.CONNECT_BLE;
        boolean hidMode = device != null && device.connectMode == ConnectMode.CONNECT_HID;

        menu.findItem(R.id.disconnect).setVisible(connected || bound);

        menu.findItem(R.id.toggle_alarm).setVisible(bound);
        menu.findItem(R.id.request_rssi).setVisible(bound);
        menu.findItem(R.id.request_battery).setVisible(bound);
        menu.findItem(R.id.request_info).setVisible(bound);
        menu.findItem(R.id.request_control_custom_action).setVisible(bound);
        menu.findItem(R.id.send_control_mode).setVisible(bound);
        menu.findItem(R.id.send_control_custom_clicks).setVisible(bound);
        menu.findItem(R.id.update_firmware).setVisible(bound);

        // It's not recommended to using BLE connect mode.
        menu.findItem(R.id.setup_ble_mode).setVisible(false);
        //menu.findItem(R.id.setup_ble_mode).setVisible(bound && hidMode);
        menu.findItem(R.id.setup_hid_mode).setVisible(bound && bleMode);

        // `MyntManager#sendControl*` methods are only valid in `ConnectMode#CONNECT_HID` mode.
        menu.findItem(R.id.send_control_mode).setEnabled(hidMode);
        menu.findItem(R.id.send_control_custom_clicks).setEnabled(hidMode);
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
                pStrong(getString(R.string.toggle_alarm) + " " + mAlarmOn);
                mMyntManager.alarmLong(mDeviceSn, mAlarmOn);
                break;
            case R.id.request_rssi:
                pStrong(getString(R.string.request_rssi));
                mMyntManager.requestRssi(mDeviceSn);
                break;
            case R.id.request_battery:
                pStrong(getString(R.string.request_battery));
                mMyntManager.requestBattery(mDeviceSn);
                break;
            case R.id.request_info:
                pStrong(getString(R.string.request_info));
                mMyntManager.requestInfo(mDeviceSn);
                break;
            case R.id.request_control_custom_action:
                pStrong(getString(R.string.request_control_custom_action));
                mMyntManager.requestControlCustomAction(mDeviceSn);
                break;
            case R.id.send_control_mode:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.send_control_mode)
                        .setItems(R.array.control_modes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ControlMode mode = ControlMode.get(which + 1);
                                pStrong(getString(R.string.send_control_mode) + " " + mode.name());
                                mMyntManager.sendControlMode(mDeviceSn, mode);
                                updateInfo(mDeviceSn, mode);
                            }
                        })
                        .show();
                break;
            case R.id.send_control_custom_clicks:
                pStrong(getString(R.string.send_control_custom_clicks));
                mMyntManager.sendControlCustomClicks(mDeviceSn);
                updateInfo(mDeviceSn, ControlMode.CONTROL_MODE_CUSTOM);
                break;
            case R.id.update_firmware:
                updateFirmware();
                break;
            case R.id.setup_ble_mode:
                pStrong(getString(R.string.setup_ble_mode));
                mMyntManager.setupConnectBLE(mDeviceSn);
                break;
            case R.id.setup_hid_mode:
                pStrong(getString(R.string.setup_hid_mode));
                mMyntManager.setupConnectHID(mDeviceSn);
                break;
            case R.id.clear_history:
                mViewAdapter.clear();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connect() {
        pStrong("connect");
        // should not use the input device directly whose state will be incorrect
        boolean ok = mMyntManager.connect(mDeviceSn);
        if (!ok) pWarn("connect failed");
    }

    private void disconnect() {
        // keepSystemBond false
        pStrong("disconnect");
        mMyntManager.disconnect(mDeviceSn, false);
    }

    private void updateFirmware() {
        String msg = null;
        if (mUpdating) {
            msg = "firmware is updating now";
            return;
        }
        if (!mMyntManager.checkOADService(mDeviceSn)) {
            msg = "firmware update service is failed";
        }
        if (msg != null) {
            pWarn(msg);
            ToastUtils.show(this, msg);
            return;
        }
        UpdateDialogFragment.show(this, mDeviceSn).setOnUpdateSelectListener(this);
    }

    @Override
    public void onUpdateSelect(String filepath, boolean formAssets) {
        pStrong("select: " + filepath);
        mMyntManager.sendOADFile(mDeviceSn, filepath, formAssets, createUpdateCallback());
    }

    private OADUpdater.Callback createUpdateCallback() {
        return new OADUpdater.Callback() {

            long mTimeStart;
            PowerManager.WakeLock mWakeLock;

            @Override
            public void onError(OADUpdater updater, int errorCode) {
                String errorMsg = errorCode + ": " + OADUpdater.errorToString(errorCode);
                pWarn("update error: " + errorMsg);
                ToastUtils.show(ControlActivity.this, errorMsg);
                mUpdating = false;
            }

            @Override
            public void onPrepare(OADUpdater updater) {
                pStrong("update: prepare");
            }

            @Override
            public void onStart(OADUpdater updater) {
                pStrong("update: start to send");
                mTimeStart = System.currentTimeMillis();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "StayWake");
                mWakeLock.acquire();
                mUpdating = true;
            }

            @Override
            public void onProgress(OADUpdater updater, float progress) {
                final OADProgInfo info = updater.getOADProgInfo();
                Log.i(TAG, String.format(
                        "progress: %.2f; trans blocks: %d; total blocks: %d",
                        progress, info.iBlocks(), info.nBlocks()));
                setTitle(String.format("%.1f%%, %d/%d blocks", progress, info.iBlocks(), info.nBlocks()));
            }

            @Override
            public void onStop(OADUpdater updater, boolean success) {
                final long elapsed = System.currentTimeMillis() - mTimeStart;
                String result = (success ? "success" : "failed");
                pStrong("update: " + result + "; elapsed: " + (elapsed / 1000) + "s");
                if (mWakeLock != null) {
                    mWakeLock.release();
                }
                mWakeLock = null;
                mUpdating = false;
                setTitle(mDeviceSn);
                ToastUtils.show(ControlActivity.this, "Update " + result);

                // disconnect if success for reconnecting it much quickly
                if (success) {
                    mMyntManager.disconnect(mDeviceSn);
                }
            }
        };
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
    protected void onDestroy() {
        super.onDestroy();
        mMyntManager.removePairCallback(this);
        mMyntManager.removeEventCallback(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void updateState(State state) {
        mState = state;
        invalidateOptionsMenu();
    }

    @Override
    public void pairConnectStart(Device device) {
        pInfo("pairConnectStart");
        updateState(State.Connecting);
    }

    @Override
    public void pairConnectOver(Device device, int errorCode, int status) {
        if (errorCode == 0) {
            // success
            pInfo("pairConnectOver: %d, %d", errorCode, status);
            updateState(State.Connected);
        } else {
            // failed, then will disconnect
            pError("pairConnectOver: %d, %d", errorCode, status);
        }
    }

    @Override
    public void pairServicesDiscovered(Device device, boolean success) {
        if (success) {
            pInfo("pairServicesDiscovered: true");
            updateState(State.Binding);
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
        pInfo("pairBindOver:\n"
                + "  connectMode: %s\n"
                + "  controlMode: %s",
                device.connectMode.name(),
                device.controlMode.name());
        updateState(State.Bound);
        updateInfo(device);
        // get the firmware info once bound
        pStrong(getString(R.string.request_info));
        mMyntManager.requestInfo(mDeviceSn);
    }

    @Override
    public void pairDisconnect(Device device, boolean fromUser) {
        pInfo("pairDisconnect: %s", fromUser);
        updateState(State.Disconnected);
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

    @Override
    public String keyStored(Device device) {
        pStrong("please click the MYNT button to pair");
        return null;
    }

    @Override
    public void keyPaired(Device device, String password) {
        pInfo("keyPaired: %s", password);
    }

    private void pInfo(String msg, Object... args) {
        LogUtils.i(TAG, msg, args);
        pushInfo(Color.GRAY, msg, args);
    }

    private void pStrong(String msg, Object... args) {
        LogUtils.i(TAG, msg, args);
        pushInfo(ContextCompat.getColor(this, R.color.blue), msg, args);
    }

    private void pWarn(String msg, Object... args) {
        LogUtils.w(TAG, msg, args);
        pushInfo(ContextCompat.getColor(this, R.color.orange), msg, args);
    }

    private void pError(String msg, Object... args) {
        LogUtils.e(TAG, msg, args);
        pushInfo(ContextCompat.getColor(this, R.color.red), msg, args);
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

        public void clear() {
            mInfos.clear();
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
