package com.smart.android.uploadqueue;

import android.text.TextUtils;

import com.smart.android.uploadqueue.net.QiniuToken;
import com.smart.android.uploadqueue.net.retrofit.Api;
import com.smart.android.uploadqueue.net.retrofit.StandResponse;
import com.smart.android.utils.qiniu.QiniuUploader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author wangxin
 * @since 2017/09/25
 */
class TransactionManager {


    private final List<FileModel> list = new ArrayList<>();
    private final List<FileModel> pendingList = new LinkedList<>();
    private final List<FileModel> completedList = new LinkedList<>();
    private volatile boolean lock = false;


    private OnDataChangedListener mListener;

    private int count;

    public TransactionManager(int count) {
        this.count = count;
    }

    void add(FileModel traffic) {
        if (lock || list.size() == count) {
            synchronized (pendingList) {
                pendingList.add(traffic);
            }
        } else {
            list.add(traffic);
        }
    }

    void add(List<FileModel> traffic) {
        if (lock || list.size() == count) {
            synchronized (pendingList) {
                pendingList.addAll(traffic);
            }
        } else {
            // list.size never bigger than count.
            int capacityRemain = count - list.size();
            if (traffic.size() <= capacityRemain) {// 容量充足
                list.addAll(traffic);
            } else {
                list.addAll(traffic.subList(0, capacityRemain));
                synchronized (pendingList) {
                    pendingList.addAll(traffic.subList(capacityRemain, traffic.size()));
                }
            }
        }
    }

    // AnyThread
    void start() {
        boolean flag = false;
        if (!lock) {
            synchronized (this) {
                if (!lock) {
                    lock = true;
                    flag = true;
                }
            }
        }
        if (!flag) return;

        try {
            // TODO 如何监听上传完成或删除的事件再添加新的任务进入工作线程
            //noinspection unchecked
//            listener.onTransaction(list); // 异步？
            for (final FileModel model : list) {
                doUpload(model);

            }


        } finally {
            lock = false;
        }
    }


    private void doUpload(final FileModel model) {
        Api.getInstance().getQiniuToken()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<StandResponse<QiniuToken>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(StandResponse<QiniuToken> response) {
                        String uptoken = response.getData().getUptoken();
                        model.setStatus(FileStatus.UPLOADING);
                        model.setStartTime(System.currentTimeMillis());
                        QiniuUploader.DEFAULT.put(
                                new File(model.getLocalPath()),
                                model.getKey(),
                                uptoken,
                                new QiniuUploader.UpCompletionHandler() {
                                    @Override
                                    public void completed(String key) {
                                        model.setUrl(String.format(
                                                "%s/%s",
                                                "https://oss.zhihanyun.com",
                                                key));
                                        model.setStatus(FileStatus.COMPLETED);
                                        makeData(model);
                                    }
                                },
                                new QiniuUploader.UploadProgressHandler() {
                                    @Override
                                    public void progress(double percent) {
                                        long now = System.currentTimeMillis();
                                        long delta = now - model.getStartTime();
                                        double speed = model.getFileSize() * percent / (delta / 1000.0);
                                        model.setSpeed(speed);
                                        model.setProgress(percent);
                                        makeData(model);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        model.setStatus(FileStatus.FAILED);
                        makeData(model);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    // TODO 暂停/继续上传/删除操作
    synchronized void notify(String key, FileModel fileModel) {
        // 某个文件上传完毕 或 被暂停 或 上传失败
        list.add(pendingList.remove(0));

        int status = fileModel.getStatus();
        if (status == FileStatus.PAUSED) {

        }
    }

    public void setOnDataChangedListener(OnDataChangedListener l) {
        this.mListener = l;
    }


    private synchronized void makeData(FileModel fileModel) {
        int status = fileModel.getStatus();
        if (status == FileStatus.COMPLETED) {// 下载完成
            completedList.add(0, fileModel);
            Iterator<FileModel> iterator = list.iterator();
            while (iterator.hasNext()) {
                FileModel next = iterator.next();
                if (TextUtils.equals(next.getKey(), fileModel.getKey())) {
                    iterator.remove();
                    if (pendingList.size() > 0) {
                        FileModel remove = pendingList.remove(0);
                        list.add(remove);
                        doUpload(remove);
                    } else {
                        // 没有更多的需要上传的文件
                    }
                    break;
                }
            }
        } else if (status == FileStatus.UPLOADING) {// 来更新进度的
            for (int i = 0; i < list.size(); i++) {
                FileModel model = list.get(i);
                if (TextUtils.equals(model.getKey(), fileModel.getKey())) {
                    list.set(i, fileModel);
                    break;
                }
            }
        }
        List<FileModel> result = new ArrayList<>();
        result.addAll(list);
        result.addAll(pendingList);
        mListener.onDataChanged(result, completedList);
    }
}