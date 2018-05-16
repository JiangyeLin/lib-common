package com.trc.android.common.exception;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;

/**
 * @author HanTuo on 2017/11/13.
 */

public class AntiNpeUtil {


    public static void init(Application application) {
        setDefaultUncaughtExceptionHandler();
        trcCatchLooper();
    }

    private static void trcCatchLooper() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //主线程异常拦截
                while (true) {
                    try {
                        Looper.loop();//主线程的异常会从这里抛出
                    } catch (Throwable e) {
                        ExceptionManager.handle(e);
                    }
                }
            }
        });
    }

    //设置全局异常捕捉，如果已经设置则代理之。捕捉到主线程的异常则退出APP
    private static void setDefaultUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler originHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (null != originHandler) originHandler.uncaughtException(t, e);
                if (Looper.getMainLooper().isCurrentThread()) {
                    ExceptionManager.handleSync(e);
                    System.exit(0);
                } else {
                    ExceptionManager.handle(e);
                }
            }
        });
    }
}
