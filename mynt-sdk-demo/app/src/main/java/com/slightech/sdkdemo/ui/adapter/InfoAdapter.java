package com.slightech.sdkdemo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.slightech.sdkdemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class InfoAdapter extends BaseAdapter {

    final Context mContext;
    final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    final LinkedList<Info> mInfos = new LinkedList<Info>();

    public InfoAdapter(Context context) {
        mContext = context;
    }

    public void p(String text) {
        mInfos.addFirst(new Info(timestamp() + "  " + text));
        notifyDataSetChanged();
    }

    private String timestamp() {
        return mFormat.format(new Date());
    }

    public void clear() {
        if (!mInfos.isEmpty()) {
            mInfos.clear();
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_info, parent, false);
        }

        final TextView textView = (TextView) view.findViewById(R.id.text_info);
        bindInfo(textView, mInfos.get(position));

        return view;
    }

    private void bindInfo(TextView textView, Info info) {
        textView.setText(info.text);
    }

    private static class Info {

        final String text;

        public Info(String text) {
            this.text = text;
        }
    }

}
