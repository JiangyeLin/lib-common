package com.trc.android.common.util;

/**
 * @author HanTuo on 2017/5/23.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.meituan.android.walle.ChannelInfo;
import com.meituan.android.walle.ChannelReader;
import com.trc.android.common.exception.ExceptionManager;

import java.io.File;

public class ChannelUtil {

    public static final String XML_NAME = "channel";
    /**
     * 渠道号：正式：offical  测试:dev1
     */
    public static final String CHANNEL = "official";

    /**
     * 返回市场。  如果获取失败返回""
     *
     * @return
     */
    public static String getChannel() {
        Context context = Contexts.getInstance();
        SharedPreferences sp = getChannelSp(context);
        String version = CHANNEL;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionManager.handle(e);
        }
        String channel = sp.getString(version, null);
        if (TextUtils.isEmpty(channel)) {
            channel = getChannelFromApk(context);
        }
        if (!TextUtils.isEmpty(channel)) {
            //保存sp中备用
            getChannelSp(context).edit().putString(version, channel).apply();
            return channel;
        }
        //全部获取失败
        return channel;
    }


    private static SharedPreferences getChannelSp(Context context) {
        return context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 从apk中获取版本信息
     *
     * @param context
     * @return
     */
    private static String getChannelFromApk(Context context) {
        try {
            //从apk包中获取
            ApplicationInfo appinfo = context.getApplicationInfo();
            String sourceDir = appinfo.sourceDir;

            ChannelInfo channelInfo = ChannelReader.get(new File(sourceDir));
            return channelInfo.getChannel();
        } catch (Throwable e) {
            ExceptionManager.handle(e);
            return CHANNEL;
        }
    }

}