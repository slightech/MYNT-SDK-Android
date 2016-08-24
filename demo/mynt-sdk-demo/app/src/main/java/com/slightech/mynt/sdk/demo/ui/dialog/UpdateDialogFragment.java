package com.slightech.mynt.sdk.demo.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.slightech.mynt.sdk.demo.Firmware;
import com.slightech.mynt.sdk.demo.R;
import com.slightech.mynt.sdk.demo.ui.base.BaseActivity;
import com.slightech.mynt.sdk.demo.ui.base.BaseDialogFragment;
import com.slightech.mynt.sdk.demo.util.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class UpdateDialogFragment extends BaseDialogFragment implements
        ListView.OnItemClickListener {

    public static UpdateDialogFragment show(BaseActivity activity, String title) {
        UpdateDialogFragment dlg = new UpdateDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        dlg.setArguments(args);
        dlg.show(activity.getSupportFragmentManager(), "dlg_update");
        return dlg;
    }

    private final String SELECT_BIN_FILE = "Select external bin file";
    private final int REQ_SELECT = 1;

    private ListView mListView;

    private String mTitle;

    private ArrayList<String> mData;

    private OnUpdateSelectListener mListener;

    public UpdateDialogFragment setOnUpdateSelectListener(OnUpdateSelectListener l) {
        mListener = l;
        return this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString("title");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        @SuppressLint("InflateParams")
        View v = activity.getLayoutInflater().inflate(R.layout.frag_dlg_update, null);

        ListView list = (ListView) v.findViewById(R.id.list);
        list.setOnItemClickListener(this);
        mListView = list;

        updateListView();

        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle(mTitle)
                .setView(v)
                .setNegativeButton(android.R.string.no, null);
        return b.create();
    }

    private void updateListView() {
        ArrayList<String> data = new ArrayList<>();
        data.addAll(Arrays.asList(Firmware.FILES));
        data.add(SELECT_BIN_FILE);
        mData = data;
        mListView.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1, data));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int end = mData.size() - 1;
        if (position == end) {
            showFileChooser();
        } else {
            onUpdateSelect(mData.get(position), true);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, SELECT_BIN_FILE), REQ_SELECT);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtils.show(getActivity(), "Please install a File Manager.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_SELECT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    String path = getPath(getActivity(), uri);
                    onUpdateSelect(path, false);
                }
                break;
        }
    }

    private String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            try {
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        return cursor.getString(index);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private void onUpdateSelect(String path, boolean formAssets) {
        if (mListener != null && path != null) {
            mListener.onUpdateSelect(path, formAssets);
        }
        if (path != null) {
            dismiss();
        }
    }

    public interface OnUpdateSelectListener {
        void onUpdateSelect(String filepath, boolean formAssets);
    }

}
