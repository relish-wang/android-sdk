package com.zhihan.android.upload;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zhihan.android.upload.FileStatus.COMPLETED;
import static com.zhihan.android.upload.FileStatus.FAILED;
import static com.zhihan.android.upload.FileStatus.PAUSED;
import static com.zhihan.android.upload.FileStatus.PENDING;
import static com.zhihan.android.upload.FileStatus.REMOVE;
import static com.zhihan.android.upload.FileStatus.UPLOADING;

/**
 * 文件的上传状态
 * <p>
 * 它们的大小刚好是默认排序规则
 *
 * @author wangxin
 * @since 20190705
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({REMOVE, FAILED, PENDING, COMPLETED, UPLOADING, PAUSED})
public @interface FileStatus {
    /** 上传中 */
    int UPLOADING = 6;
    /** 等待中 */
    int PENDING = 5;
    /** 暂停中 */
    int PAUSED = 4;
    /** 上传失败 */
    int FAILED = 3;
    /** 上传成功 */
    int COMPLETED = 2;
    /** 被移除 */
    int REMOVE = 1;
}
