package com.zhihan.android.uplaod.sample;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zhihan.android.upload.UploadSdk;
import com.zhihan.android.upload.FileModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends PickerActivity {

    RecyclerView mRvPending;
    List<FileModel> mPendingFiles = new ArrayList<>();
    PendingAdapter mPendingAdapter;
    RecyclerView mCompleted;

    private ViewHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goOneFile();

        handler = new ViewHandler(this);
        UploadSdk.init(getApplication(), true)
                .setOnDataUpdateListener(fileModelList -> {
                    mPendingFiles = fileModelList;
                    handler.removeMessages(0x123);
                    handler.sendEmptyMessageDelayed(0x123, 10);
                });

        // http://pre.api.iotrack.cn/organize/app/
        // http://pre.api.iotrack.cn/organize/app/qiniu/uptoken

        findViewById(R.id.pick).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageInfo.REQUESTED_PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageInfo.REQUESTED_PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, 0x123);
            } else {
                pick();
            }
        });


        mPendingAdapter = new PendingAdapter(mPendingFiles, new PendingAdapter.OnOperationListener() {
            @Override
            public void onDelete(View v, FileModel model, int position) {
                //  删除
                UploadSdk.remove(model);
            }

            @Override
            public void onPause(View v, FileModel model, int position) {
                //  暂停
                UploadSdk.pause(model);
            }

            @Override
            public void onResume(View v, FileModel model, int position) {
                // 继续上传
                UploadSdk.resume(model);
            }
        });
        mRvPending = findViewById(R.id.rv_uploading);
        mRvPending.setAdapter(mPendingAdapter);
        mRvPending.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void pick() {
        mPicturePicker = new PicturePicker();
        mPicturePicker.setOnTakeFinishListener(images -> {
            //  执行上传任务
            UploadSdk.enqueue(images);
        });
        mPicturePicker.openGallery(this, false);
    }


    private void goOneFile() {
        findViewById(R.id.one).setOnClickListener((v) -> OneFileActivity.start(this));
    }


    public void updateList() {
        mPendingAdapter.setNewData(mPendingFiles);
    }
}

class ViewHandler extends Handler {

    WeakReference<MainActivity> mActivity;

    public ViewHandler(MainActivity activity) {
        this.mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0x123:
                if (mActivity == null) return;
                MainActivity activity = mActivity.get();
                if (activity == null || activity.isFinishing()) return;
                activity.updateList();
                break;
        }
    }
}

