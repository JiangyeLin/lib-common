package com.trc.android.common;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;
import com.trc.android.common.login.LoginStatusManager;
import com.trc.android.common.util.Contexts;
import com.trc.android.common.util.ObjCacheUtil;

/**
 * Common模块的初始化工作{@link #init(Application)}
 * 包括一下工具类的初始化工作
 * <br>{@link Contexts}
 * <br>{@link ObjCacheUtil}
 * <br>{@link LoginStatusManager}
 */
public class CommonLib {

    public static void init(Application application) {
        Contexts.init(application);
        ObjCacheUtil.init(application);
        LoginStatusManager.init(application);
//        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.initX5Environment(application, new QbSdk.PreInitCallback() {

            @Override
            public void onCoreInitFinished() {
                System.out.println("x5CoreInitComplete");
            }

            @Override
            public void onViewInitFinished(boolean x5CoreInitComplete) {
                System.out.println("x5CoreInitComplete：" + x5CoreInitComplete);
            }
        });
    }

}
