package com.slightech.sdkdemo.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.slightech.ble.mynt.callback.FoundCallback;
import com.slightech.ble.mynt.model.Device;
import com.slightech.sdkdemo.R;
import com.slightech.sdkdemo.manager.MyMyntManger;
import com.slightech.sdkdemo.ui.adapter.DeviceAdapter;

public class MainActivity extends Activity implements View.OnClickListener,
        FoundCallback, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getName();

    private Button mBtnScan;
    private ListView mListDevices;

    private boolean mScanning = false;

    private MyMyntManger mMyntManager;
    private DeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyntManager = MyMyntManger.getInstance();
        mMyntManager.setFoundCallback(this);

        setContentView(R.layout.activity_main);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(this);

        mListDevices = (ListView) findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this);
        mListDevices.setAdapter(mDeviceAdapter);
        mListDevices.setOnItemClickListener(this);

        setBtnText();

        // ensure enable bluetooth
        BluetoothAdapter.getDefaultAdapter().enable();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (mScanning) {
                    stopScan();
                } else {
                    startScan();
                }
                break;
        }
    }

    /**
     * startScan
     */
    private void startScan() {
        mMyntManager.startSearch();
        mScanning = true;
        setBtnText();
    }

    /**
     * stopScan
     */
    private void stopScan() {
        mMyntManager.stopSearch();
        mScanning = false;
        setBtnText();
    }

    private void setBtnText() {
        mBtnScan.setText(mScanning ? R.string.stop_scan : R.string.start_scan);
    }

    @Override
    public void foundFailed(int errorCode) {
        Log.i(TAG, "errorCode:" + errorCode);
    }

    @Override
    public void foundDevice(Device device, boolean newOne) {
        if (newOne) {
            mDeviceAdapter.addDevice(device);
        }
    }

    @Override
    public void foundDeviceRemoved(Device device) {
        mDeviceAdapter.removeDevice(device);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = (Device) parent.getItemAtPosition(position);
        toMyntCallBackDemoActivity(device);
    }

    /**
     * to demo for MyntManger CallBack demo
     *
     * @param device
     */
    public void toMyntCallBackDemoActivity(Device device) {
        Intent intent = new Intent(this, MyntCallBackDemoActivity.class);
        intent.putExtra(MyntCallBackDemoActivity.ARG_SN, device.sn);
        intent.putExtra(MyntCallBackDemoActivity.ARG_ADDRESS, device.address);
        startActivity(intent);
    }

}
