package com.trc.android.common.h5;

import com.google.gson.annotations.SerializedName;

/**
 * @author HanTuo on 2017/3/22.
 */

public class WebActionItem {
    @SerializedName("title")
    public String title;
    @SerializedName("action")
    public String action;
    @SerializedName("icon")
    public String icon;
    @SerializedName("badge")
    public String badge;
    @SerializedName("backgroundColor")
    public String backgroundColor;
    @SerializedName("fontColor")
    public String fontColor;
    public Integer iconRes;//本地图标配置
}
