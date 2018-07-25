package com.tbruyelle.rxpermissions2.sample;

import java.util.concurrent.TimeUnit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {
    /**
     * 封裝io上游 mainThread 下游.
     * @param <T> upstream
     * @return ObservableTransformer
     */
    public static <T>ObservableTransformer<T, T> io_main() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 封裝dialog處理.
     * @param dialog Dialog
     * @param <T> upstream
     * @return ObservableTransformer
     */
    public static <T> ObservableTransformer<T, T> applySchedulers(@NonNull final Dialog dialog) {
        return upstream -> upstream
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull final Disposable disposable) throws Exception {
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override public void onCancel(DialogInterface dialog1) {
                                disposable.dispose();
                            }
                        });
                        dialog.show();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(new Action() {
                    @Override public void run() throws Exception {
                        dialog.dismiss();
                    }
                });
    }

}
