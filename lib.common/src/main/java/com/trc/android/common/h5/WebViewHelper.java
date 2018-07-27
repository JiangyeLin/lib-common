package com.trc.android.common.h5;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.trc.android.common.exception.ExceptionManager;
import com.trc.android.common.util.ContactSelectUtil;
import com.trc.android.common.util.DensityUtil;
import com.trc.android.common.util.ImgUtil;
import com.trc.android.common.util.NullUtil;
import com.trc.android.common.util.PicturesSelectUtil;
import com.trc.common.R;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.trc.android.common.h5.ParamsUtil.getBase64EncodedParameter;

/**
 * @author HanTuo on 2017/3/22.
 */

public class WebViewHelper {
    private FragmentActivity activity;
    private WebViewClientInterface webViewClientInterface;

    private View rootView;
    private WebView webView;
    private TextView tvTitle;
    private LinearLayout llToolbarBtnContainer;
    private ProgressBar progressBar;
    private FrameLayout errorCoverViewContainer;

    private String originUrl;
    private String fixedTitle;
    private HashMap<String, String> backActionMap = new HashMap<>(1);
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean needClearHistory;
    private HashMap<String, List<WebActionItem>> toolbarCache = new HashMap<>();//记录配置了Toolbar的H5页面
    private HashMap<String, String> configTitleMap = new HashMap<>(1);
    private View errorCoverView;

    public WebViewHelper(FragmentActivity fragmentActivity) {
        activity = fragmentActivity;
    }


    public static WebViewHelper create(FragmentActivity activity) {
        WebViewHelper webViewHelper = new WebViewHelper(activity);
        webViewHelper.initViews();
        webViewHelper.setupChromeClient();
        webViewHelper.setupWebClient();
        return webViewHelper;
    }

    private void initViews() {
        rootView = LayoutInflater.from(activity).inflate(R.layout.lib_common_fragment_webview, null);
        webView = rootView.findViewById(R.id.webView);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        tvTitle = rootView.findViewById(R.id.tvTitle);
        llToolbarBtnContainer = rootView.findViewById(R.id.toolbarBtnContainer);
        progressBar = rootView.findViewById(R.id.progressBar);
        errorCoverViewContainer = rootView.findViewById(R.id.container);

        rootView.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webViewClientInterface.closeWindow();
            }
        });
        webView.setVerticalScrollBarEnabled(false);
        //解决webView与refreshLayout滑动冲突
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> webView.getWebScrollY() > 0);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!webViewClientInterface.onRefresh()) {
                reload();
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setEnabled(false);
                    }
                }, 2000);
            }
        });
    }


    public WebViewHelper setOriginUrl(String url) {
        originUrl = url;
        return this;
    }

    public WebViewHelper setConnectErrorCover(View connectErrorCoverView, View reloadBtn) {
        connectErrorCoverView.setVisibility(View.GONE);
        errorCoverView = connectErrorCoverView;
        errorCoverViewContainer.addView(connectErrorCoverView);
        if (null != reloadBtn) {
            reloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload();
                }
            });
        }
        return this;
    }

    public WebViewHelper setConnectErrorCover(@LayoutRes int layoutId, @IdRes int btnId) {
        View failCover = LayoutInflater.from(activity).inflate(layoutId, errorCoverViewContainer, false);
        View reloadBtn = failCover.findViewById(btnId);
        return setConnectErrorCover(failCover, reloadBtn);
    }

    public WebViewHelper setFixedTitle(String title) {
        fixedTitle = title;
        return this;
    }

    public WebViewHelper showToolbar(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        rootView.findViewById(R.id.toolbarLayout).setVisibility(visibility);
        rootView.findViewById(R.id.divider).setVisibility(visibility);
        return this;
    }

    private void reload() {
        if (null != errorCoverView) {
            errorCoverView.setVisibility(View.GONE);
        }
        webView.reload();
    }

    private void loadUrl(String url) {
        if (null != errorCoverView) {
            errorCoverView.setVisibility(View.GONE);
        }
        webView.loadUrl(url);
    }

    public WebViewHelper setClientInterface(WebViewClientInterface clientInterface) {
        webViewClientInterface = clientInterface;
        return this;
    }


    private void setupChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                webViewClientInterface.onProgressChanged(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                String curUrl = view.getUrl();
                if (NullUtil.equal(originUrl, curUrl) && NullUtil.notEmpty(fixedTitle)) {
                    tvTitle.setText(fixedTitle);
                } else {
                    if (configTitleMap.get(curUrl) != null) {
                        tvTitle.setText(configTitleMap.get(curUrl));
                    } else {
                        if (null == title) {
                            tvTitle.setText(null);
                        } else {
                            String titleLower = title.toLowerCase();
                            if (titleLower.startsWith("http://") || titleLower.startsWith("https://")) {
                                tvTitle.setText(null);
                            } else {
                                tvTitle.setText(title);
                            }
                        }
                    }
                }
            }

            private ValueCallback valueCallback;


            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                String acceptTypes = Arrays.toString(fileChooserParams.getAcceptTypes()).toLowerCase();
                handleFileChooser(filePathCallback, acceptTypes);
                return true;
            }

            // Andorid 4.1+
//            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
//                handleFileChooser(uploadFile, acceptType);
//            }


            @Override
            public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {
                super.openFileChooser(valueCallback, s, s1);
                handleFileChooser(valueCallback, s);
            }

            // Andorid 3.0 +
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
                handleFileChooser(uploadFile, acceptType);
            }


            private void handleFileChooser(ValueCallback filePathCallback, String acceptTypes) {
                try {
                    valueCallback = filePathCallback;
                    PicturesSelectUtil.select(activity, false, new PicturesSelectUtil.OnPicturesCallback() {
                        @Override
                        public void onSelect(File file) {
                            valueCallback.onReceiveValue(Uri.fromFile(file));
                        }

                        @Override
                        public void onCancel() {
                            valueCallback.onReceiveValue(null);
                        }
                    });
                } catch (Exception e) {
                    ExceptionManager.handle(e);
                }
            }

        });
    }

    private void setupWebClient() {
        webView.setWebViewClient(new WebViewClient() {
            boolean pageFailToOpen = false;

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
                //super.onReceivedSslError(webView, sslErrorHandler, sslError);
                //在重写WebViewClient的onReceivedSslError方法时，注意一定要去除onReceivedSslError方法的super.onReceivedSslError(view, handler, error);，否则设置无效。
                sslErrorHandler.proceed();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pageFailToOpen = false;
                if (NullUtil.equal(url, originUrl)) {
                    tvTitle.setText(fixedTitle);
                }
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                swipeRefreshLayout.setEnabled(!url.contains("disableRefresh=false"));
                if (null == originUrl) {
                    originUrl = webView.getUrl();
                }
                boolean result = handleUri(url);
                return result;
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                pageFailToOpen = true;
                if (null != errorCoverView) {
                    errorCoverView.setVisibility(View.VISIBLE);
                }
                webViewClientInterface.onReceivedError(errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (null != webViewClientInterface) {
                    webViewClientInterface.onPageFinished(url);
                }
                if (null != progressBar) {
                    progressBar.setVisibility(View.GONE);
                }
                if (null != swipeRefreshLayout) {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(pageFailToOpen);
                }
                if (needClearHistory) {
                    needClearHistory = false;
                    webView.clearHistory();
                }
                updateToolbarBtns();
            }
        });
    }

    public boolean handleUri(String url) {
        try {
            Uri uri = Uri.parse(url);
            //先交给WebViewHelper的webViewClientInterface处理
            if (webViewClientInterface.onLoadUrl(url)) {
                return true;
            } else if (handleWebViewScheme(uri)) {
                //交给内置的处理jsbridge://的代码处理
                return true;
            } else if (handleHttpLink(url)) {
                //处理Http、Https链接
                return false;
            } else if (url.startsWith("javascript:")) {
                webView.loadUrl(url);
            } else {//调用系统处理URI
                webViewClientInterface.onNoResolver(url);
            }
        } catch (Exception e) {
            ExceptionManager.handle(e);
        }
        return true;
    }


    private boolean handleHttpLink(String url) {
        if (url.toLowerCase().startsWith("http")) {
            tvTitle.setText(null);
            loadUrl(url);
            return true;
        }
        return false;
    }


    private boolean handleWebViewScheme(Uri uri) {
        if (WebViewScheme.SCHEME.equals(uri.getScheme())) {
            switch (uri.getHost()) {
                case WebViewScheme.ACTION_SET_TITLE://设置页面Title，如果这里设置了title，那么不使用DOM里面接收的TITLE
                    String title = uri.getQueryParameter("title");
                    tvTitle.setText(title);
                    configTitleMap.put(webView.getUrl(), title);
                    return true;
                case WebViewScheme.ACTION_OPEN_LINK_IN_NEW_WINDOW://新的Activity打开网页
                    String url = getBase64EncodedParameter(uri, "url");
                    webViewClientInterface.openLinkInNewWindow(url);
                    return true;
                case WebViewScheme.ACTION_OPEN_LINK_AT_STACK_ROOT://清除所有访问记录并打开网页
                    String link = getBase64EncodedParameter(uri, "url");
                    if (null != link) {
                        link = webViewClientInterface.transformUrl(link);
                        if (link.toLowerCase().startsWith("http")) {
                            loadUrl(link);
                            needClearHistory = true;
                        }
                    }
                    return true;
                case WebViewScheme.ACTION_GO_BACK_TO_H5_HOME://回到H5应用的首页
                case WebViewScheme.ACTION_GO_BACK_TO_H5_HOME_OLD://回到H5应用的首页
                    loadUrl(originUrl);
                    needClearHistory = true;
                    return true;
                case WebViewScheme.ACTION_GO_BACK_OLD://返回上一个加载的页面
                case WebViewScheme.ACTION_GO_BACK://返回上一个加载的页面
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        activity.finish();
                    }
                    return true;
                case WebViewScheme.ACTION_CLOSE_WINDOW_OLD://推出所有的H5页面,关闭Activity
                case WebViewScheme.ACTION_CLOSE_WINDOW://推出所有的H5页面,关闭Activity
                    activity.finish();
                    return true;
                case WebViewScheme.ACTION_CLEAR_HISTORY://清除所有历史记录
                case WebViewScheme.ACTION_CLEAR_HISTORY_OLD://清除所有历史记录
                    needClearHistory = true;
                    return true;
                case WebViewScheme.ACTION_CONFIG_OPTION_MENU://配置更多菜单
                case WebViewScheme.ACTION_CONFIG_OPTION_MENU_OLD://配置更多菜单
                    showOptionMenu(uri);
                    return true;
                case WebViewScheme.ACTION_CONFIG_TOOLBAR_BTNS://配置Toolbar的按钮（文字按钮&图片按钮&图文按钮）
                case WebViewScheme.ACTION_CONFIG_TOOLBAR_BTNS_OLD://配置Toolbar的按钮（文字按钮&图片按钮&图文按钮）
                    configToolbar(uri);
                    updateToolbarBtns();
                    return true;
                case WebViewScheme.ACTION_RELOAD://重新加载
                    reload();
                    return true;
                case WebViewScheme.ACTION_CONFIG_BACK_BTN://H5配置点击返回按钮的事件，一次配置有效一次，一个页面只能配置一次
                    String backAction = getBase64EncodedParameter(uri, "action");
                    backActionMap.put(webView.getUrl(), backAction);
                    return true;
                case WebViewScheme.ACTION_SELECT_CONTACT_OLD:
                case WebViewScheme.ACTION_SELECT_CONTACT:
                    ContactSelectUtil.select(activity, new ContactSelectUtil.SelectCallback() {
                        @Override
                        public void onSelectSuccess(String name, String phone) {
                            webView.loadUrl("javascript:onSelectContact(" + name + "," + phone + ")");
                        }
                    });
                    return true;
                case WebViewScheme.ACTION_CLEAR_CACHE:
                    webView.clearCache(true);
                    return true;
                case WebViewScheme.ACTION_CLEAR_COOKIE:
                    CookieUtil.clearCookie();
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }


    public void configToolbar(Uri uri) {
        String json = getBase64EncodedParameter(uri, "params");
        List<WebActionItem> actionItemList = new Gson().fromJson(json, new TypeToken<List<WebActionItem>>() {
        }.getType());
        toolbarCache.put(webView.getUrl(), actionItemList);
    }

    /**
     * @return true 菜单显示状态被关闭 false 菜单原本就未显示
     */
    public boolean consumeBackEvent() {
        boolean isShowing = null != popupWindow && popupWindow.isShowing();
        if (isShowing) {
            popupWindow.dismiss();
            return true;
        } else {
            String backAction = backActionMap.get(webView.getUrl());
            backActionMap.remove(webView.getUrl());
            if (TextUtils.isEmpty(backAction)) {
                if (webView.canGoBack()) {
                    webView.goBack();
                    return true;
                } else {
                    return false;
                }
            } else {
                if (backAction.toLowerCase().startsWith("http")) {
                    loadUrl(backAction);
                } else if (backAction.startsWith("trmall://") || backAction.startsWith("jsbridge://")) {
                    handleUri(backAction);
                } else {
                    loadUrl("javascript:" + backAction);
                }
                return true;
            }
        }
    }

    public void updateToolbarBtns() {
        if (null != llToolbarBtnContainer) {
            if (toolbarCache.containsKey(webView.getUrl())) {
                llToolbarBtnContainer.setVisibility(View.VISIBLE);
            } else {
                llToolbarBtnContainer.setVisibility(View.GONE);
            }
            llToolbarBtnContainer.removeAllViews();
            List<WebActionItem> actionItems = toolbarCache.get(webView.getUrl());
            if (null != actionItems) {
                for (final WebActionItem actionItem : actionItems) {
                    View vItem = LayoutInflater.from(activity).inflate(R.layout.lib_common_toolbar_btn_layout, llToolbarBtnContainer, false);
                    setUpActionBtn(actionItem, vItem);
                    llToolbarBtnContainer.addView(vItem);
                }
            }
        }

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
            handleUri(actionItem.action);
        });
        ImageView ivBg = vItem.findViewById(R.id.ivBg);
        if (!isEmpty(actionItem.backgroundColor)) {
            ivBg.setVisibility(View.VISIBLE);
            ivBg.setColorFilter(Color.parseColor(actionItem.backgroundColor));
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
            marginLayoutParams.leftMargin = marginLayoutParams.rightMargin = (int) DensityUtil.dip2px(15);
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

    public View getView() {
        return rootView;
    }

    public WebView getWebView() {
        return webView;
    }

    public void loadUrl() {
        loadUrl(originUrl);
    }

    public abstract static class WebViewClientInterface {

        public boolean onLoadUrl(final String url) {
            return false;
        }

        public void onPageFinished(String url) {
        }

        public void onReceivedError(int errorCode, String description, String failingUrl) {
        }

        public void onProgressChanged(int newProgress) {

        }

        public void onNoResolver(String url) {

        }

        public abstract void openLinkInNewWindow(String url);

        public String transformUrl(String url) {
            return url;
        }

        public abstract void closeWindow();

        public boolean onRefresh() {
            return false;
        }

    }

}
