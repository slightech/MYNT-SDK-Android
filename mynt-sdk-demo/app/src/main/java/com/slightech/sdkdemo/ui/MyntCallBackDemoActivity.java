package com.slightech.sdkdemo.ui;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightech.ble.mynt.MyntClickMode;
import com.slightech.ble.mynt.MyntEvent;
import com.slightech.ble.mynt.callback.EventCallback;
import com.slightech.ble.mynt.callback.HIDCallback;
import com.slightech.ble.mynt.callback.PairCallback;
import com.slightech.ble.mynt.model.Device;
import com.slightech.ble.mynt.model.DeviceInfo;
import com.slightech.sdkdemo.R;
import com.slightech.sdkdemo.manager.MyMyntManger;
import com.slightech.sdkdemo.ui.adapter.InfoAdapter;

/**
 * Created by Willard
 * <p>
 * use MyntManager for pair device and process mynt click
 */
public class MyntCallBackDemoActivity extends Activity implements View.OnClickListener,
        PairCallback, EventCallback, HIDCallback {

    public static final String ARG_SN = "device_sn";
    public static final String ARG_ADDRESS = "device_address";

    private static final int STATE_UNCONNECT = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;

    private MyMyntManger mMyntManager;

    private String mSn;
    private String mAddress;

    private TextView mTextInfo;
    private TextView mTextRss;
    private TextView mTextBattery;
    private Button mBtnConnect;
    private Button mBtnRing;
    private Button mBtnTimeRing;
    private TextView mTextState;
    private ListView mListLog;

    private InfoAdapter infoAdapter;

    private int mState = STATE_CONNECTED;

    private boolean mRing;
    private boolean mTimeRing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnRing = (Button) findViewById(R.id.btn_ring);
        mBtnTimeRing = (Button) findViewById(R.id.btn_time_ring);
        mTextInfo = (TextView) findViewById(R.id.textInfo);
        mTextRss = (TextView) findViewById(R.id.textRss);
        mTextState = (TextView) findViewById(R.id.textState);
        mTextBattery = (TextView) findViewById(R.id.textBattery);
        mListLog = (ListView) findViewById(R.id.list_log);

        infoAdapter = new InfoAdapter(this);
        mListLog.setAdapter(infoAdapter);
        mBtnConnect.setOnClickListener(this);
        mBtnRing.setOnClickListener(this);
        mBtnTimeRing.setOnClickListener(this);
        initTextInfo();
        setConnectText(STATE_UNCONNECT);
        setRingText(false);
        setTimeRingText(false);

        setMyntCallBack();
    }

    private void setMyntCallBack() {
        mMyntManager = MyMyntManger.getInstance();
        // 配对过程回调
        mMyntManager.setPairCallback(this);
        // 事件监听回调
        mMyntManager.setEventCallback(this);
        // HID回调，刚连接会读取当前HID配置
        mMyntManager.setHIDCallback(this);
    }

    private void initTextInfo() {
        mSn = getIntent().getStringExtra(ARG_SN);
        mAddress = getIntent().getStringExtra(ARG_ADDRESS);
        mTextInfo.setText(String.format("sn: %s \naddress: %s", mSn, mAddress));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                if (mState == STATE_CONNECTING) {
                    return;
                }
                if (mState == STATE_UNCONNECT) {
                    mMyntManager.connect(mSn);
                } else {
                    mMyntManager.disconnect(mSn);
                }
                break;
            case R.id.btn_ring:
                if (!mRing) {
                    mMyntManager.alarmLong(mSn, true);
                } else {
                    mMyntManager.alarmLong(mSn, false);
                }
                setRingText(!mRing);
                break;
            case R.id.btn_time_ring:
                if (!mTimeRing) {
                    mMyntManager.alarm(mSn, true);
                } else {
                    mMyntManager.alarm(mSn, false);
                }
                setTimeRingText(!mTimeRing);
                break;
        }
    }

    private void setRingText(boolean ring) {
        this.mRing = ring;
        this.mBtnRing.setText(mRing ? getResString(R.string.stop_ring) : getResString(R.string.start_ring));
    }

    private void setTimeRingText(boolean ring) {
        this.mTimeRing = ring;
        this.mBtnTimeRing.setText(mTimeRing ? getResString(R.string.stop_time_ring) : getResString(R.string.start_time_ring));
    }

    private void setConnectText(int state) {
        mState = state;
        String btnText = "unknown";
        String stateText = "unknown";
        switch (mState) {
            case STATE_CONNECTED:
                btnText = getResString(R.string.disconnect);
                stateText = getResString(R.string.state_connected);
                mBtnRing.setEnabled(true);
                mBtnTimeRing.setEnabled(true);
                break;
            case STATE_CONNECTING:
                btnText = getResString(R.string.state_connecting);
                stateText = getResString(R.string.state_connecting);
                mBtnRing.setEnabled(false);
                mBtnTimeRing.setEnabled(false);
                break;
            case STATE_UNCONNECT:
                btnText = getResString(R.string.connect);
                stateText = getResString(R.string.state_unconnected);
                mBtnRing.setEnabled(false);
                mBtnTimeRing.setEnabled(false);
                break;
        }
        mBtnConnect.setText(btnText);
        mTextState.setText(getResString(R.string.state) + stateText);
    }

    @Override
    public void pairConnectStart(Device device) {
        infoAdapter.p("pairConnectStart");
        setConnectText(STATE_CONNECTING);
    }

    @Override
    public void pairConnectOver(Device device, int errorCode, int status) {
        boolean success = (status == BluetoothGatt.GATT_SUCCESS);
        infoAdapter.p(String.format("pairConnectOver errorCode: %s, status: %s", errorCode, status));
    }

    @Override
    public void pairServicesDiscovered(Device device, boolean success) {
        infoAdapter.p(String.format("pairServicesDiscovered success: %s",
                success ? getResString(R.string.success) : getResString(R.string.fail)));
    }

    @Override
    public void pairBindStart(Device device) {
        infoAdapter.p("pairBindStart");
    }

    @Override
    public void pairBindOver(Device device, boolean success) {
        // do noting
    }

    @Override
    public void hidBindOver(Device device) {
        infoAdapter.p(String.format("hidBindOver success"));

        setConnectText(STATE_CONNECTED);
        //Read the signal value, read only once. If you need time to read, you can write a custom device to call this method to read
        mMyntManager.requestRssi(device);
        //read battery
        mMyntManager.requestBattery(device.sn);
        //set alarmNum  when disconnect alram or call alarm method will ring
        mMyntManager.alarmNum(mSn, 1);
    }

    @Override
    public void eventFired(Device device, MyntClickMode myntClickMode) {
    }

    @Override
    public void pairServicesDiscoveredTimeout(Device device) {
    }

    @Override
    public void pairDisconnect(Device device, boolean fromUser) {
        infoAdapter.p(String.format("pairDisconnect"));
        setConnectText(STATE_UNCONNECT);
    }

    @Override
    public void pairDisconnectError(Device device, int errorCode, int status) {
        infoAdapter.p(String.format("pairDisconnectError errorCode: %s, status: %s", errorCode, status));
        setConnectText(STATE_UNCONNECT);
    }

    @Override
    public void eventFired(Device device, MyntEvent myntEvent) {
        switch (myntEvent) {
            case Click:
                infoAdapter.p("Click");
                toast(getResString(R.string.click));
                break;
            case DoubleClick:
                infoAdapter.p("DoubleClick");
                toast(getResString(R.string.doubleClick));
                break;
            case TripleClick:
                infoAdapter.p("TripleClick");
                toast(getResString(R.string.tripleClick));
                break;
            case LongPress:
                infoAdapter.p("LongClick");
                toast(getResString(R.string.longClick));
                break;
        }
    }

    @Override
    public void eventPasswordReceived(Device device, byte[] password) {
    }

    @Override
    public void eventInfoChanged(Device device, DeviceInfo deviceInfo) {
    }

    @Override
    public void eventRssiChanged(Device device, int rssi) {
        mTextRss.setText("RSSI: " + rssi);
    }

    @Override
    public void eventBatteryChanged(Device device, int battery) {
        mTextBattery.setText("Battery: " + battery);
    }

    @Override
    public void eventAlarmChanged(Device device, boolean on) {
    }

    @Override
    public void onHIDClickChanged(Device device) {
        infoAdapter.p("HID click: " + device.click.getName()
                + "\ndoubleClick: " + device.doubleClick.getName()
                + "\ntripleClick: " + device.tripleClick.getName()
                + "\nlongClick: " + device.longClick.getName()
                + "\nclickHold: " + device.clickHold.getName());
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String getResString(int resId) {
        return this.getResources().getString(resId);
    }

}
