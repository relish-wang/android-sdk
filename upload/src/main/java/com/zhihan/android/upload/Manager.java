package com.zhihan.android.upload;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.zhihan.android.upload.net.QiniuToken;
import com.zhihan.android.upload.net.retrofit.Api;
import com.zhihan.android.upload.net.retrofit.StandResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import retrofit2.Response;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190708
 */
public class Manager {

    private static final ThreadPool mThreadPool = new ThreadPool(UploadSdk.getConfig().getThreadCount());
    private static final UploadManager mManager = new UploadManager(Config.getInstance());
    private static transient String token = "";

    private static transient final LinkedHashMap<String, FileModel> mFiles = new LinkedHashMap<>();

    static synchronized void execute(final FileModel model) {
        // FIXME 如何判断是被执行了还是未被执行
        model.setStatus(FileStatus.UPLOADING);
        mFiles.put(model.getKey(), model);
        notifyInternal();
        mThreadPool.execute(new Runnable() {
            @Override
            public synchronized void run() {
                if (TextUtils.isEmpty(token)) {
                    fetchToken();
                }
                if (TextUtils.isEmpty(token)) {
                    Utils.Log.e("网络错误, 无法获取token");
                    return;
                }
                uploadAsyn();
//                upload();
            }

            private synchronized void fetchToken() {
                Response<StandResponse<QiniuToken>> execute;
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
                StandResponse<QiniuToken> body = execute.body();
                if (body == null) return;
                token = body.getData().getUptoken();
            }

            private transient boolean isRetry = false;

            private synchronized void uploadAsyn() {
                final String key = model.getKey();
                mManager.put(
                        model.getLocalPath(),
                        key,
                        token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(
                                    String key, ResponseInfo info, JSONObject response) {
                                model.setUrl(String.format("https://oss.zhihanyun.com/%s", key));
                                model.setStatus(FileStatus.COMPLETED);
                                updateFileModel(model);
                            }
                        },
                        new UploadOptions(
                                null/*map*/,
                                null,
                                false,
                                new UpProgressHandler() {
                                    @Override
                                    public void progress(String key, double percent) {
                                        // 上传进度更新
                                        model.setProgress(percent);
                                        long operationTime = model.getOperationTime();
                                        long delta = System.currentTimeMillis() - operationTime;
                                        // B / s
                                        double v = model.getFileSize() * percent / (delta / 1000.0);
                                        model.setSpeed(v);
                                        Utils.Log.d("progress = " + percent + " time = " + (delta / 1000) + " speed = " + v + " B/s");
                                        updateFileModel(model);
                                    }
                                }, new UpCancellationSignal() {
                            @Override
                            public boolean isCancelled() {
                                boolean paused = isPaused(key);
                                if (paused) {
                                    model.setStatus(FileStatus.PAUSED);
                                    updateFileModel(model);
                                }
                                return paused || isRemoved(key);
                            }
                        }));
            }

            private synchronized boolean upload() {
                final String key = model.getKey();
                ResponseInfo responseInfo = mManager.syncPut(
                        model.getLocalPath(),
                        key,
                        token,
                        new UploadOptions(
                                null/*map*/,
                                null,
                                false,
                                new UpProgressHandler() {
                                    @Override
                                    public void progress(String key, double percent) {
                                        // 上传进度更新
                                        model.setProgress(percent);
                                        long operationTime = model.getOperationTime();
                                        long delta = System.currentTimeMillis() - operationTime;
                                        // B / s
                                        double v = model.getFileSize() * percent / (delta / 1000.0);
                                        model.setSpeed(v);
                                        Utils.Log.d("progress = " + percent + " speed = " + v + " B/s");
                                        updateFileModel(model);
                                    }
                                }, new UpCancellationSignal() {
                            @Override
                            public boolean isCancelled() {
                                boolean paused = isPaused(key);
                                if (paused) {
                                    model.setStatus(FileStatus.PAUSED);
                                    updateFileModel(model);
                                }
                                return paused || isRemoved(key);
                            }
                        }));
                if (responseInfo.statusCode == ResponseInfo.InvalidToken) {
                    if (isRetry) return false;
                    fetchToken();
                    isRetry = true;
                    return upload();
                }
                if (responseInfo.isNetworkBroken()) {
                    model.setStatus(FileStatus.FAILED);
                    updateFileModel(model);
                    return true;
                } else if (responseInfo.isOK()) {
                    String path = null;
                    try {
                        path = responseInfo.response.getString("key");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        model.setStatus(FileStatus.FAILED);
                        updateFileModel(model);
                        return false;
                    }
                    model.setUrl(String.format("https://oss.zhihanyun.com/%s", path));
                    model.setStatus(FileStatus.COMPLETED);
                    updateFileModel(model);
                    return true;
                }
                return false;
            }
        });
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


    private synchronized static void updateFileModel(FileModel file) {
        if (file == null) return;
        final String key = file.getKey();
        FileModel fileModel = mFiles.get(key);
        if (fileModel == null) return;
        mFiles.put(key, fileModel);
        notifyInternal();
    }


    private synchronized static void notifyInternal() {
        final OnDataUpdateListener listener = UploadSdk.getConfig().getOnDataUpdateListener();
        if (listener != null) {
            final ArrayList<FileModel> list = new ArrayList<>(mFiles.values());
            if (Looper.myLooper() == Looper.getMainLooper()) {
                listener.onDataUpdate(list);
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDataUpdate(list);
                    }
                });
            }
        }
    }
}
