package com.trc.android.common.h5;


import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;

import com.tencent.smtt.sdk.CookieManager;

/**
 * 对系统CookieManager和腾讯X5内核的CookieManager同时进行Cookie的写入操作
 * 之所以对系统CookieManager进行同步Cookie管理是因为ReactNative要从该系统CookieManager读取相关数据
 */
public class CookieUtil {
    /**
     * @param domain 可以是域名也可以是链接，支持通配域名，例如.trc.com
     * @param value  如果为null 或 "",则相当于移除该Cookie值， 如果想RemoveCookie{@link #removeCookie(String, String)}
     */
    public static void setCookie(String domain, String key, @Nullable String value) {
        if (null == value) value = "";
        if (domain.contains("//")) {
            domain = Uri.parse(domain).getHost();
        }
        CookieManager x5CookieManager = CookieManager.getInstance();
        x5CookieManager.setCookie(domain, buildCookie(domain, key, value));
        x5CookieManager.flush();

        android.webkit.CookieManager androidCookieManager = android.webkit.CookieManager.getInstance();
        androidCookieManager.setCookie(domain, buildCookie(domain, key, value));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            androidCookieManager.flush();
        }

    }

    public static void clearCookie() {
        com.tencent.smtt.sdk.CookieManager.getInstance().removeAllCookie();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().removeAllCookies(null);
        } else {
            android.webkit.CookieManager.getInstance().removeAllCookie();
        }
    }

    private static String buildCookie(String domain, String name, String value) {
        return name + "=" + value + "; domain=" + domain + "; path=/";
    }

    private static String buildExpireCookie(String domain, String name) {
        return name + "=; domain=" + domain + "; path=/ ;Expires=1 Jan 1970 00:00:00 GMT";
    }

    /**
     * 通过过期Cookie覆盖之前的Value，以移除特定Cookie的Value
     */
    public static void removeCookie(String domain, String key) {
        CookieManager x5CookieManager = CookieManager.getInstance();
        x5CookieManager.setCookie(domain, buildExpireCookie(domain, key));
        x5CookieManager.flush();
        x5CookieManager.removeExpiredCookie();


        android.webkit.CookieManager androidCookieManager = android.webkit.CookieManager.getInstance();
        androidCookieManager.setCookie(domain, buildExpireCookie(domain, key));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            androidCookieManager.flush();
        } else {
            androidCookieManager.removeExpiredCookie();
        }

    }
}
