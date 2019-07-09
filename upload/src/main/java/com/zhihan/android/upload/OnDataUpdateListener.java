package com.zhihan.android.upload;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author wangxin
 * @since 20190708
 */
public interface OnDataUpdateListener {

    /**
     * @param isAllPaused 是否全部暂停了
     *                    (true: 已全部暂停;false: 并非全部暂停; null: 不确定是否全部暂停(维持现状即可))
     * @param map         分类任务列表(默认的分类器会将已完成的分类在"completed"中，其他的分类在"upload"中)
     *                    也可以在初始化UploadSdk的时候自定义分类器
     * @see Separator 分类器
     */
    @MainThread
    void onDataUpdate(
            @Nullable Boolean isAllPaused,
            @NonNull Map<String, List<FileModel>> map
    );
}
