package com.slightech.sdkdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.slightech.ble.mynt.AbsMyntManager;
import com.slightech.ble.mynt.model.Device;
import com.slightech.sdkdemo.R;
import com.slightech.sdkdemo.manager.MyMyntManger;
import com.slightech.sdkdemo.ui.adapter.DeviceAdapter;
public class MainActivity extends Activity implements View.OnClickListener,
        AbsMyntManager.FoundCallback, AdapterView.OnItemClickListener {
    private static final String TAG = MainActivity.class.getName();
    private Button mBtnScan;
    private ListView mListDevices;
    private boolean mScan = true;
    private MyMyntManger mMyntManager;
    private DeviceAdapter mDeviceAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyntManager = MyMyntManger.getInstance();
        mMyntManager.setFoundCallback(this);

        setContentView(R.layout.activity_main);
        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListDevices = (ListView) findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this);
        mListDevices.setAdapter(mDeviceAdapter);
        mListDevices.setOnItemClickListener(this);
        mBtnScan.setOnClickListener(this);
        setBtnText();

    }


    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.btn_scan:
               if (mScan) {
                   stopScan();
               } else {
                   startScan();
               }
               mScan = !mScan;
               setBtnText();
               break;
       }
    }

    /**
     * startScan
     */
    private void startScan() {
        mMyntManager.startSearch();
    }

    /**
     * stopScan
     */
    private void stopScan() {
        mMyntManager.stopSearch();
    }

    private void setBtnText() {
        String text = mScan ? this.getString(R.string.start_scan) : this.getString(R.string.stop_scan);
        mBtnScan.setText(text);
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
         //toMyntCallBackDemoActivity(device);
        toMyntListenerCallBackDemoActivity(device);
    }

    /**
     * to demo for MyntManger CallBack demo
     * @param device
     */
    public void toMyntCallBackDemoActivity(Device device) {
        Intent intent = new Intent(this, MyntCallBackDemoActivity.class);
        intent.putExtra(MyntCallBackDemoActivity.ARG_SN, device.sn);
        intent.putExtra(MyntCallBackDemoActivity.ARG_ADDRESS, device.address);
        startActivity(intent);
    }
    /**
     * to demo for MyntListener Callback demo
     * @param device
     */
    public void toMyntListenerCallBackDemoActivity(Device device) {
        Intent intent = new Intent(this, MyntListenerCallBackDemoActivity.class);
        intent.putExtra(MyntListenerCallBackDemoActivity.ARG_SN, device.sn);
        intent.putExtra(MyntListenerCallBackDemoActivity.ARG_ADDRESS, device.address);
        startActivity(intent);
    }

}
