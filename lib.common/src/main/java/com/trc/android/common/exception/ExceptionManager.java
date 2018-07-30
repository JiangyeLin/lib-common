package com.trc.android.common.exception;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 1.通过Handler内调用主循环，完成在主循环最外层的异常捕捉,主要解决无处不在的异常导致的APP Crash，
 * 该工具类可以捕捉所有的异常，极大程度上避免了很多地方的try catch遗漏造成的问题，
 * 比如点击事件的回调、网络请求回调等都不需要进行try catch处理<br>
 * 2.通过用ExceptionManager来管理异常的处理，方便进行统一的异常（日志）处理
 */
public class ExceptionManager {
    private static ExecutorService singleThreadExecutorService;

    private static Thread.UncaughtExceptionHandler sHandler;

    public static void init(Thread.UncaughtExceptionHandler listener) {
        sHandler = listener;
        tryCatchLooper();
        setDefaultUncaughtExceptionHandler();
    }

    private static void tryCatchLooper() {
        new Handler(Looper.getMainLooper()).post(() -> {
            //主线程异常拦截
            while (true) {
                try {
                    Looper.loop();//主线程的异常会从这里抛出
                } catch (Throwable e) {
                    handle(e);
                }
            }
        });
    }

    private static void setDefaultUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler originHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (null != originHandler) originHandler.uncaughtException(t, e);
            handle(t, e);
            if (Looper.getMainLooper() == Looper.myLooper()) {
                System.exit(0);
            }
        });
    }

    private static void handle(final Thread thread, final Throwable throwable) {
        if (null == singleThreadExecutorService) {//线程不安全，但是没关系
            singleThreadExecutorService = Executors.newSingleThreadExecutor();
        }
        singleThreadExecutorService.submit(() -> handleSync(thread, throwable));
    }

    /**
     * 用ExceptionManager.handle(e) 来代替捕捉异常后的随意 e.printStackTrace()
     */
    public static void handle(final Throwable throwable) {
        handle(Thread.currentThread(), throwable);
    }

    private static void handleSync(Thread thread, Throwable throwable) {
        if (sHandler == null) {
            throw new RuntimeException(ExceptionManager.class.getName() + "需要初始化，查看init(ThrowableHandler)方法");
        } else {
            sHandler.uncaughtException(thread, throwable);
        }
    }


}
