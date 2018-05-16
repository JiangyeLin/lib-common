package com.trc.android.common.exception;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HanTuo on 2017/8/8.
 */

public class ExceptionManager {
    private static ExecutorService singleThreadExecutorService;

    private static LinkedList<ThrowableHandler> sHanlderList = new LinkedList();

    public static void addListener(ThrowableHandler listener) {
        sHanlderList.add(listener);
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    public static void handle(final Throwable throwable) {
        if (null == singleThreadExecutorService) {//线程不安全，但是没关系
            singleThreadExecutorService = Executors.newSingleThreadExecutor();
        }
        singleThreadExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                handleSync(throwable);
            }
        });

    }

    public static void handleSync(Throwable throwable) {
        Iterator<ThrowableHandler> iterator = sHanlderList.iterator();
        while (iterator.hasNext()) {
            try {
                ThrowableHandler listener = iterator.next();
                listener.handleThrowable(throwable);
            } catch (Throwable t) {
                handle(t);
            }
        }
    }


}
