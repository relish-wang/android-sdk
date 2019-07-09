package com.zhihan.android.upload;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxin
 * @since 20190706
 */
public class UploadSdkConfig {

    public UploadSdkConfig(boolean isDebug) {
        mIsDebug = isDebug;
    }

    public final class Editor {

        public Editor setBaseUrl(String baseUrl) {
            if (TextUtils.isEmpty(baseUrl)) {
                mBaseUrl = baseUrl;
            }
            return this;
        }

        public Editor setThreadCount(int threadCount) {
            if (mThreadCount < 1) {
                throw new IllegalArgumentException("并发任务数至少为1个: " + threadCount);
            } else if (mThreadCount > 6) {
                throw new IllegalArgumentException("并发任务数最多为6个: " + threadCount);
            }
            mThreadCount = threadCount;
            return this;
        }

        public Editor setSeparator(Separator separator) {
            mSeparator = separator;
            return this;
        }

        public Editor setFileComparator(FileComparator comparator) {
            mFileComparator = comparator;
            return this;
        }
    }

    private static final String DEFAULT_DEV_BASE_URL = "http://pre.api.ececloud.cn/";

    // 必填
    private boolean mIsDebug = false;
    // 非必填
    private String mBaseUrl = "http://api.ececloud.cn/";
    private int mThreadCount = 2;// 默认2个任务
    private List<OnDataUpdateListener> mOnDataUpdateListeners;
    private Separator mSeparator = Separator.DEFAULT;
    private FileComparator mFileComparator = FileComparator.DEFAULT;

    /* package */ UploadSdkConfig() {
    }

    public String getBaseUrl() {
        if (mIsDebug) {
            return DEFAULT_DEV_BASE_URL;
        }
        return mBaseUrl;
    }

    public int getThreadCount() {
        return mThreadCount;
    }

    public boolean isDebug() {
        return mIsDebug;
    }

    void addOnDataUpdateListener(OnDataUpdateListener l) {
        if (mOnDataUpdateListeners == null) {
            mOnDataUpdateListeners = new ArrayList<>();
        }
        mOnDataUpdateListeners.add(l);
    }

    public void removeOnDataUpdateListener(OnDataUpdateListener l) {
        mOnDataUpdateListeners.remove(l);
    }

    public List<OnDataUpdateListener> getOnDataUpdateListeners() {
        return mOnDataUpdateListeners;
    }

    public Separator getSeparator() {
        return mSeparator;
    }

    public FileComparator getFileComparator() {
        return mFileComparator;
    }

    /* package */ Editor editor() {
        return new Editor();
    }
}
