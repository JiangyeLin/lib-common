package com.trc.android.common.util;


import android.os.Build;


public class CookieUtil {
    public static void setCookie(String domain, String key, String value) {

        com.tencent.smtt.sdk.CookieManager x5CookieManager = com.tencent.smtt.sdk.CookieManager.getInstance();
        x5CookieManager.setCookie(domain, getCookie(domain, key, value));

        android.webkit.CookieManager androidCookieManager1 = android.webkit.CookieManager.getInstance();
        androidCookieManager1.setCookie(domain, getCookie(domain, key, value));
    }

    public static void clearCookie() {
        com.tencent.smtt.sdk.CookieManager.getInstance().removeAllCookie();

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
