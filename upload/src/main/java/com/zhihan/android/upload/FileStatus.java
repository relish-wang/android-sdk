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
 *
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({REMOVE, FAILED, PENDING, COMPLETED, UPLOADING, PAUSED})
public @interface FileStatus {
    /** 被移除 */
    int REMOVE = -2;
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
