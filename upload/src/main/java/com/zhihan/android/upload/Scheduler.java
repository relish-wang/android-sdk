package com.zhihan.android.upload;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.zhihan.android.upload.net.Api;
import com.zhihan.android.upload.net.QiniuToken;
import com.zhihan.android.upload.net.StdResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * 上传任务调度管理
 *
 * @author wangxin
 * @since 20190708
 */
class Scheduler {

    /** 线程池管理 */
    private static final ThreadPool mThreadPool =
            new ThreadPool(UploadSdk.getConfig().getThreadCount());
    /** 七牛云上传 */
    private static final UploadManager mManager = new UploadManager(Config.getInstance());
    /** 七牛云上传token */
    private static volatile String mToken = "";

    private static final ConcurrentHashMap<String, FileModel> mFiles =
            new ConcurrentHashMap<>();

    /**
     * 适用于小文件上传
     *
     * @param model 文件任务
     * @param l     监听器
     */
    static synchronized void uploadSingleFile(FileModel model, OnSingleFileListener l) {
        execute(true, model, single -> {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                l.onDataUpdate(single);
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> l.onDataUpdate(single));
            }
        });
    }

    static synchronized void execute(final FileModel model) {
        execute(false, model, single -> updateFileModel(model));
    }

    private static synchronized void execute(
            boolean isSingleFile,
            final FileModel model,
            @NonNull OnSingleFileListener l) {
        mThreadPool.execute(new Runnable() {
            @Override
            public synchronized void run() {
                // 开始执行上传任务
                model.setStatus(FileStatus.UPLOADING);
                model.setOperationTime(System.currentTimeMillis());
                mFiles.put(model.getKey(), model);
                if (!isSingleFile) {
                    notifyInternal(false);
                } else {
                    l.onDataUpdate(model);
                }

                if (TextUtils.isEmpty(mToken)) {
                    // 失效后会重新获取, 不必每次调用
                    fetchToken();
                }
                if (TextUtils.isEmpty(mToken)) {
                    Utils.Log.e("网络错误, 无法获取token");
                    return;
                }
                uploadAsync();
            }

            private volatile boolean isRetry = false;

            private synchronized void uploadAsync() {
                final String key = model.getKey();
                mManager.put(
                        model.getLocalPath(),
                        key,
                        mToken,
                        (key1, info, response) -> {
                            if (info.isOK()) {
                                model.setUrl(String.format("https://oss.zhihanyun.com/%s", key1));
                                model.setStatus(FileStatus.COMPLETED);
                                l.onDataUpdate(model);
                                return;
                            }
                            //noinspection StatementWithEmptyBody
                            if (info.statusCode == ResponseInfo.Cancelled) {
                                // do nothing. 被用户取消了-> 暂停 或 删除
                            } else if (info.statusCode == ResponseInfo.InvalidToken) {
                                // 获取token 再上传(重试一次)
                                if (isRetry) return;
                                isRetry = true;
                                fetchTokenAsync()
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new ObserverAdapter<StdResponse<QiniuToken>>() {
                                            @Override
                                            public void onNext(StdResponse<QiniuToken> t) {
                                                QiniuToken data = t.getData();
                                                if (data == null) return;
                                                mToken = data.getUptoken();
                                                uploadAsync();
                                            }
                                        });
                            } else {
                                if (info.statusCode == ResponseInfo.NetworkError) {
                                    Utils.Log.e("上传图片时, 网络错误");
                                }
                                model.setStatus(FileStatus.FAILED);
                                model.setOperationTime(System.currentTimeMillis());
                                l.onDataUpdate(model);
                                Utils.Log.e("其他未做区分的错误: " +
                                        "[" + info.error + "](" + info.statusCode + ")");
                            }
                        },
                        // 小型单文件上传一般不设置这个监听, 所以无妨
                        new UploadOptions(
                                null/*map*/,
                                null,
                                false,
                                (key12, percent) -> {
                                    // 上传进度更新
                                    model.setProgress(percent);
                                    long operationTime = model.getOperationTime();
                                    long delta = System.currentTimeMillis() - operationTime;
                                    // 单位: B / s
                                    double v = model.getFileSize()
                                            * (percent - model.getOperationTimeProcess())
                                            / (delta / 1000.0);
                                    model.setSpeed(v);
                                    l.onDataUpdate(model);
                                }, () -> {
                            boolean paused = isPaused(key);
                            if (paused) {
                                model.setStatus(FileStatus.PAUSED);
                                model.setOperationTime(System.currentTimeMillis());
                                l.onDataUpdate(model);
                            }
                            return paused || isRemoved(key);
                        }));
            }
        });
    }


    private synchronized static void fetchToken() {
        Response<StdResponse<QiniuToken>> execute;
        try {
            execute = Api.getInstance()
                    .getClientApi()
                    .getQiniuToken()
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.Log.e("网络错误, 无法获取token");
            return;
        }
        StdResponse<QiniuToken> body = execute.body();
        if (body == null) return;
        mToken = body.getData().getUptoken();
    }

    private synchronized static Observable<StdResponse<QiniuToken>> fetchTokenAsync() {
        return Api.getInstance()
                .getClientApi()
                .getQiniuTokenObservable();
    }

    private synchronized static boolean isPaused(String key) {
        FileModel fileModel = mFiles.get(key);
        if (fileModel == null) return false;
        return fileModel.getStatus() == FileStatus.PAUSED;
    }


    private synchronized static boolean isRemoved(String key) {
        FileModel fileModel = mFiles.get(key);
        if (fileModel == null) return false;
        return fileModel.getStatus() == FileStatus.REMOVE;
    }


    /**
     * 通知文件状态改变
     *
     * @param file 改变的文件状态
     */
    synchronized static void updateFileModel(FileModel file) {
        if (file == null) return;
        final String key = file.getKey();
        FileModel fileModel = mFiles.get(key);
        if (fileModel == null) return;
        @FileStatus int status = fileModel.getStatus();
        boolean b = status == FileStatus.PENDING;
        if (b) {
            execute(fileModel);// 继续上传
        }
        mFiles.put(key, fileModel);
        notifyInternal(b || status == FileStatus.UPLOADING ? false : null);
    }

    /**
     * 暂停所有文件的上传任务
     */
    synchronized static void pauseAll() {
        for (FileModel value : mFiles.values()) {
            @FileStatus int status = value.getStatus();
            if (status == FileStatus.UPLOADING) {
                value.setStatus(FileStatus.PAUSED);
                mFiles.put(value.getKey(), value);
            }
        }
        notifyInternal(true);
    }

    /**
     * 继续所有文件的上传任务
     */
    synchronized static void resumeAll() {
        for (FileModel value : mFiles.values()) {
            @FileStatus int status = value.getStatus();
            if (status == FileStatus.PAUSED || status == FileStatus.FAILED) {
                value.setStatus(FileStatus.PENDING);
                mFiles.put(value.getKey(), value);
                execute(value);
            }
        }
        notifyInternal(false);
    }

    /**
     * 删除所有已上传的文件
     */
    synchronized static void clearCompleted() {
        boolean isAllPaused = true;
        for (FileModel value : mFiles.values()) {
            @FileStatus int status = value.getStatus();
            if (status == FileStatus.COMPLETED) {
                value.setStatus(FileStatus.REMOVE);
                mFiles.put(value.getKey(), value);
            }
            if (isAllPaused && (status == FileStatus.PENDING || status == FileStatus.UPLOADING)) {
                isAllPaused = false;
            }
        }
        notifyInternal(isAllPaused);
    }

    /**
     * 真正最后通知到开发者的方法
     */
    private synchronized static void notifyInternal(Boolean isAllPaused) {
        synchronized (mFiles) {
            List<OnDataUpdateListener> listeners = UploadSdk.getConfig().getOnDataUpdateListeners();
            if (listeners == null || listeners.isEmpty()) return;

            Iterator<FileModel> iterator = mFiles.values().iterator();
            while (iterator.hasNext()) {
                FileModel next = iterator.next();
                if (next.getStatus() == FileStatus.REMOVE) {
                    iterator.remove();
                }
            }
            final ArrayList<FileModel> list = new ArrayList<>(mFiles.values());
            Utils.Log.d("all = " + list.size());
            Collections.sort(list, UploadSdk.getConfig().getFileComparator());
            Map<String, List<FileModel>> result =
                    UploadSdk.getConfig().getSeparator().separate(list);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                for (OnDataUpdateListener l : listeners) {
                    l.onDataUpdate(isAllPaused, result);
                }
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    for (OnDataUpdateListener l : listeners) {
                        l.onDataUpdate(isAllPaused, result);
                    }
                });
            }
        }
    }
}
