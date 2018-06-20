package com.trc.android.common.util;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static Map create(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 == 1) {
            throw new IllegalArgumentException("Key Value 必须能够配对");
        }
        HashMap<Object, Object> map = new HashMap(keyValuePairs.length / 2);
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            map.put(key, value);
        }
        return map;
    }

    public static Map createImmutable(Object... keyValuePairs) {
        return Collections.unmodifiableMap(create(keyValuePairs));
    }

    public static boolean hasValue(Map map) {
        return map == null || map.isEmpty();
    }
}
