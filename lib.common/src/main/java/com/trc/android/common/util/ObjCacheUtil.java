package com.trc.android.common.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author HanTuo on 16/7/27.
 */
public class ObjCacheUtil {
    private static File DEFAULT_DIR;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void init(Context context) {
        DEFAULT_DIR = context.getExternalFilesDir("ObjectCache");
    }

    public static void saveAsync(final Callback<Boolean> callback, @NonNull final File file, @NonNull final Object obj) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = save(file, obj);
                if (null != callback)
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(success);
                        }
                    });
            }
        }).start();
    }

    public static void saveAsync(final Callback<Boolean> callback, @NonNull final String name, @NonNull final Object obj) {
        saveAsync(callback, getFileFromObj(obj, name), obj);
    }

    @Deprecated
    /**
     * obj的Class文件如果被混淆会导致缓存文件对应关系丢失
     */
    public static void saveAsync(final Callback<Boolean> callback, @NonNull final Object obj) {
        saveAsync(callback, getFileFromObj(obj), obj);
    }

    @Deprecated
    /**
     * obj的Class文件如果被混淆会导致缓存文件对应关系丢失
     */
    public static void saveAsync(@NonNull File file, @NonNull Object obj) {
        saveAsync(null, file, obj);
    }

    @Deprecated
    /**
     * obj的Class文件如果被混淆会导致缓存文件对应关系丢失
     */
    public static void saveAsync(@NonNull String name, @NonNull Object obj) {
        saveAsync(null, name, obj);
    }

    @Deprecated
    /**
     * obj的Class文件如果被混淆会导致缓存文件对应关系丢失
     */
    public static void saveAsync(@NonNull Object obj) {
        saveAsync((Callback<Boolean>) null, obj);
    }

    public static boolean save(@NonNull File file, @NonNull Object obj) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            File tmpFile = new File(file.getPath() + ".tmp-" + System.currentTimeMillis());
            FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
            String string = new Gson().toJson(obj);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(string.getBytes());
            bufferedOutputStream.flush();
            fileOutputStream.close();
            return tmpFile.renameTo(file);

        } catch (Throwable e) {
            file.delete();
        }
        return false;
    }

    public static boolean save(@NonNull String name, @NonNull Object obj) {
        return save(getFileFromObj(obj, name), obj);
    }

    public static boolean save(@NonNull Object obj) {
        return save(getFileFromObj(obj), obj);
    }

    public static void delete(Class clazz) {
        new File(DEFAULT_DIR, clazz.getName()).delete();
    }

    public static void deleteCollection(Class clazz) {
        new File(DEFAULT_DIR, "Collection-" + clazz.getName()).delete();
    }

    public static void delete(String name) {
        new File(DEFAULT_DIR, name).delete();
    }

    public static <T> void getAsync(final Callback<T> callback, @NonNull final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Type type = callback.getClass().getGenericInterfaces()[0];
                    type = ((ParameterizedType) type).getActualTypeArguments()[0];
                    final T t = get(file, type);
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(t);
                        }
                    });
                } catch (Exception e) {
                    callback.onResult(null);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static <T> void getAsync(final Callback<T> callback, String name) {
        try {
            getAsync(callback, getFile(callback, name));
        } catch (Exception e) {
            callback.onResult(null);
            e.printStackTrace();
        }
    }

    public static <T> void getAsync(final Callback<T> callback) {
        try {
            getAsync(callback, getFile(callback));
        } catch (Exception e) {
            callback.onResult(null);
            e.printStackTrace();
        }
    }


    private static <T> File getFile(Callback<T> callback) {
        return getFile(callback, null);
    }

    private static <T> File getFile(Callback<T> callback, String name) {
        String fileName;
        if (null != name) {
            fileName = name;
        } else {
            Type type = callback.getClass().getGenericInterfaces()[0];
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (type instanceof Class) {
                fileName = ((Class) type).getName();
            } else {
                fileName = "Collection-" + ((Class) ((ParameterizedType) type).getActualTypeArguments()[0]).getName();
            }
        }
        return new File(DEFAULT_DIR, fileName);
    }

    public static File getFileFromObj(Object object) {
        return getFileFromObj(object, null);
    }

    public static File getFileFromObj(Object object, String name) {
        String fileName;
        if (null != name) {
            fileName = name;
        } else {
            if (object instanceof Iterable) {
                Iterable iterable = (Iterable) object;
                if (iterable.iterator().hasNext()) {
                    fileName = "Collection-" + iterable.iterator().next().getClass().getName();
                } else {
                    fileName = "EmptyList";
                }
            } else if (object instanceof Class) {
                fileName = ((Class) object).getName();
            } else {
                fileName = object.getClass().getName();
            }
        }
        return new File(DEFAULT_DIR, fileName);
    }

    @Nullable
    public static <T extends Object> T get(File file, Type clazz) {
        try {
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                byte[] bytes = new byte[bufferedInputStream.available()];
                bufferedInputStream.read(bytes);
                String string = new String(bytes);
                T t = new Gson().fromJson(string, clazz);
                return t;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static <T extends Object> T get(String name, Class<T> clazz) {
        return get(getFileFromObj(clazz, name), clazz);
    }

    @Nullable
    public static <T extends Object> T get(Class<T> clazz) {
        return get(getFileFromObj(clazz), clazz);
    }

    public interface Callback<T> {
        void onResult(T t);
    }


}
