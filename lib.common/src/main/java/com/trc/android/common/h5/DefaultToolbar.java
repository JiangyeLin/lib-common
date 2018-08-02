package com.trc.android.common.h5;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trc.android.common.util.ImgUtil;
import com.trc.common.R;

import java.util.HashMap;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.trc.android.common.h5.ParamsUtil.getBase64EncodedParameter;

public class DefaultToolbar implements ToolbarInterface {

    private HashMap<String, List<WebActionItem>> toolbarCache = new HashMap<>();//记录配置了Toolbar的H5页面

    protected TextView tvTitle;
    protected LinearLayout llToolbarBtnContainer;
    protected WebViewHelper webViewHelper;
    protected Activity activity;
    protected TrWebView webView;
    private String updateToken;


    @Override
    public void onAttach(ViewGroup container, WebViewHelper helper, Activity host) {
        activity = host;
        webViewHelper = helper;
        webView = webViewHelper.getWebView();

        LayoutInflater.from(container.getContext()).inflate(R.layout.lib_common_default_h5_toolbar, container, true);
        tvTitle = container.findViewById(R.id.tvTitle);
        llToolbarBtnContainer = container.findViewById(R.id.toolbarBtnContainer);
        View btnClose = container.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> webViewHelper.closeWindow());
    }

    @Override
    public void onSetTitle(String title) {
        tvTitle.setText(title);
    }

    @Override
    public void onConfigToolbar(Uri uri, String url) {
        String json = getBase64EncodedParameter(uri, "params");
        List<WebActionItem> actionItemList = new Gson().fromJson(json, new TypeToken<List<WebActionItem>>() {
        }.getType());
        toolbarCache.put(url, actionItemList);
        updateToolbarBtns();
    }

    @Override
    public void onConfigOptionMenu(Uri uri) {
        showOptionMenu(uri);
    }

    @Override
    public void onPageFinished(String url) {
        updateToolbarBtns();
    }

    @Override
    public boolean onBackBtnPress() {
        boolean isShowing = null != popupWindow && popupWindow.isShowing();
        if (isShowing) {
            popupWindow.dismiss();
            return true;
        } else {
            return false;
        }
    }

    public void updateToolbarBtns() {
        if (null != llToolbarBtnContainer) {
            String currentUrl = webView.getUrl();
            if (currentUrl.equals(updateToken)) {//已经设置过了
                return;
            } else {
                updateToken = currentUrl;//记录已经设置过了
            }
            if (toolbarCache.containsKey(currentUrl)) {
                llToolbarBtnContainer.setVisibility(View.VISIBLE);
            } else {
                llToolbarBtnContainer.setVisibility(View.GONE);
            }
            llToolbarBtnContainer.removeAllViews();
            List<WebActionItem> actionItems = toolbarCache.get(currentUrl);
            if (null != actionItems) {
                for (final WebActionItem actionItem : actionItems) {
                    View vItem = LayoutInflater.from(activity).inflate(R.layout.lib_common_toolbar_btn_layout, llToolbarBtnContainer, false);
                    setUpActionBtn(actionItem, vItem);
                    llToolbarBtnContainer.addView(vItem);
                }
            }
        }
    }

    PopupWindow popupWindow;

    private void showOptionMenu(Uri uri) {
        String json = getBase64EncodedParameter(uri, "params");
        List<WebActionItem> menuActionItemList = new Gson().fromJson(json, new TypeToken<List<WebActionItem>>() {
        }.getType());
        if (null == menuActionItemList || menuActionItemList.isEmpty()) {
            return;
        }
        if (null == popupWindow) {
            popupWindow = new PopupWindow(activity);
        }
        View contentView = popupWindow.getContentView();
        if (null == contentView) {
            contentView = LayoutInflater.from(activity).inflate(R.layout.lib_common_option_menu_layout, null);
            popupWindow.setContentView(contentView);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
        }
        final LinearLayout container = contentView.findViewById(R.id.container);
        container.removeAllViews();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);

        for (WebActionItem actionItem : menuActionItemList) {
            View vItem = layoutInflater.inflate(R.layout.lib_common_toolbar_menu_layout, container, false);
            setUpActionBtn(actionItem, vItem);
            container.addView(vItem);
            View divider = new View(activity);
            LinearLayout.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            marginLayoutParams.leftMargin = marginLayoutParams.rightMargin = (int) dip2px(15);
            divider.setLayoutParams(marginLayoutParams);
            divider.setBackgroundColor(Color.WHITE);
            container.addView(divider);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            popupWindow.showAsDropDown(llToolbarBtnContainer, 0, 0, Gravity.RIGHT);
        } else {
            popupWindow.showAsDropDown(llToolbarBtnContainer);
        }
    }

    public float dip2px(float dpValue) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    private void setUpActionBtn(final WebActionItem actionItem, View vItem) {
        TextView tvTitle = vItem.findViewById(R.id.tvTitle);
        if (isEmpty(actionItem.title)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(actionItem.title);
        }
        if (!isEmpty(actionItem.fontColor)) {
            tvTitle.setTextColor(Color.parseColor(actionItem.fontColor));
        }

        TextView tvBadge = vItem.findViewById(R.id.tvBadge);
        if (isEmpty(actionItem.badge)) {
            tvBadge.setVisibility(View.GONE);
        } else {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(actionItem.badge);
        }

        ImageView ivIcon = vItem.findViewById(R.id.ivIcon);
        boolean showIvIcon = false;
        if (null != actionItem.iconRes) {
            showIvIcon = true;
            ivIcon.setImageResource(actionItem.iconRes);
        } else if (!isEmpty(actionItem.icon)) {
            showIvIcon = true;
            ivIcon.setVisibility(View.VISIBLE);
            ImgUtil.load(actionItem.icon, ivIcon);
        }
        ivIcon.setVisibility(showIvIcon ? View.VISIBLE : View.GONE);

        vItem.setOnClickListener(v -> {
            if (null != popupWindow && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            webViewHelper.handleUri(actionItem.action);
        });
        ImageView ivBg = vItem.findViewById(R.id.ivBg);
        if (!isEmpty(actionItem.backgroundColor)) {
            ivBg.setVisibility(View.VISIBLE);
            ivBg.setColorFilter(Color.parseColor(actionItem.backgroundColor));
        }
    }


}
