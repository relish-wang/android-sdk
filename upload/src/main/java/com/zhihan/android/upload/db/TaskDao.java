package com.zhihan.android.upload.db;

import android.support.annotation.Nullable;

import com.zhihan.android.upload.FileModel;

import java.util.List;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190708
 */
public interface TaskDao {

    /**
     * 进入批量上传页面需要先加载所有历史任务
     *
     * @param userId 当前登录用户的ID
     * @return 所有上传任务(包含上传完成的)
     */
    List<FileModel> findAllTask(long userId);

    /**
     * 暂停(为了断点续传而存库)
     *
     * @param fileModel 文件
     * @return 保存成功与否
     */
    boolean pause(FileModel fileModel);

    /**
     * 开始(断点续传)
     *
     * @param md5Key 文件本地地址的MD5值
     * @return 如果是空, 说明这个文件没有缓存, 直接上传就行
     */
    @Nullable
    FileModel resume(String md5Key);

}
