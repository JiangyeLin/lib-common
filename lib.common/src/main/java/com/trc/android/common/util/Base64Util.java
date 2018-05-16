package com.trc.android.common.util;

import android.util.Base64;

public class Base64Util {


    /**
     * URL base64解码
     *
     * @param str
     * @return
     */
    public static String decodeString(String str) {
        try {
            str = str.replace(' ', '-');//Base64.URL_SAFE
            str = str.replace('+', '-');//Base64.URL_SAFE
            str = str.replace('/', '_');//Base64.URL_SAFE
            str = str.replace("=", "");//Base64.NO_PADDING
            str = str.replace("\n", "");//Base64.NO_WRAP
            String result = new String(Base64.decode(str.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String encodeString(String str) {
        try {
            String result = Base64.encodeToString(str.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}