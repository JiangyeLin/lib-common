package com.trc.android.common.h5;

@SuppressWarnings("DeprecatedIsStillUsed")
public class WebViewScheme {
    public static final String SCHEME = "jsbridge";

    @Deprecated
    public static final String ACTION_CLOSE_WINDOW_OLD = "exitModule";//关闭Page
    public static final String ACTION_CLOSE_WINDOW = "close_window";//关闭Page
    @Deprecated
    public static final String ACTION_CLEAR_HISTORY_OLD = "clearHistory";//清除网页堆栈记录
    public static final String ACTION_CLEAR_HISTORY = "clear_history";//清除网页堆栈记录

    @Deprecated
    public static final String ACTION_CONFIG_OPTION_MENU_OLD = "configOptionMenu";//配置toolbar右上角里面的菜单
    public static final String ACTION_CONFIG_OPTION_MENU = "config_option_menu";//配置toolbar右上角里面的菜单
    @Deprecated
    public static final String ACTION_CONFIG_TOOLBAR_BTNS_OLD = "configToolbarBtns";//toolbar里面的按钮
    public static final String ACTION_CONFIG_TOOLBAR_BTNS = "config_toolbar_btns";//toolbar里面的按钮
    @Deprecated
    public static final String ACTION_GO_BACK_OLD = "goBack";//返回上个页面
    public static final String ACTION_GO_BACK = "go_back";//返回上个页面
    @Deprecated
    public static final String ACTION_SELECT_CONTACT_OLD = "getContactPhoneNum";//充值获取手机号码
    public static final String ACTION_SELECT_CONTACT = "select_contact";//充值获取手机号码
    public static final String ACTION_RELOAD = "reload";//页面刷新
    public static final String ACTION_OPEN_LINK_IN_NEW_WINDOW = "open_link_in_new_window";
    public static final String ACTION_OPEN_LINK_AT_STACK_ROOT = "open_link_at_stack_root";

    @Deprecated
    public static final String ACTION_GO_BACK_TO_H5_HOME_OLD = "goHome";
    public static final String ACTION_GO_BACK_TO_H5_HOME = "go_home";

    public static final String ACTION_CONFIG_BACK_BTN = "config_back_btn"; //获取地区信息 ?action=BASE64ENCODED_JS_STR
    public static final String ACTION_SET_TITLE = "set_title"; //H5通知去请求购物袋中商品的数量 jsbridge://set_title?title=购物车(1)"
}
