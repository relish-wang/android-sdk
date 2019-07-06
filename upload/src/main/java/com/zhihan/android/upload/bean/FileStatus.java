package com.zhihan.android.upload.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zhihan.android.upload.bean.FileStatus.COMPLETED;
import static com.zhihan.android.upload.bean.FileStatus.FAILED;
import static com.zhihan.android.upload.bean.FileStatus.PAUSED;
import static com.zhihan.android.upload.bean.FileStatus.PENDING;
import static com.zhihan.android.upload.bean.FileStatus.UPLOADING;

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
