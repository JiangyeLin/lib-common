package com.trc.android.common.http.cookie;

import com.tencent.smtt.sdk.CookieManager;
import com.trc.android.common.h5.CookieConfig;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class X5CookieManager implements CookieJar {


    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            CookieManager.getInstance().setCookie(url.host(), cookie.toString());
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        String cookieKeyValuePairs = CookieManager.getInstance().getCookie(host);
        LinkedList<Cookie> list = new LinkedList<>();
        for (CookieConfig.KeyValue keyValue : CookieConfig.getUniversalCookie()) {
            Cookie cookie = new Cookie.Builder()
                    .domain(host)
                    .name(keyValue.key)
                    .value(keyValue.value)
                    .build();
            list.add(cookie);
        }
        if (null != cookieKeyValuePairs) {
            String[] keyValuePairs = cookieKeyValuePairs.split("; ");
            for (String keyValuePair : keyValuePairs) {
                String[] keyValue = keyValuePair.split("=");
                if (keyValue.length > 1) {
                    Cookie cookie = new Cookie.Builder()
                            .domain(host)
                            .name(keyValue[0])
                            .value(keyValue[1])
                            .build();
                    list.add(cookie);
                }
            }
        }
        return list;
    }
}
