package com.zhihan.android.uplaod.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.zhihan.android.upload.FileModel;
import com.zhihan.android.upload.FileStatus;
import com.zhihan.android.upload.Config;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190708
 */
public class OneFileActivity extends PickerActivity {


    public static void start(Context context) {
        Intent intent = new Intent(context, OneFileActivity.class);
        context.startActivity(intent);
    }

    View mRoot;
    PendingAdapter mAdapter;
    PendingAdapter.ViewHolder mHolder;
    FileModel model;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_file);
        mRoot = findViewById(R.id.ll);

        mAdapter = new PendingAdapter(new ArrayList<>(), new PendingAdapter.OnOperationListener() {
            @Override
            public void onDelete(View v, FileModel model, int position) {

            }

            @Override
            public void onPause(View v, FileModel model, int position) {
                isPaused = true;
            }

            @Override
            public void onResume(View v, FileModel model, int position) {
                execute(model.getLocalPath());
                isPaused = false;
            }
        });
        mHolder = new PendingAdapter.ViewHolder(mRoot);

        initView();
    }


    private transient boolean isPaused = false;

    private void initView() {

        findViewById(R.id.btn_pick).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(OneFileActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageInfo.REQUESTED_PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(OneFileActivity.this,
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
            execute(s);
        });
        mPicturePicker.openGallery(this, false);
    }

    private void execute(String localPath) {
        model = FileModel.newLocal(localPath);
        UploadManager mManager = new UploadManager(Config.getInstance());
        mManager.put(
                model.getLocalPath(),
                model.getKey(),
                "DVRkIPodRYU1gxgR2Sg5umivdZxP4Ig9hhW0Nldb:gO8F9pV00m8pMzMD0CrN3_4Dl28=:eyJzY29wZSI6InpoaWhhbiIsImRlYWRsaW5lIjoxNTYyNjYwMzY2fQ==",
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject response) {
                        model.setStatus(FileStatus.COMPLETED);
                        update();
                    }
                }, new UploadOptions(null, "", false, new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        model.setProgress(percent);
                        update();
                    }
                }, new UpCancellationSignal() {
                    @Override
                    public boolean isCancelled() {
                        return isPaused;
                    }
                }));
    }


    private void update() {
        mAdapter.setNewData(Collections.singletonList(model));
        mAdapter.onBindViewHolder(mHolder, 0);
    }
}
