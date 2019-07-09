package com.zhihan.android.upload;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxin
 * @since 20190709
 */
public interface Separator {
    /**
     * 默认分离器
     * upload: 上传中(包含等待上传、上传失败、正在上传、暂停中)
     * completed: 已完成
     */
    Separator DEFAULT = fileModels -> {
        List<FileModel> upload = new ArrayList<>();
        List<FileModel> completed = new ArrayList<>();
        for (FileModel next : fileModels) {
            if (next.getStatus() == FileStatus.COMPLETED) {
                completed.add(next);
            } else {
                upload.add(next);
            }
        }
        Map<String, List<FileModel>> map = new HashMap<>();
        map.put("upload", upload);
        map.put("completed", completed);
        Utils.Log.d("upload = " + upload.size());
        Utils.Log.d("completed = " + completed.size());
        return map;
    };

    /**
     * 将文件根据文件状态分离
     *
     * @param fileModels 文件任务
     * @return 分离开的文件任务
     * @see FileStatus
     */
    @NonNull
    Map<String, List<FileModel>> separate(List<FileModel> fileModels);
}
