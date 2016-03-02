package com.slightech.sdkdemo.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.slightech.ble.mynt.model.Device;
import com.slightech.sdkdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Willard  on 2015/9/22.
 */
public class DeviceAdapter extends BaseAdapter {

    private List<Device> devices;
    private Context mContext;

    public DeviceAdapter(Context context) {
        devices = new ArrayList<>();
        mContext = context;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device addDevice) {
        boolean isExists = false;
        for (Device device : devices) {
            if (device.sn.equals(addDevice.sn)) {
                isExists = true;
                break;
            }
        }
        if (!isExists) {
            devices.add(addDevice);
            notifyDataSetChanged();
        }
    }

    public void removeDevice(Device rmDevice) {
        for (int index = 0; index < devices.size(); index++) {
            Device device = devices.get(index);
            if (device.sn.equals(rmDevice.sn)) {
                devices.remove(index);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = devices.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_devices_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.text_address);
            viewHolder.tvSn = (TextView) convertView.findViewById(R.id.text_sn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvAddress.setText("mac:" + device.address);
        viewHolder.tvSn.setText("sn:" + device.sn);
        return convertView;
    }

    static class ViewHolder {
        TextView tvAddress;
        TextView tvSn;
    }

}