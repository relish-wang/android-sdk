package com.zhihan.android.upload.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.zhihan.android.upload.FileModel;
import com.zhihan.android.upload.OnSingleFileListener;
import com.zhihan.android.upload.UploadSdk;
import com.zhihan.android.upload.sample.other.PendingAdapter;
import com.zhihan.android.upload.sample.other.PickerActivity;
import com.zhihan.android.upload.sample.other.PicturePicker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author wangxin
 * @since 20190708
 */
public class SingleFileActivity extends PickerActivity implements OnSingleFileListener {


    public static void start(Context context) {
        Intent intent = new Intent(context, SingleFileActivity.class);
        context.startActivity(intent);
    }

    View mRoot;
    PendingAdapter mAdapter;
    PendingAdapter.ViewHolder mHolder;
    FileModel model;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_file);
        mRoot = findViewById(R.id.ll);

        mAdapter = new PendingAdapter(new ArrayList<>(), new PendingAdapter.OnOperationListener() {
            @Override
            public void onDelete(View v, FileModel model, int position) {
                UploadSdk.remove(model);
            }

            @Override
            public void onPause(View v, FileModel model, int position) {
                UploadSdk.pause(model);
            }

            @Override
            public void onResume(View v, FileModel model, int position) {
                UploadSdk.resume(model, SingleFileActivity.this);
            }
        });
        mHolder = new PendingAdapter.ViewHolder(mRoot);

        initView();
    }

    private void initView() {

        findViewById(R.id.btn_pick).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(SingleFileActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageInfo.REQUESTED_PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(SingleFileActivity.this,
                    Manifest.permission.CAMERA) != PackageInfo.REQUESTED_PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, 0x123);
            } else {
                pick();
            }
        });
    }


    @Override
    protected void pick() {
        mPicturePicker = new PicturePicker();
        mPicturePicker.setOnTakeFinishListener(images -> {
            //  执行上传任务
            if (images.size() == 0) return;
            String s = images.get(0);
            UploadSdk.upload(s, this);
        });
        mPicturePicker.openGallery(this, false);
    }

    private void update() {
        if (isFinishing()) return;
        mAdapter.setNewData(model == null ? new ArrayList<>() : Collections.singletonList(model));
        if (model != null) {
            mAdapter.onBindViewHolder(mHolder, 0);
        }
    }

    @Override
    public void onDataUpdate(@NonNull FileModel single) {
        model = single;
        update();
    }
}
