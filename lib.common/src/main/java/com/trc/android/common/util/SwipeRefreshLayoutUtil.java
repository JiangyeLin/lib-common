package com.trc.android.common.util;

import android.support.v4.widget.SwipeRefreshLayout;

import com.trc.common.R;


public class SwipeRefreshLayoutUtil {

    public static void initStyle(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.lib_common_loading_color_1,
                    R.color.lib_common_loading_color_2,
                    R.color.lib_common_loading_color_3,
                    R.color.lib_common_loading_color_4,
                    R.color.lib_common_loading_color_5);
        }
    }

}