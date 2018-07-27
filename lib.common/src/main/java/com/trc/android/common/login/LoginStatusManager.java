package com.trc.android.common.login;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用系统广播的方式以保证做到登录状态的跨进程通知
 */
public class LoginStatusManager {
    public static final String LOGIN_STATUS_ACTION_PREFIX = "login_status_changed:";
    public static final String INTENT_KEY_STATUS = "status";
    public static final String INTENT_KEY_EXTRA = "extra";
    private static final String INTENT_KEY_PROCESS_ID = "pid";
    private static Application sApplication;
    private static List<Listener> sListenerList = new LinkedList<>();

    private static String getAction() {
        return LOGIN_STATUS_ACTION_PREFIX + sApplication.getPackageName();
    }

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

    public static void sendBroadCast(boolean isLogin, Serializable serializable) {
        //优先处理当前进程中的Listeners
        notifyListeners(isLogin, serializable);

        //发广播通知其他进程中的Listeners
        Intent intent = new Intent(getAction());
        intent.putExtra(INTENT_KEY_STATUS, isLogin);
        intent.putExtra(INTENT_KEY_PROCESS_ID, Process.myPid());
        intent.putExtra(INTENT_KEY_EXTRA, serializable);
        sApplication.sendBroadcast(intent);
    }

    public static void sendBroadCast(boolean isLogin) {
        sendBroadCast(isLogin, null);
    }

    public static void register(final Listener listener) {
        synchronized (sListenerList) {
            sListenerList.add(listener);
        }
    }

    public static void unregister(final Listener listener) {
        synchronized (sListenerList) {
            sListenerList.remove(listener);
        }
    }

    public interface Listener {
        void onChanged(boolean loginOrlogout, Serializable extra);
    }
}
