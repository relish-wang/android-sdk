package com.zhihan.android.upload;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 线程池管理
 *
 * @author wangxin
 * @since 20190705
 */
class ThreadPool {

    private final ThreadPoolExecutor mExecutor;

    ThreadPool(int count) {
        mExecutor = new ThreadPoolExecutor(
                count,
                count,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "QINIU_UPLOAD"));
        mExecutor.allowCoreThreadTimeOut(true);
    }

    @SuppressWarnings("UnusedReturnValue")
    final Future<?> execute(Runnable runnable) {
        return mExecutor.submit(new RunnableTask(runnable));
    }

    private static final class RunnableTask implements Runnable {

        private static final Handler HANDLER = new Handler(Looper.getMainLooper());

        private final Runnable mRunnable;

        @SuppressWarnings("ConstantConditions")
        private RunnableTask(@NonNull Runnable runnable) {
            if (runnable == null) throw new IllegalArgumentException("runnable is null!");
            mRunnable = runnable;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (final Throwable t) {
                if (UploadSdk.getConfig().isDebug()) {
                    HANDLER.post(() -> {
                        throw new RuntimeException("Thread throws exception!!!", t);
                    });
                }
            }
        }
    }
}
