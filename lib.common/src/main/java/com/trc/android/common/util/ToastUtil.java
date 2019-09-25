package com.trc.android.common.util;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

/**
 * @author hm
 *         <p>
 *         Description：封装Taost，频繁出发show（）时，只显示最新的Toast 防止消息过多，Toast长时间至所有消息释放完毕
 */
public class ToastUtil {
    private static Toast toast = null;

    public static void showNormalToast(final String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            toast(message);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> toast(message));
        }
    }

    private static void toast(String message) {
        try {
            if (toast != null) {
                toast.cancel();
            }
            if (TextUtils.isEmpty(message)) {
                return;
            }
            if (NotificationManagerCompat.from(Contexts.getInstance()).areNotificationsEnabled()) {
                toast = Toast.makeText(Contexts.getInstance(), message, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                CustomToastUtil.makeText(Contexts.getFrontActivity(), message, CustomToastUtil.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelTosat() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
