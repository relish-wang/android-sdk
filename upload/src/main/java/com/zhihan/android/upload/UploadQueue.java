package com.zhihan.android.upload;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190706
 */
public interface UploadQueue<T> {

    boolean enqueue(T t);

    T dequeue();

}
