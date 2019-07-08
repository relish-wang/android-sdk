package com.zhihan.android.upload.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190706
 */
public interface UploadQueue<T> {

    /**
     * 任务入队
     * @param t
     * @return
     */
    boolean enqueue(T t);

    /**
     * 任务出队
     * @return
     */
    T dequeue();

}
