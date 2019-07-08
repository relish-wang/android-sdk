package com.zhihan.android.upload;

import android.app.Application;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1 <strong>添加</strong>上传文件(进入任务队列)<p>
 * 2 <strong>暂停/继续</strong>上传<p>
 * 3 <strong>全部暂停/上传</strong><p>
 * 4 <strong>删除</strong>某个上传任务<p>
 * 5 <strong>清空</strong>已完成的任务<p>
 *
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190706
 */
public class UploadSdk {
    private static final String TAG = "UploadSdk";

    private static UploadSdkConfig sConfig;

    public static UploadSdkConfig.Editor init(Application app, boolean isDebug) {
        sConfig = new UploadSdkConfig(app, isDebug);
        return sConfig.editor();
    }

    public static UploadSdkConfig getConfig() {
        return sConfig;
    }

    private UploadSdk() {
        throw new UnsupportedOperationException();
    }

    /////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// 公开的可调用的功能 ////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * 1.1 添加上传任务
     *
     * @param localPath 文件本地地址
     */
    public static void enqueue(String localPath) {
        enqueueInternal(Collections.singletonList(FileModel.newLocal(localPath)));
    }

    /**
     * 1.2 添加上传任务
     *
     * @param localPaths 文件们的本地地址
     */
    public static void enqueue(List<String> localPaths) {
        List<FileModel> fileModels = new ArrayList<>();
        for (String localPath : localPaths) {
            fileModels.add(FileModel.newLocal(localPath));
        }
        enqueueInternal(fileModels);
    }

    /**
     * 2.1 TODO 暂停上传
     */
    public static void pause(FileModel model) {
        @FileStatus int status = model.getStatus();
        if (status != FileStatus.UPLOADING) {
            Utils.Log.i(TAG, "==> 只有上传中的文件才可以暂停");
            return;
        }
        //
    }

    /**
     * 2.2 TODO 继续上传
     */
    public static void resume(FileModel model) {
        @FileStatus int status = model.getStatus();
        if (status != FileStatus.PAUSED) {
            Utils.Log.i(TAG, "==> 只有暂停中的文件才可以继续上传");
            return;
        }
        //
    }

    /**
     * 3.1 TODO 全部暂停上传
     * 断点续传保存节点
     */
    public static void pauseAll() {

    }

    /**
     * 3.2 TODO 全部继续上传
     * 根据上传核定线程数开启前两个任务
     */
    public static void resumeAll() {

    }

    /**
     * 4 TODO 删除上传任务
     */
    public static void remove(@Nullable FileModel fileModel) {
        if (fileModel == null) {
            Utils.Log.e(TAG, "删除的上传任务文件不得为null!!!");
            return;
        }
        final String key = fileModel.getKey();
        // 根据key查找具体任务
    }

    /**
     * 5 TODO 清空已完成任务
     */
    public static void clearCompleted() {

    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// Internal Core Method ////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * 真正执行入队任务的方法
     *
     * @param fileModels 待上传的文件
     */
    private static synchronized void enqueueInternal(List<FileModel> fileModels) {
        for (FileModel fileModel : fileModels) {
            Manager.execute(fileModel);
        }
    }
}
