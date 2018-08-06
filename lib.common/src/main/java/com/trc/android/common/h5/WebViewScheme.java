package com.trc.android.common.h5;

@SuppressWarnings("DeprecatedIsStillUsed")
public class WebViewScheme {
    public static final String SCHEME = "jsbridge";

    @Deprecated
    /**
     * 关闭Page{@linkplain #ACTION_CLOSE_WINDOW}
     */
    public static final String ACTION_CLOSE_WINDOW_OLD = "exitModule";//关闭Page
    /**
     * 关闭当前Activity（一般是WebViewActivity）
     */
    public static final String ACTION_CLOSE_WINDOW = "close_window";//关闭Page
    @Deprecated
    public static final String ACTION_CLEAR_HISTORY_OLD = "clearHistory";//清除网页堆栈记录
    public static final String ACTION_CLEAR_HISTORY = "clear_history";//清除网页堆栈记录
    public static final String ACTION_CLEAR_COOKIE = "clear_cookie";//清除Cookie
    public static final String ACTION_CLEAR_CACHE = "clear_cache";//清除缓存
    public static final String ACTION_SET_COOKIE = "set_cookie";

    @Deprecated
    public static final String ACTION_CONFIG_OPTION_MENU_OLD = "configOptionMenu";//配置toolbar右上角里面的菜单
    public static final String ACTION_CONFIG_OPTION_MENU = "config_option_menu";//配置toolbar右上角里面的菜单
    @Deprecated
    public static final String ACTION_CONFIG_TOOLBAR_BTNS_OLD = "configToolbarBtns";//toolbar里面的按钮
    /**
     * 配置Toolbar里面的按钮
     */
    public static final String ACTION_CONFIG_TOOLBAR_BTNS = "config_toolbar_btns";//toolbar里面的按钮
    @Deprecated
    /**
     * 返回上个页面{@link #ACTION_GO_BACK}
     */
    public static final String ACTION_GO_BACK_OLD = "goBack";//返回上个页面
    /**
     * 返回上个页面
     */
    public static final String ACTION_GO_BACK = "go_back";
    @Deprecated
    public static final String ACTION_SELECT_CONTACT_OLD = "getContactPhoneNum";//充值获取手机号码
    public static final String ACTION_SELECT_CONTACT = "select_contact";//充值获取手机号码
    public static final String ACTION_RELOAD = "reload";//页面刷新
    public static final String ACTION_OPEN_LINK_IN_NEW_WINDOW = "open_link_in_new_window";
    public static final String ACTION_OPEN_LINK_AT_STACK_ROOT = "open_link_at_stack_root";

    @Deprecated
    public static final String ACTION_GO_BACK_TO_H5_HOME_OLD = "goHome";
    public static final String ACTION_GO_BACK_TO_H5_HOME = "go_home";

    /**
     * 配置返回按钮，包括虚拟键、物理键的返回按钮
     * 示例：jsbridge://config_back_btn?action=BASE64ENCODED_JS_STR
     * 当用户点击时会执行一次action对应的URI
     */
    public static final String ACTION_CONFIG_BACK_BTN = "config_back_btn";
    /**
     * 设置当前WebView加载的URL展示的TOOLBAR里面的标题
     */
    public static final String ACTION_SET_TITLE = "set_title";

}
