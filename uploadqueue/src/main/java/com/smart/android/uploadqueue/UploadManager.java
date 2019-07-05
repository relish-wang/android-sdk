package com.smart.android.uploadqueue;

import com.smart.android.uploadqueue.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public class UploadManager {

    private ThreadPool mPool;
    private TransactionManager mTransactionManager;

    private String baseUrl;


    public static UploadManager sManager;

    public static UploadManager getInstance(int count, String baseUrl, OnDataChangedListener l) {
        if (sManager == null) {
            sManager = new UploadManager();
            sManager.mPool = new ThreadPool(count);
            sManager.mTransactionManager = new TransactionManager(count);
            sManager.mTransactionManager.setOnDataChangedListener(l);
            sManager.baseUrl = baseUrl;
        }
        return sManager;

    }


    /**
     * 继续添加
     *
     * @param filePaths 本地文件地址
     */
    public void add(List<String> filePaths) {
        List<FileModel> fileModels = new ArrayList<>();
        for (String filePath : filePaths) {
            fileModels.add(FileModel.newLocal(filePath));
        }
        mTransactionManager.add(fileModels);
    }

    /**
     * 本地文件地址
     *
     * @param filePath 本地文件地址
     */
    public void add(String filePath) {
        mTransactionManager.add(FileModel.newLocal(filePath));
    }


    public void start() {
        mTransactionManager.start();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private static boolean isDebug;

    public static void setIsDebug(boolean isDebug) {
        UploadManager.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }
}
