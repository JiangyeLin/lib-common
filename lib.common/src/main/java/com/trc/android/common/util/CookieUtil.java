package com.trc.android.common.util;


import android.os.Build;

import com.tencent.smtt.sdk.CookieManager;


public class CookieUtil {
    public static void setCookie(String domain, String key, String value) {

        CookieManager x5CookieManager = CookieManager.getInstance();
        x5CookieManager.setCookie(domain, getCookie(domain, key, value));
        x5CookieManager.flush();

        android.webkit.CookieManager androidCookieManager = android.webkit.CookieManager.getInstance();
        androidCookieManager.setCookie(domain, getCookie(domain, key, value));
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

    private static String getCookie(String domain, String name, String value) {
        return name + "=" + value + "; domain=" + domain + "; path=/";
    }

    private static String getExpireCookie(String domain, String name) {
        return name + "=; domain=" + domain + "; path=/ ;Expires=1 Jan 1970 00:00:00 GMT";
    }

    //通过过期Cookie覆盖之前的Value，以移除特定Cookie的Value
    public static void removeCookie(String domain, String key) {
        CookieManager x5CookieManager = CookieManager.getInstance();
        x5CookieManager.setCookie(domain, getExpireCookie(domain, key));
        x5CookieManager.flush();
        x5CookieManager.removeExpiredCookie();


        android.webkit.CookieManager androidCookieManager = android.webkit.CookieManager.getInstance();
        androidCookieManager.setCookie(domain, getExpireCookie(domain, key));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            androidCookieManager.flush();
        } else {
            androidCookieManager.removeExpiredCookie();
        }

    }
}
