package com.trc.android.common.login;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用系统广播的方式以保证做到登录状态的跨进程通知
 */
public class LoginStatusManager {
    private static final String LOGIN_STATUS_ACTION_PREFIX = "login_status_changed:";
    private static final String INTENT_KEY_STATUS = "status";
    private static final String INTENT_KEY_EXTRA = "extra";
    private static final String INTENT_KEY_PROCESS_ID = "pid";
    private static Application sApplication;
    private static List<Listener> sListenerList = new LinkedList<>();

    private static String getAction() {
        return LOGIN_STATUS_ACTION_PREFIX + sApplication.getPackageName();
    }

    /**
     * 该方法需要在应用程序初始化的时候调用
     */
    public static void init(Application application) {
        sApplication = application;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //处理其他进程发过来的登录状态变化的通知
                if (intent.getIntExtra(INTENT_KEY_PROCESS_ID, 0) != Process.myPid()) {
                    boolean loginStatus = intent.getBooleanExtra(INTENT_KEY_STATUS, false);
                    Serializable extra = null;
                    if (intent.hasExtra(INTENT_KEY_EXTRA))
                        extra = intent.getSerializableExtra(INTENT_KEY_EXTRA);
                    notifyListeners(loginStatus, extra);
                }
            }
        };
        IntentFilter filter = new IntentFilter(getAction());
        sApplication.registerReceiver(receiver, filter);
    }

    private static void notifyListeners(boolean loginStatus, Serializable extra) {
        synchronized (sListenerList) {
            for (Listener listener : sListenerList) {
                try {
                    listener.onChanged(loginStatus, extra);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    /**
     * @param isLogin true：登录 false：登出
     * @param serializable 携带的参数：比如用户token, 该参数可忽律，参考{@link #sendBroadCast(boolean)}
     */
    public static void sendBroadCast(boolean isLogin,@Nullable Serializable serializable) {
        //优先处理当前进程中的Listeners
        notifyListeners(isLogin, serializable);

        //发广播通知其他进程中的Listeners
        Intent intent = new Intent(getAction());
        intent.putExtra(INTENT_KEY_STATUS, isLogin);
        intent.putExtra(INTENT_KEY_PROCESS_ID, Process.myPid());
        intent.putExtra(INTENT_KEY_EXTRA, serializable);
        sApplication.sendBroadcast(intent);
    }

    /**
     * @param isLogin true：登录 false：登出
     */
    public static void sendBroadCast(boolean isLogin) {
        sendBroadCast(isLogin, null);
    }

    /**
     * 注册一个对登录状态变化对监听，主要调用{@link #unregister(Listener)}以防内存泄漏
     */
    public static void register(final Listener listener) {
        synchronized (sListenerList) {
            sListenerList.add(listener);
        }
    }

    /**
     * 反注册一个对登录状态变化对监听，主要调用以防内存泄漏，对应{@link #register(Listener)}方法
     */
    public static void unregister(final Listener listener) {
        synchronized (sListenerList) {
            sListenerList.remove(listener);
        }
    }

    public interface Listener {
        void onChanged(boolean loginOrlogout, Serializable extra);
    }
}
