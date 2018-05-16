package com.trc.android.common.util;

import android.content.Context;

public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(float dpValue) {
        final float scale = Contexts.getInstance().getResources()
                .getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dip(float pxValue) {
        final float scale = Contexts.getInstance().getResources()
                .getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }

}
