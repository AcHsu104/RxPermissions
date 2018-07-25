package com.tbruyelle.rxpermissions2.sample;

import android.Manifest.permission;
import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableSubscribeProxy;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RxPermissionsSample";

    private Camera camera;
    private SurfaceView surfaceView;
    private CoordinatorLayout coordinator;

    private Disposable disposable;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RxPermissions rxPermissions;

    private ObservableSubscribeProxy<Long> timerObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);

        setContentView(R.layout.act_main);

        coordinator = findViewById(R.id.coordinator);
        surfaceView = findViewById(R.id.surfaceView);

/*        RxView.clicks(findViewById(R.id.enableCamera))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object value) {
                        showSnakeBar("msg");
                        Log.e("ccc", System.currentTimeMillis() + "");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });*/

        /*RxView.clicks(findViewById(R.id.enableCamera))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(aVoid -> { Log.e(getClass().getSimpleName(), System.currentTimeMillis() + ""); }
                , throwable -> { Log.e(getClass().getSimpleName(), throwable.toString()); });*/

        RxView.clicks(findViewById(R.id.enableCamera))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(aVoid -> { compositeDisposable.add(getRequestPermission(permission.CAMERA)
                                .subscribe(getPermissionConsumer())); }
                        , throwable -> { Log.e(getClass().getSimpleName(), throwable.toString()); });


        //doOnNext 用來處理check upstream 的一些行為, 例如data status code 去拋出exception 給onerror 處理
        Observable.range(0,10).doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                if (integer.intValue() == 5) {
                    throw new RuntimeException("123");
                }
            }
        }).compose(_retryWhen()).subscribe(new MyObservser<Integer>(this) {
            @Override
            public void onNext(Integer integer) {
                Log.e(getClass().getSimpleName(), "onNext");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(getClass().getSimpleName(), "onError");
            }
        });
        /*findViewById(R.id.enableCamera).setOnClickListener(aVoid -> {
            getRequestPermission(permission.CAMERA)
                    .subscribe(getPermissionConsumer());
        });*/
        //getRequestPermission(permission.CAMERA)
        /*Observable.just(1,2).map(integer -> integer * 10 + "").subscribe(new Observer<String>() {
            Disposable d;
            @Override
            public void onSubscribe(Disposable d) {
                this.d = d;
            }

            @Override
            public void onNext(String value) {
                Log.i(TAG, "accept : " + value + "\n");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete");
                d.dispose();
            }
        });*/

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading Video ...");

        Observable.timer(3, TimeUnit.SECONDS).flatMap(integer -> Observable.just(5 * integer))
                .compose(RxSchedulers.applySchedulers(progressDialog))
                //.compose(RxSchedulers.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this,Lifecycle.Event.ON_DESTROY)))
                .subscribe(new Observer<Long>() {
            Disposable d;
            @Override
            public void onSubscribe(Disposable d) {
                this.d = d;
            }

            @Override
            public void onNext(Long value) {
                Log.i(TAG, "accept : " + value + "\n");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete");
                d.dispose();
            }
        });

        timerObservable = Observable.intervalRange(0,Integer.MAX_VALUE,0, 2, TimeUnit.SECONDS)
                .doOnDispose(() -> {Log.e(this.getClass().getSimpleName(), "doOnDispose");})
                .map(aLong -> aLong * 5)
                .compose(RxSchedulers.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_PAUSE)));

        /*timerObservable.subscribe(stringObservableSource -> {
            Log.e("rrrr", stringObservableSource.toString());
        });*/

        /*compositeDisposable.add(RxView.clicks(findViewById(R.id.enableCamera))
                // Ask for permissions when button is clicked
                .compose(rxPermissions.ensureEach(permission.CAMERA))
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    private int mRetryCount = 0;
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                if (mRetryCount < 5) {
                                    ++mRetryCount;
                                    return Observable.just(new Object()).compose(rxPermissions.ensureEach(permission.CAMERA));
                                } else {
                                    return Observable.error(throwable);
                                }
                            }
                        });
                    }
                })
                .subscribe(new Consumer<Permission>() {
                               @Override
                               public void accept(Permission permission) {
                                   Log.i(TAG, "Permission result " + permission);
                                   if (permission.granted) {
                                       releaseCamera();
                                       camera = Camera.open(0);
                                       try {
                                           camera.setPreviewDisplay(surfaceView.getHolder());
                                           camera.startPreview();
                                       } catch (IOException e) {
                                           Log.e(TAG, "Error while trying to display the camera preview", e);
                                       }
                                   } else if (permission.shouldShowRequestPermissionRationale) {
                                       // Denied permission without ask never again
                                       *//*Toast.makeText(MainActivity.this,
                                               "Denied permission without ask never again",
                                               Toast.LENGTH_SHORT).show();*//*
                                       showSnakeBar("Denied permission without ask never again");
                                   } else {
                                       // Denied permission with ask never again
                                       // Need to go to the settings
                                       *//*Toast.makeText(MainActivity.this,
                                               "Permission denied, can't enable the camera",
                                               Toast.LENGTH_SHORT).show();*//*
                                       showSnakeBar("Permission denied, can't enable the camera");
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable t) {
                                Log.e(TAG, "onError", t);
                            }
                        },
                        new Action() {
                            @Override
                            public void run() {
                                Log.i(TAG, "OnComplete");
                            }
                        }));*/

        /*disposable = RxView.clicks(findViewById(R.id.enableCamera))
                // Ask for permissions when button is clicked
                .compose(rxPermissions.ensureEach(permission.CAMERA))
                .subscribe(new Consumer<Permission>() {
                               @Override
                               public void accept(Permission permission) {
                                   Log.i(TAG, "Permission result " + permission);
                                   if (permission.granted) {
                                       releaseCamera();
                                       camera = Camera.open(0);
                                       try {
                                           camera.setPreviewDisplay(surfaceView.getHolder());
                                           camera.startPreview();
                                       } catch (IOException e) {
                                           Log.e(TAG, "Error while trying to display the camera preview", e);
                                       }
                                   } else if (permission.shouldShowRequestPermissionRationale) {
                                       // Denied permission without ask never again
                                       Toast.makeText(MainActivity.this,
                                               "Denied permission without ask never again",
                                               Toast.LENGTH_SHORT).show();
                                   } else {
                                       // Denied permission with ask never again
                                       // Need to go to the settings
                                       Toast.makeText(MainActivity.this,
                                               "Permission denied, can't enable the camera",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable t) {
                                Log.e(TAG, "onError", t);
                            }
                        },
                        new Action() {
                            @Override
                            public void run() {
                                Log.i(TAG, "OnComplete");
                            }
                        });*/
    }

    private Observable<Permission> getRequestPermission(String... permissions) {
        return rxPermissions.requestEach(permissions).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Consumer<Permission> getPermissionConsumer() {
        return permission -> {
                Log.i(TAG, "Permission result " + permission);
                if (permission.granted) {
                    releaseCamera();
                    camera = Camera.open(0);
                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                        camera.startPreview();
                    } catch (IOException e) {
                        Log.e(TAG, "Error while trying to display the camera preview", e);
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    showSnakeBar("Denied permission without ask never again");
                } else {
                    showSnakeBar("Permission denied, can't enable the camera");
                }
            };
    }

    private void showSnakeBar(String msg) {
        compositeDisposable.clear();
        runOnUiThread(() -> {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar
                    .make(coordinator, msg, Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            compositeDisposable.add(getRequestPermission(permission.CAMERA)
                                    .subscribe(getPermissionConsumer()));
                            //throw new NullPointerException("permission denine");
                        }
                    });
            snackbar.setActionTextColor(Color.RED);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.setDuration(3000);
            snackbar.show();
        });

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerObservable.subscribe(stringObservableSource -> {
            Log.e("rrrr", stringObservableSource.toString());
            //showSnakeBar("iheihewiew");
        });
        //  showSnakeBar("iheihewiew");
    }

    private Observable<Integer> getObservable () {
        return Observable.create((ObservableOnSubscribe<Integer>) e -> {
            e.onNext(new Integer(55));
            e.onError(new RuntimeException());
        });
    }

    private <T>ObservableTransformer<T, T> _retryWhen() {
        AtomicInteger counter = new AtomicInteger();
        return upstream ->  upstream.retryWhen(observable ->
                observable.flatMap(throwable -> {
                    return Observable.create(subscriber -> {
                        counter.incrementAndGet();
                            if (counter.get() > 5) {
                                return;
                            }
                                String msg = "もう一度リトライしますか？";
                                if (throwable instanceof RuntimeException) {
                                    msg = throwable.getMessage();
                                }
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("エラー")
                                        .setMessage(msg)
                                        .setPositiveButton("はい", (dialog, which) ->
                                                subscriber.onNext(upstream)
                                        )
                                        .setNegativeButton("いいえ", (dialog, which) ->
                                                subscriber.onError(throwable)
                                        )
                                        .show();
                            });
                    }
                )
        );
    }
}