package com.slightech.mynt.sdk.demo.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightech.bluetooth.BluetoothUtils;
import com.slightech.bluetooth.le.LeState;
import com.slightech.mynt.api.MyntManager;
import com.slightech.mynt.api.callback.FoundCallback;
import com.slightech.mynt.api.model.Device;
import com.slightech.mynt.sdk.demo.MyApplication;
import com.slightech.mynt.sdk.demo.R;
import com.slightech.mynt.sdk.demo.ui.base.BaseActivity;
import com.slightech.mynt.sdk.demo.util.KitUtils;
import com.slightech.mynt.sdk.demo.util.LogUtils;
import com.slightech.mynt.sdk.demo.util.ToastUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class SearchActivity extends BaseActivity implements FoundCallback {

    static final String TAG = "SearchActivity";

    private RecyclerView mRecyclerView;
    private ViewAdapter mViewAdapter;

    private MyntManager mMyntManager;

    private boolean mSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle(R.string.title_search);
        initViews();
        initObjects();
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
        mViewAdapter.setOnDeviceClickListener(new ViewAdapter.OnDeviceClickListener() {
            @Override
            public void onItemViewClick(View view, int position, Device device) {
                ControlActivity.start(SearchActivity.this, device);
            }
        });
        mRecyclerView.setAdapter(mViewAdapter);
    }

    private void initObjects() {
        mMyntManager = MyApplication.getMyntManager();
        mMyntManager.setFoundCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        menu.findItem(R.id.start).setVisible(!mSearching);
        menu.findItem(R.id.stop).setVisible(mSearching);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start: startSearch(); break;
            case R.id.stop: stopSearch(true); break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSearch() {
        LogUtils.i(TAG, "startSearch");
        if (BluetoothUtils.isEnabled()) {
            mMyntManager.startSearch();
            mSearching = true;
            invalidateOptionsMenu();
        } else {
            BluetoothUtils.requestEnable(this);
        }
    }

    private void stopSearch(boolean fromUser) {
        LogUtils.i(TAG, "stopSearch");
        mMyntManager.stopSearch();
        mSearching = false;
        invalidateOptionsMenu();
        if (fromUser) {
            mViewAdapter.clear();
        }
    }

    @Override
    public void foundFailed(int errorCode) {
        LogUtils.i(TAG, "foundFailed: %d", errorCode);
        stopSearch(false);
        switch (errorCode) {
            case LeState.ERROR_BLE_NOT_SUPPORTED:
                ToastUtils.show(this, R.string.error_ble_not_supported);
                break;
            case LeState.ERROR_BLUETOOTH_NOT_SUPPORTED:
                ToastUtils.show(this, R.string.error_bluetooth_not_supported);
                break;
            case LeState.ERROR_BLUETOOTH_OFF:
                // ignore
                break;
            case LeState.ERROR_SCAN_FAILED:
                ToastUtils.show(this, R.string.error_scan_failed);
                break;
        }
    }

    @Override
    public void foundDevice(Device device, boolean newOne) {
        LogUtils.i(TAG, "foundDevice: %s, %s", device.sn, newOne);
        mViewAdapter.addDevice(device);
    }

    @Override
    public void foundDeviceRemoved(Device device) {
        LogUtils.i(TAG, "foundDeviceRemoved");
        mViewAdapter.removeDevice(device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textName;
        public TextView textAddr;
        public TextView textRssi;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = KitUtils.findById(itemView, R.id.tv_name);
            textAddr = KitUtils.findById(itemView, R.id.tv_addr);
            textRssi = KitUtils.findById(itemView, R.id.tv_rssi);
        }
    }

    public static class ViewAdapter extends RecyclerView.Adapter<ViewHolder>
            implements View.OnClickListener {

        static final String FILTER_NAME_PREFIX = "MYNT";

        private LinkedList<Device> mDevices;
        private LinkedList<Device> mDevicesCache;

        private long mLastUpdateMillis;

        private OnDeviceClickListener mOnDeviceClickListener;

        public ViewAdapter() {
            mDevices = new LinkedList<>();
            mDevicesCache = new LinkedList<>();
        }

        public void setOnDeviceClickListener(@Nullable OnDeviceClickListener listener) {
            mOnDeviceClickListener = listener;
        }

        public void addDevice(Device device) {
            Device deviceCache = containsDevice(mDevicesCache, device.sn);
            if (deviceCache != null) {
                return;
            }
            Device deviceNow = containsDevice(mDevices, device.sn);
            if (deviceNow == null) {
                mDevicesCache.add(device);
            }
            notifyDevicesChanged(true);
        }

        public void removeDevice(Device device) {
            Device deviceNow = removeDevice(mDevices, device.sn);
            if (deviceNow == null) {
                removeDevice(mDevicesCache, device.sn);
                return;
            }
            notifyDevicesChanged(false);
        }

        public void clear() {
            mDevices.clear();
            mDevicesCache.clear();
            notifyDataSetChanged();
        }

        private Device containsDevice(LinkedList<Device> devices, String sn) {
            for (Device device : devices) {
                if (sn.equals(device.sn)) {
                    return device;
                }
            }
            return null;
        }

        private Device removeDevice(LinkedList<Device> devices, String sn) {
            Iterator<Device> it = devices.iterator();
            Device device;
            while (it.hasNext()) {
                device = it.next();
                if (sn.equals(device.sn)) {
                    it.remove();
                    return device;
                }
            }
            return null;
        }

        private void notifyDevicesChanged(boolean delayWithCache) {
            if (delayWithCache && (System.currentTimeMillis() - mLastUpdateMillis < 1000)) {
                return;
            }
            mLastUpdateMillis = System.currentTimeMillis();

            if (!mDevicesCache.isEmpty()) {
                mDevices.addAll(mDevicesCache);
                mDevicesCache.clear();
            }
            sortDevices();
            notifyDataSetChanged();
        }

        private void sortDevices() {
            Collections.sort(mDevices, new Comparator<Device>() {
                @Override
                public int compare(Device lhs, Device rhs) {
                    String lhsName = lhs.name;
                    String rhsName = rhs.name;
                    int rssiDiff = rhs.rssi - lhs.rssi;
                    if (isEmpty(lhsName)) {
                        if (isEmpty(rhsName)) return rssiDiff;
                        else return 1;
                    } else if (isMynt(lhsName)) {
                        if (isEmpty(rhsName)) return -1;
                        else if (isMynt(rhsName)) return rssiDiff;
                        else return -1;
                    } else {
                        if (isEmpty(rhsName)) return -1;
                        else if (isMynt(rhsName)) return 1;
                        else return rssiDiff;
                    }
                }

                private boolean isEmpty(String s) {
                    return s == null || s.length() <= 0;
                }

                private boolean isMynt(String s) {
                    return s.startsWith(FILTER_NAME_PREFIX);
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
            v.setOnClickListener(this);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemView.setTag(position);

            final Device device = mDevices.get(position);
            holder.textName.setText(device.name + " " + device.sn);
            holder.textAddr.setText(device.address);
            holder.textRssi.setText(device.rssi + "dB");
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }

        @Override
        public void onClick(View v) {
            if (mOnDeviceClickListener != null) {
                final int position = (int) v.getTag();
                mOnDeviceClickListener.onItemViewClick(v, position, mDevices.get(position));
            }
        }

        public interface OnDeviceClickListener {
            void onItemViewClick(View view, int position, Device device);
        }
    }

    //private final int REQ_ACCESS_LOCATION = 1;

    /**
     * <p>Android 6.0 及以上蓝牙能够扫描到设备，需要蓝牙和定位都开启时才行，如果 targetSdkVersion >= 23。
     *
     * <p>http://stackoverflow.com/questions/32708374/bluetooth-le-scanfilters-dont-work-on-android-m
     *
     * <p>java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
     */
    /*private void requestPermissions() {
        if (!PermissionUtils.checkPermissionsGranted(this, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                    "Request ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION to get scan results for Bluetooth LE",
                    REQ_ACCESS_LOCATION);
        }
    }

    private void requestPermissions(@NonNull final String[] permissions,
                                    @Nullable final String explanation,
                                    final int requestCode) {
        if (PermissionUtils.shouldPermissionsShowRationale(this, permissions)) {
            if (explanation == null) {
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Request Permissions")
                    .setMessage(explanation)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SearchActivity.this,
                                    permissions, requestCode);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_ACCESS_LOCATION) {
            if (PermissionUtils.verifyPermission(grantResults)) {
                ToastUtils.show(this, "Permission was granted, yay!");
            } else {
                ToastUtils.show(this, "Permission denied, boo!");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/
}
