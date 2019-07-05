package com.smart.android.uploadqueue;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.smart.android.uploadqueue.FileStatus.COMPLETED;
import static com.smart.android.uploadqueue.FileStatus.FAILED;
import static com.smart.android.uploadqueue.FileStatus.PAUSED;
import static com.smart.android.uploadqueue.FileStatus.PENDING;
import static com.smart.android.uploadqueue.FileStatus.UPLOADING;

/**
 * 文件的上传状态
 *
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({FAILED, PENDING, COMPLETED, UPLOADING, PAUSED})
public @interface FileStatus {
    /** 上传失败 */
    int FAILED = -1;
    /** 等待中 */
    int PENDING = 0;
    /** 上传成功 */
    int COMPLETED = 1;
    /** 上传中 */
    int UPLOADING = 2;
    /** 暂停中 */
    int PAUSED = 3;
}
