package com.trc.android.common;

import android.app.Application;

import com.tencent.smtt.sdk.WebView;
import com.trc.android.common.exception.AntiNpeUtil;
import com.trc.android.common.login.LoginStatusManager;
import com.trc.android.common.util.Contexts;
import com.trc.android.common.util.ObjCacheUtil;

public class CommonLib {
    public static void init(Application application) {
        Contexts.init(application);
        ObjCacheUtil.init(application);
        AntiNpeUtil.init(application);
        LoginStatusManager.init(application);
        new WebView(application);
    }
}
