package com.smart.android.uploadqueue.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.smart.android.imagepickerlib.ImagePicker;
import com.smart.android.imagepickerlib.bean.ImageItem;
import com.smart.android.imagepickerlib.ui.ImageGridActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hyu on 2018/10/23.
 * Email: fvaryu@163.com
 */
public class PicturePicker {
    public final static int REQUEST_IMAGE_PICKER = 1001;
    public static final String PHOTO_FILE = "takephotofile";
    final static String TAG = "tag";

    public static int max_size = 9;
    public static int limit = 9;//最多一次选择几张


    private OnTakeFinishListener mOnTakeFinishListener;

    public void setOnTakeFinishListener(OnTakeFinishListener onTakeFinishListener) {
        mOnTakeFinishListener = onTakeFinishListener;
    }

    public interface OnTakeFinishListener {
        void onTakeFinish(List<String> images);
    }


    public static boolean checkCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean dolimit(Activity activity, boolean isSigne) {
        if (!checkCameraPermission(activity)) {
            Toast.makeText(activity, "未授权相机权限", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (limit <= 0) {
            Toast.makeText(activity, "最多上传" + max_size + "张图片",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isSigne)
            limit = 1;
        else
            limit = max_size;

        return true;
    }


    /**
     * @param activity 上下文
     * @param fragment 如果在fragment不能为空否则onActivityResult 不会进入
     * @param isSingle 单选
     */
    public void openGallery(
            @NonNull Activity activity, @Nullable Fragment fragment, boolean isSingle) {
        if (!dolimit(activity, isSingle))
            return;

        ImagePicker.getInstance().setSelectLimit(limit);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        if (fragment == null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_PICKER);
        } else {
            fragment.startActivityForResult(intent, REQUEST_IMAGE_PICKER);
        }
    }

    public void openGallery(Activity activity, boolean isSigne) {
        openGallery(activity, null, isSigne);
    }

    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//返回多张照片
                    if (data != null) {
                        //是否发送原图
                        //noinspection unchecked
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>)
                                data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                        List<String> list = new ArrayList<>();
                        for (ImageItem imageItem : images) {
                            list.add(imageItem.path);
                        }

                        if (mOnTakeFinishListener != null) {
                            mOnTakeFinishListener.onTakeFinish(list);
                        }
                    }
                }
                break;
        }
    }
}
