package com.trc.android.common.h5;

public class WebViewScheme {
    public static final String SCHEME = "jsbridge";

    public static final String ACTION_EXIT_H5 = "exitModule";//关闭Page
    public static final String ACTION_CLEAR_HISTORY = "clearHistory";//清除网页堆栈记录
    public static final String ACTION_CONFIG_OPTION_MENU = "configOptionMenu";//配置toolbar右上角里面的菜单
    public static final String ACTION_CONFIG_TOOLBAR_BTNS = "configToolbarBtns";//toolbar里面的按钮
    public static final String ACTION_GO_BACK = "goBack";//返回上个页面
    public static final String ACTION_GET_PHONE_NUM = "getContactPhoneNum";//充值获取手机号码
    //jsbridge://configSearchIcon?params=WEBSAFE_BASE64_ENCODED_STRING
    public static final String ACTION_RELOAD = "reload";//页面刷新
    /**
     * trmall://open_link_in_new_window?url=DCAKU2KCOSOIJF
     * - 在新的窗口打开链接 要打开的URL使用BASE64 WEBSAFE 编码
     */
    public static final String ACTION_OPEN_LINK_IN_NEW_WINDOW = "open_link_in_new_window";
    public static final String ACTION_OPEN_LINK_AT_STACK_ROOT = "open_link_at_stack_root";
    public static final String ACTION_GO_BACK_TO_H5_HOME = "goHome";
    public static final String ACTION_PAY_BY_ALIPAY = "pay_by_alipay";//支付宝支付, 参数params=payinfo_base64_encoded
    public static final String ACTION_PAY_BY_WECHAT = "pay_by_wechat";//微信支付, 参数params=payinfo_base64_encoded
    public static final String ACTION_COOPERATE_APP = "cooperate_app";//打开第三方应用 trmall://cooperate_app?scheme=XIJIJSIDJ&ios_download_url=XLIDIOSFUIO&android_download_url&bundleId=XKCJOSIDJOI&packageName=SJOICUO
    public static final String ACTION_TRC_PAYBOX = "pay_by_trcpay";//泰然城收银台 jsbridge://pay_by_trcpay?payid=GW2016101910593622311626
    public static final String ACTION_TRC_CHOOSEPIC = "choosePic";//泰然上传图片 jsbridge://choosePic?maxSize=2000&size=0&crop=false&postUrl=XOIDJSOIO
    public static final String ACTION_SCAN_ECARD = "scan_ecard";
    public static final String ACTION_PAY_BY_INSTALLMENT = "pay_by_installment";//?payid=KXKXKK
    public static final String ACTION_GET_AREA = "getAreaInfo"; //获取地区信息
    public static final String ACTION_CONFIG_BACK_BTN = "config_back_btn"; //获取地区信息 ?action=BASE64ENCODED_JS_STR
    public static final String ACTION_VIEWPAGER_CONTROL = "viewpager"; //控制viewpager左右滑动，以支持H5的banner正常滚动 state false禁止滑动，true启动滑动
    public static final String ACTION_SET_TITLE = "set_title"; //H5通知去请求购物袋中商品的数量 jsbridge://set_title?title=购物车(1)"
    public static final String ACTION_HANDLE_TOUCH = "handleTouch"; //H5通知是否在滑动状态 jsbridge://handleTouch?block=true  true在滑动状态，false松开状态
}
