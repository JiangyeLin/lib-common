package com.trc.android.common.dev;

import android.content.Context;
import android.content.SharedPreferences;

import com.trc.android.common.util.Contexts;


/**
 * @author HanTuo on 2017/8/9.
 */

public class ConfigHelper {
    private static SharedPreferences sp = Contexts.getInstance().getSharedPreferences("DevConfig.xml", Context.MODE_PRIVATE);

    public static String get(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static String get(String key) {
        return sp.getString(key, null);
    }

    public static void put(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }

    public static boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public static void put(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }


    public static void clear() {
        sp.edit().clear().apply();
    }

}
