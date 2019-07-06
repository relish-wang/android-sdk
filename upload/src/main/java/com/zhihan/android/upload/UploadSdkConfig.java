package com.zhihan.android.upload;

import android.app.Application;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190706
 */
public class UploadSdkConfig {

    public UploadSdkConfig(Application app, boolean isDebug) {
        this.mApp = app;
        mIsDebug = isDebug;
    }

    public final class Editor {

        public Editor setBaseUrl(String payAlias) {
            mBaseUrl = payAlias == null ? "" : payAlias;
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
    }

    // 必填
    private Application mApp;
    private boolean mIsDebug;
    // 非必填
    private String mBaseUrl = "http://pre.api.iotrack.cn/";
    private int mThreadCount = 2;// 默认2个任务

    /* package */ UploadSdkConfig() {
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public int getThreadCount() {
        return mThreadCount;
    }

    public Application getApp() {
        return mApp;
    }

    public boolean isDebug() {
        return mIsDebug;
    }

    /* package */ Editor editor() {
        return new Editor();
    }
}
