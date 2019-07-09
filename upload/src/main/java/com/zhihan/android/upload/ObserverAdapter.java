package com.zhihan.android.upload;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author wangxin
 * @since 20190709
 */
public abstract class ObserverAdapter<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
