package com.zhihan.android.upload.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.smart.android.widget.NoScrollListView;
import com.zhihan.android.upload.FileModel;
import com.zhihan.android.upload.OnDataUpdateListener;
import com.zhihan.android.upload.UploadSdk;
import com.zhihan.android.upload.sample.other.PendingAdapter;
import com.zhihan.android.upload.sample.other.PickerActivity;
import com.zhihan.android.upload.sample.other.PicturePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiFileActivity extends PickerActivity implements OnDataUpdateListener {

    TextView mPauseAll;
    TextView mTvUploading;
    NoScrollListView mRvPending;
    List<FileModel> mPendingFiles = new ArrayList<>();
    PendingAdapter mPendingAdapter;

    TextView mTvCompleted;
    View mEmpty;
    List<FileModel> mCompletedFiles = new ArrayList<>();
    NoScrollListView mRvCompleted;
    PendingAdapter mCompletedAdapter;

    boolean isAllPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_file);

        UploadSdk.init(BuildConfig.DEBUG);

        UploadSdk.addOnDataUpdateListener(this);

        findViewById(R.id.pick).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MultiFileActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageInfo.REQUESTED_PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MultiFileActivity.this,
                    Manifest.permission.CAMERA) != PackageInfo.REQUESTED_PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, 0x123);
            } else {
                pick();
            }
        });

        mTvUploading = findViewById(R.id.tv_uploading);
        mTvUploading.setText("上传中(0)");
        mPauseAll = findViewById(R.id.pause_all);
        mPauseAll.setText("全部暂停");
        mTvCompleted = findViewById(R.id.tv_completed);
        mTvCompleted.setText("已完成(0)");
        mEmpty = findViewById(R.id.remove_completed);

        mPauseAll.setOnClickListener(v -> {
            if (isAllPaused) {
                UploadSdk.resumeAll();
            } else {
                UploadSdk.pauseAll();
            }
        });
        mEmpty.setOnClickListener(v -> {
            UploadSdk.clearCompleted();
        });


        mPendingAdapter = new PendingAdapter(mPendingFiles,
                new PendingAdapter.OnOperationListener() {
                    @Override
                    public void onDelete(View v, FileModel model, int position) {
                        //  删除
                        UploadSdk.remove(model);
                    }

                    @Override
                    public void onPause(View v, FileModel model, int position) {
                        //  暂停, 其实是取消
                        UploadSdk.pause(model);
                    }

                    @Override
                    public void onResume(View v, FileModel model, int position) {
                        // 继续上传, 单文件继续上传无法接收回调了
                        UploadSdk.resume(model);
                    }
                });
        mRvPending = findViewById(R.id.rv_uploading);
        mRvPending.setAdapter(mPendingAdapter);


        mCompletedAdapter = new PendingAdapter(mCompletedFiles,
                new PendingAdapter.OnOperationAdapter() {
                    @Override
                    public void onDelete(View v, FileModel model, int position) {
                        //  删除
                        UploadSdk.remove(model);
                    }
                });
        mRvCompleted = findViewById(R.id.rv_completed);
        mRvCompleted.setAdapter(mCompletedAdapter);

        goOneFile();
    }

    @Override
    protected void pick() {
        mPicturePicker = new PicturePicker();
        //  执行上传任务
        mPicturePicker.setOnTakeFinishListener(UploadSdk::enqueue);
        mPicturePicker.openGallery(this, false);
    }

    @SuppressLint("SetTextI18n")
    public void updateList() {
        mTvUploading.setText("上传中(" + mPendingFiles.size() + ")");
        mPauseAll.setText(isAllPaused ? "全部上传" : "全部暂停");
        mPendingAdapter.setNewData(mPendingFiles);
        mTvCompleted.setText("已完成(" + mCompletedFiles.size() + ")");
        mCompletedAdapter.setNewData(mCompletedFiles);
    }

    private void goOneFile() {
        findViewById(R.id.one).setOnClickListener((v) -> SingleFileActivity.start(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除监听器
        UploadSdk.removeOnDataUpdateListener(this);
    }

    @Override
    public void onDataUpdate(
            @Nullable Boolean isAllPaused,
            @NonNull Map<String, List<FileModel>> map) {
        if (isAllPaused != null) {
            this.isAllPaused = isAllPaused;
        }
        mCompletedFiles = map.get("completed");
        mPendingFiles = map.get("upload");
        updateList();
    }
}

