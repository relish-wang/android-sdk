package com.zhihan.android.upload;

import java.util.Comparator;

/**
 * 文件排序规则
 *
 * @author wangxin
 * @since 20190709
 */
public interface FileComparator extends Comparator<FileModel> {

    /**
     * 默认文件排序规则
     * <p>
     * 1 上传状态排序: 上传中->等待上传->暂停中->上传失败->已删除->上传完成
     * 2 时间戳排序(operationTime): 越早(小)越前
     */
    FileComparator DEFAULT = (o1, o2) -> {
        @FileStatus int status = o1.getStatus();
        int compare = -Integer.compare(status, o2.getStatus());
        if (compare == 0) {
            int compareTime = Long.compare(o1.getOperationTime(), o2.getOperationTime());
            if (status == FileStatus.COMPLETED) {
                return -compareTime;
            }
            return compareTime;
        } else {
            return compare;
        }
    };

    @Override
    int compare(FileModel o1, FileModel o2);
}
