package com.trc.android.common.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author HanTuo on 2015/12/23.
 */
public class LogUtil {
    private static boolean showLog = true;
    private static String prefix = "";

    /**
     * @param tagPrefix    appName is recommended
     */
    public static void config(boolean showDebugLog, String tagPrefix) {
        showLog = showDebugLog;
        prefix = tagPrefix;
    }

    /**
     * 支持"%s"作为占位符出现在str里面，并用后面对象的String Value替换
     */
    public static void sv(String str, Object... objects) {
        for (Object o : objects) {
            str = str.replaceFirst("%s", String.valueOf(o));
        }
        v(str);
    }

    public static void v(@Nullable Object e) {
        v(prefix, e);
    }

    public static void v(String tag, @Nullable Object o) {
        if (showLog) {
            Log.v(prefix + tag, getString(o));
        }
    }

    //---------------------------------------------------------------------------------------------------------------------

    /**
     *支持"%s"作为占位符出现在str里面，并用后面对象的String Value替换
     */
    public static void sd(String str, Object... objects) {
        for (Object o : objects) {
            str = str.replaceFirst("%s", String.valueOf(o));
        }
        d(str);
    }

    public static void d(Object e) {
        d(prefix, e);
    }

    public static void d(String tag, @Nullable Object o) {
        if (showLog) {
            Log.d(prefix + tag, getString(o));
        }
    }

    //---------------------------------------------------------------------------------------------------------------------

    /**
     * 支持"%s"作为占位符出现在str里面，并用后面对象的String Value替换
     */
    public static void si(String str, Object... objects) {
        for (Object o : objects) {
            str = str.replaceFirst("%s", String.valueOf(o));
        }
        i(str);
    }

    public static void i(@Nullable Object o) {
        i(prefix, o);
    }

    public static void i(String tag, @Nullable Object o) {
        if (showLog) {
            Log.i(prefix + tag, getString(o));
        }
    }

    //---------------------------------------------------------------------------------------------------------------------

    /**
     * 支持"%s"作为占位符出现在str里面，并用后面对象的String Value替换
     */
    public static void sw(String str, Object... objects) {
        for (Object o : objects) {
            str = str.replaceFirst("%s", String.valueOf(o));
        }
        w(str);
    }

    public static void w(@Nullable Object e) {
        w(prefix, e);
    }

    public static void w(String tag, @Nullable Object o) {
        if (showLog) {
            Log.w(prefix + tag, getString(o));
        }
    }

    //---------------------------------------------------------------------------------------------------------------------

    /**
     * @param str     "%s"作为占位符
     * @param objects 对象的String.valueOf(o)
     */
    public static void se(String str, Object... objects) {
        for (Object o : objects) {
            str = str.replaceFirst("%s", String.valueOf(o));
        }
        e(str);
    }

    public static void e(@Nullable Object o) {
        e(prefix, o);
    }

    public static void e(String tag, @Nullable Object o) {
        if (showLog) {
            Log.e(prefix + tag, getString(o));
        }
    }

    /**
     * 输入日志到文件
     *
     * @param logFileName 输出的
     */
    public static void f(@Nullable Object o, @NonNull String logFileName, Context context) {
        File file = new File(context.getExternalFilesDir("Log"), logFileName);
        String str = getString(o);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            str = simpleDateFormat.format(new Date(System.currentTimeMillis())) + "\n" + str + "\n\n";
            byte[] bytes = str.getBytes();
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException e) {
            Log.e("LogUtil", Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.e("LogUtil", Log.getStackTraceString(e));
        }
    }

    private static String getString(Object o) {
        String str;
        if (null == o) str = "null";
        else if (o instanceof Throwable) str = Log.getStackTraceString((Throwable) o);
        else str = String.valueOf(o);
        return str;
    }

}
