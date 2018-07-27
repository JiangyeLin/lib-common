package com.trc.android.common.h5;

import com.google.gson.annotations.SerializedName;

/**
 * @author HanTuo on 2017/3/22.
 */

public class WebActionItem {
    /**
     * 按钮的文字
     */
    @SerializedName("title")
    public String title;
    /**
     * 点击配置按钮后进行的操作：可以是一个Uri,例如http://www.baidu.com 或 trc://share?params=BASE64_ENCODED_SHARE_URI
     * 也可以是一个javascript:开头的字符串，例如javascript://onClickShare()，该Action执行的时候会调用当前H5页面的onClickShare()的JS方法
     */
    @SerializedName("action")
    public String action;
    /**
     * 按钮的图片链接，如果没有配置则为文字按钮
     */
    @SerializedName("icon")
    public String icon;
    /**
     * 按钮右上角的文字角标
     */
    @SerializedName("badge")
    public String badge;
    /**
     * 按钮的背景颜色
     */
    @SerializedName("backgroundColor")
    public String backgroundColor;
    /**
     * 按钮的字体颜色
     */
    @SerializedName("fontColor")
    public String fontColor;
    /**
     * 本地图标配置的R资源ID
     */
    public Integer iconRes;
}
