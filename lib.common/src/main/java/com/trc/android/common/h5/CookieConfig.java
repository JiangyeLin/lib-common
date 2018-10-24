package com.trc.android.common.h5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CookieConfig {
    private static ArrayList<KeyValue> list = new ArrayList<>();
    private static List<KeyValue> outList = Collections.unmodifiableList(list);

    public static void addUniversalCookie(String key, String value) {
        remove(key);
        KeyValue keyValue = new KeyValue(key, value);
        list.add(keyValue);
    }

    public static List<KeyValue> getUniversalCookie() {
        return outList;
    }

    public static void remove(String key) {
        KeyValue k = null;
        for (KeyValue keyValue : list) {
            if (keyValue.key.equals(key)) {
                k = keyValue;
                break;
            }
        }
        list.remove(k);
    }

    public static class KeyValue implements Serializable {
        public String key;
        public String value;

        public KeyValue(String key, String value) {
            if (value == null) value = "";
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyValue keyValue = (KeyValue) o;
            return keyValue.key.equals(key) &&
                    keyValue.value.equals(value);
        }


        @Override
        public int hashCode() {
            return 31 * key.hashCode() + value.hashCode();
        }
    }
}
