package com.trc.android.common.util;

import android.view.View;

import com.trc.android.common.exception.ExceptionManager;


/**
 * @author HanTuo on 2017/2/21.
 */

public class ClickUtil {


    /**
     * @param onClickListener 该Listener会被代理掉
     */
    public static void setClickListener(final View.OnClickListener onClickListener, View... views) {
        View.OnClickListener listener = v -> {
            try {
                onClickListener.onClick(v);
            } catch (Exception e) {
                ExceptionManager.handle(e);
            }
        };
        for (View v : views) {
            v.setOnClickListener(listener);
        }
    }

    /**
     * @param onClickListener 该Listener会被代理掉
     * @param timeMilis       点击触发后，不能点击持续掉时间
     */
    public static void setClickListener(final View.OnClickListener onClickListener, final long timeMilis, View... views) {
        final View.OnClickListener listener = v -> {
            try {
                v.setClickable(false);
                onClickListener.onClick(v);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setClickable(true);
                    }
                }, timeMilis);
            } catch (Exception e) {
                ExceptionManager.handle(e);
            }
        };
        for (View v : views) {
            v.setOnClickListener(listener);
        }
    }

    /**
     * 防止快速点击
     *
     * @param view
     * @param timeMilis 点击触发后，不能点击持续掉时间
     */
    public static void antiRepeat(final View view, long timeMilis) {
        try {
            if (null != view) {
                view.setClickable(false);
                view.postDelayed(() -> view.setClickable(true), timeMilis);
            }
        } catch (Exception e) {
            ExceptionManager.handle(e);
        }
    }

    /**
     * @param times 点击事件最终触发时需要的次数
     */
    public static void setOnFastRepeatClickListener(View view, final int times, final View.OnClickListener onClickListener) {
        view.setOnClickListener(new View.OnClickListener() {
            int clickTime;

            @Override
            public void onClick(View v) {
                clickTime++;
                if (clickTime > times) {
                    clickTime = 0;
                    onClickListener.onClick(v);
                }
            }
        });
    }
}
