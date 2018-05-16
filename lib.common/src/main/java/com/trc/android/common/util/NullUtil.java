package com.trc.android.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author HanTuo on 2017/2/21.
 */

public class NullUtil {
    public static boolean equal(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    public static boolean notEmpty(Collection collection) {
        return null != collection && !collection.isEmpty();
    }

    public String getStr(String str) {
        return null == str ? "" : str;
    }

    public String getStr(String str, String defaultStr) {
        return notEmpty(str) ? str : defaultStr;
    }

    public static boolean notEmpty(Map map) {
        return null != map && !map.isEmpty();
    }

    public static boolean notEmpty(String str) {
        return null != str && !str.isEmpty();
    }

    public static boolean noEmpty(String... strs) {
        for (String str : strs) {
            if (!notEmpty(str)) return false;
        }
        return true;
    }

    public static boolean noNull(Object... objects) {
        for (Object o : objects) {
            if (o == null) return false;
        }
        return true;
    }

    public static <T> T checkNull(T t) {
        if (t == null) {
            throw new NullPointerException();
        } else {
            return t;
        }
    }
}
