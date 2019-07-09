package com.zhihan.android.upload;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能如下:
 * 1 <strong>添加</strong>上传文件(进入任务队列)<p>
 * 2 <strong>暂停/继续</strong>上传<p>
 * 3 <strong>全部暂停/上传</strong><p>
 * 4 <strong>删除</strong>某个上传任务<p>
 * 5 <strong>清空</strong>已完成的任务<p>
 *
 * @author wangxin
 * @since 20190706
 */
public class UploadSdk {
    private static final String TAG = "UploadSdk";

    private static UploadSdkConfig sConfig;

    /**
     * 多次初始化会覆盖初始化参数
     *
     * @param isDebug 是否是测试环境(正式环境会关闭日志打印)
     * @return UploadSdkConfig.Editor
     */
    public static UploadSdkConfig.Editor init(boolean isDebug) {
        sConfig = new UploadSdkConfig(isDebug);
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
    @SuppressWarnings("unused")
    public static void upload(String localPath) {
        enqueueInternal(Collections.singletonList(FileModel.newLocal(localPath)));
    }

    /**
     * 1.1.1 单文件上传带回调
     *
     * @param localPath 文件本地路径
     * @param l         回调
     */
    public static void upload(String localPath, @NonNull OnSingleFileListener l) {
        Scheduler.uploadSingleFile(FileModel.newLocal(localPath), l);
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
     * 2.1 暂停上传
     */
    public static void pause(FileModel model) {
        @FileStatus int status = model.getStatus();
        if (status != FileStatus.UPLOADING) {
            Utils.Log.i(TAG, "==> 只有上传中的文件才可以暂停");
            return;
        }
        model.setStatus(FileStatus.PAUSED);
        Scheduler.updateFileModel(model);
    }


    /**
     * 2.2.1 单文件继续上传
     *
     * @param model 文件
     * @param l     监听器
     */
    public static void resume(FileModel model, OnSingleFileListener l) {
        @FileStatus int status = model.getStatus();
        if (status != FileStatus.PAUSED && status != FileStatus.FAILED) {
            Utils.Log.i(TAG, "==> 只有*暂停中*或*上传失败*的文件才可以继续上传");
            return;
        }
        model.setStatus(FileStatus.PENDING);
        Scheduler.uploadSingleFile(model, l);
    }

    /**
     * 2.2.2 多文件的继续上传
     * <p>
     * 单文件的文件继续上传无法使用此方法->实际执行了上传, 但无法触发回调。
     */
    public static void resume(FileModel model) {
        @FileStatus int status = model.getStatus();
        if (status != FileStatus.PAUSED && status != FileStatus.FAILED) {
            Utils.Log.i(TAG, "==> 只有*暂停中*或*上传失败*的文件才可以继续上传");
            return;
        }
        model.setStatus(FileStatus.PENDING);
        Scheduler.updateFileModel(model);
    }

    /**
     * 3.1 全部暂停上传
     * 断点续传保存节点
     */
    public static void pauseAll() {
        Scheduler.pauseAll();
    }

    /**
     * 3.2 全部继续上传
     * 根据上传核定线程数开启前两个任务
     */
    public static void resumeAll() {
        Scheduler.resumeAll();
    }

    /**
     * 4 删除上传任务
     */
    public static void remove(@Nullable FileModel fileModel) {
        if (fileModel == null) {
            Utils.Log.e(TAG, "删除的上传任务文件不得为null!!!");
            return;
        }
        // 根据key查找具体任务
        fileModel.setStatus(FileStatus.REMOVE);
        Scheduler.updateFileModel(fileModel);
    }

    /**
     * 5 清空已完成任务
     */
    public static void clearCompleted() {
        Scheduler.clearCompleted();
    }


    /**
     * 添加任务进度/状态回调的监听器
     *
     * @param l 监听器
     */
    public static void addOnDataUpdateListener(OnDataUpdateListener l) {
        sConfig.addOnDataUpdateListener(l);
    }

    /**
     * 移除任务进度/状态回调的监听器(防止内存泄漏)
     * <p>
     * 建议放在{@link Activity#onDestroy()}或{@link Fragment#onDestroy()}
     *
     * @param l 监听器
     */
    @SuppressWarnings("JavadocReference")
    public static void removeOnDataUpdateListener(OnDataUpdateListener l) {
        sConfig.removeOnDataUpdateListener(l);
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
            Scheduler.execute(fileModel);
        }
    }
}
