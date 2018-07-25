package com.tbruyelle.rxpermissions2.sample;

import java.lang.ref.WeakReference;

import android.content.Context;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class MyObservser<T> implements Observer<T> {

    private WeakReference<Context> mContextReference;

    public MyObservser(Context context) {
        this.mContextReference = new WeakReference<>(context);
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public abstract void onNext(T t);

    @Override
    public abstract void onError(Throwable e);

    @Override
    public void onComplete() {

    }


}
