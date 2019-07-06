package com.smart.android.uploadqueue.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.smart.android.imagepickerlib.ImagePicker;
import com.smart.android.imagepickerlib.loader.ImageLoader;
import com.smart.android.imagepickerlib.view.CropImageView;
import com.zhihan.android.upload.UploadSdk;
import com.zhihan.android.upload.bean.FileModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRvPending;
    List<FileModel> mPendingFiles = new ArrayList<>();
    PendingAdapter mPendingAdapter;
    RecyclerView mCompleted;


    ImageView iv;
    TextView tvName;
    TextView tvProgress;
    TextView tvSpeed;
    TextView tvSize;
    Button btnDel;
    Button btnPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UploadSdk.init(getApplication(), true);

        initImagePicker();
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
                // TODO 删除
//                FileRecorder
            }

            @Override
            public void onPause(View v, FileModel model, int position) {
                // TODO 暂停
            }

            @Override
            public void onResume(View v, FileModel model, int position) {
                // TODO 继续上传
            }
        });
        mRvPending = findViewById(R.id.rv_uploading);
        mRvPending.setAdapter(mPendingAdapter);

        initTestItem();
    }

    private void initTestItem() {
        iv = findViewById(R.id.iv);
        tvName = findViewById(R.id.tv_name);
        tvProgress = findViewById(R.id.tv_progress);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSize = findViewById(R.id.tv_size);
        btnDel = findViewById(R.id.btn_del);
        btnPause = findViewById(R.id.btn_pause_or_resume);

    }

    PicturePicker picturePicker;

    private void pick() {

        picturePicker = new PicturePicker();
        picturePicker.setOnTakeFinishListener(images -> {
            Toast.makeText(MainActivity.this, Arrays.toString(images.toArray()), Toast.LENGTH_SHORT).show();
            // TODO 执行上传任务
        });
        picturePicker.openGallery(this, false);
    }


    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(
                    Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(activity)
                        .load(path)
                        .into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //设置图片加载器
        imagePicker.setShowCamera(false);  //显示拍照按钮
        imagePicker.setCrop(false);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        picturePicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) return;
        boolean allGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED && requestCode == 0x123) {
                Toast.makeText(this, "滚犊子", Toast.LENGTH_SHORT).show();
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            pick();
        }
    }


}

