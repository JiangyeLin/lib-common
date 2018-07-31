package com.trc.android.common.h5;

import android.app.Activity;
import android.net.Uri;
import android.view.ViewGroup;

public interface ToolbarInterface {

    /**
     * 在这个方法里，创建Toolbar的View，并添加到ViewGroup里面
     * @param toolbarContainer Toolbar View的容器ViewGroup
     */
    void onAttach(ViewGroup toolbarContainer, WebViewHelper webViewHelper, Activity activity);


    void onSetTitle(String title);

    /**
     * 配置Toolbar上的按钮(图标、文字内容、文字颜色、按钮背景、文字角标)处理 jsbridge://config_toolbar?params=BASE64_ENCODED_JSON
     *
     * @param uri
     */
    void onConfigToolbar(Uri uri);

    /**
     * 配置右上角菜单PopupWindow<br>
     * 处理jsbridge://config_option_menu
     *
     * @param uri
     */
    void onConfigOptionMenu(Uri uri);

    void onPageFinished(String url);

    boolean onBackBtnPress();
}
