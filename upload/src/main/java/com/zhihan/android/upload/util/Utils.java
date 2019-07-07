package com.zhihan.android.upload.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public final class Utils {


    /**
     * 获取文件名
     *
     * @param localPath 文件的本地地址
     * @return 不含路径的文件名
     */
    public static String getFileSimpleName(@NonNull String localPath) {
        if (TextUtils.isEmpty(localPath)) return localPath;
        final int lastIndexOfPathSeparator = localPath.lastIndexOf("/");
        if (lastIndexOfPathSeparator == -1 // 没找到
                || lastIndexOfPathSeparator == localPath.length() - 1) { // 最后一位是斜杆
            return localPath;
        }
        return localPath.substring(lastIndexOfPathSeparator);
    }

    /**
     * 获取文件大小(单位: B)
     *
     * @param localPath 文件的本地地址
     * @return 文件大小
     */
    public static long getFileSize(@NonNull String localPath) {
        if (TextUtils.isEmpty(localPath)) return 0;
        File file = new File(localPath);
        if (!file.exists()) return 0;
        return file.length();
    }
}
