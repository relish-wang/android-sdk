package com.zhihan.android.upload.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.zhihan.android.upload.UploadSdk;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public class ThreadPool {
    private final ThreadPoolExecutor mExecutor;

    public ThreadPool(int count) {
        mExecutor = new ThreadPoolExecutor(
                count,
                count,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "QINIU_UPLOAD");
                    }
                });
        mExecutor.allowCoreThreadTimeOut(true);
    }

    public final Future<?> execute(Runnable runnable) {
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
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            throw new RuntimeException("Thread throws exception!!!", t);
                        }
                    });
                }
            }
        }
    }
}
