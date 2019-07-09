package com.smart.android.uploadqueue;

import com.zhihan.android.upload.FileModel;

import java.util.List;

/**
 * @author wangxin
 * @since 20190705
 */
public interface OnProgressUpdateListener {

    void onUploadCompleted(List<FileModel> models);
}
