package com.trc.android.common.modulize;

import android.app.Application;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author HanTuo on 2017/6/1.
 */

public interface ModuleInterface {
    /**
     * 应用程序启动会回调此方法
     *
     * @param application
     */
    void onApplicationCreate(Application application);

    /**
     * 应用程序退出，需要退出当前模块所有进程
     */
    void onExitApp(int currentMainProcessId);

    void onMainActivityLifecycleStatusChanged(@TypeDef int type);

    /**
     * 登录状态发生变化
     */
    void onLoginStatusChanged(boolean login);



    int TYPE_ON_CREATE = 0;
    int TYPE_ON_START = 1;
    int TYPE_ON_RESUME = 2;
    int TYPE_ON_PAUSE = 3;
    int TYPE_ON_STOP = 4;
    int TYPE_ON_DESTROY = 5;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_ON_CREATE, TYPE_ON_START, TYPE_ON_RESUME, TYPE_ON_PAUSE, TYPE_ON_STOP, TYPE_ON_DESTROY})
    public @interface TypeDef {
    }
}
