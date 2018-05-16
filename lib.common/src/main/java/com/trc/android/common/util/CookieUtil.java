package com.trc.android.common.util;


import android.os.Build;
import android.webkit.ValueCallback;

import com.tencent.smtt.sdk.CookieManager;

public class CookieUtil {
    public static void setCookie(String domain, String key, String value) {
        String keyValue = key + "=" + value;

        com.tencent.smtt.sdk.CookieManager x5CookieManager = com.tencent.smtt.sdk.CookieManager.getInstance();
        String x5cookie = x5CookieManager.getCookie(domain);
        if (null != x5cookie && !x5cookie.contains(keyValue)) {
            x5CookieManager.setCookie(domain, getCookie(domain, key, value));
        }

        android.webkit.CookieManager androidCookieManager1 = android.webkit.CookieManager.getInstance();
        String androidCookie = androidCookieManager1.getCookie(domain);
        if (null != androidCookie && !androidCookie.contains(keyValue)) {
            androidCookieManager1.setCookie(domain, getCookie(domain, key, value));
        }
    }

    public static void clearCookie() {
        CookieManager.getInstance().removeAllCookie();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().removeAllCookies(null);
        } else {
            android.webkit.CookieManager.getInstance().removeAllCookie();
        }
    }

    private static String getCookie(String domain, String name, String value) {
        return name + "=" + value + "; domain=" + domain + "; path=/";
    }
}
