package com.zhihan.android.upload;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * @author wangxin
 * @since 20190708
 */
public interface OnSingleFileListener {

    /**
     * @param single 单文件上传时可使用这个数据更新上传进度
     */
    @MainThread
    void onDataUpdate(@NonNull FileModel single);
}
