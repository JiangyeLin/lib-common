package com.trc.android.common.h5;

import android.annotation.TargetApi;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.trc.android.common.exception.ExceptionManager;
import com.trc.android.common.util.ContactSelectUtil;
import com.trc.android.common.util.NullUtil;
import com.trc.android.common.util.PicturesSelectUtil;
import com.trc.common.R;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import static com.trc.android.common.h5.ParamsUtil.getBase64EncodedParameter;

/**
 * @author HanTuo on 2017/3/22.
 */

public class WebViewHelper {
    private FragmentActivity activity;
    private WebViewClientInterface webViewClientInterface;

    private View rootView;
    private TrWebView webView;


    private ProgressBar progressBar;
    private FrameLayout errorCoverViewContainer;
    private ToolbarInterface toolbarInterface;
    private String originUrl;
    private String fixedTitle;
    private HashMap<String, String> backActionMap = new HashMap<>(1);
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean needClearHistory;
    private HashMap<String, String> configTitleMap = new HashMap<>(1);

    private View errorCoverView;
    private ViewGroup toolbarContainer;

    public WebViewHelper(FragmentActivity fragmentActivity) {
        activity = fragmentActivity;
        activity.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            private void antiMemoryLeak() {
                ViewParent parent = webView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(webView);
                }
                webView.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                webView.clearHistory();
                webView.removeAllViews();
                webView.destroy();
            }
        });
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

        progressBar = rootView.findViewById(R.id.progressBar);
        errorCoverViewContainer = rootView.findViewById(R.id.container);
        toolbarContainer = rootView.findViewById(R.id.customToolbarContainer);

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


    /**
     * @param url WebView第一个加载的链接，作为根链接
     */
    public WebViewHelper setOriginUrl(String url) {
        originUrl = url;
        return this;
    }

    /**
     * 关闭窗口(关闭Activity)
     */
    public void closeWindow() {
        webViewClientInterface.closeWindow();
    }

    /**
     * 设置自定义Toolbar
     *
     * @param toolbar
     * @return
     */
    public WebViewHelper setCustomToolbar(ToolbarInterface toolbar) {
        toolbarInterface = toolbar;
        toolbar.onAttach(toolbarContainer, this, activity);
        return this;
    }


    public WebViewHelper setConnectErrorCover(View connectErrorCoverView, View reloadBtn) {
        connectErrorCoverView.setVisibility(View.GONE);
        errorCoverView = connectErrorCoverView;
        errorCoverViewContainer.addView(connectErrorCoverView);
        if (null != reloadBtn) {
            reloadBtn.setOnClickListener(v -> reload());
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
        toolbarContainer.setVisibility(visibility);
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
                    toolbarInterface.onSetTitle(fixedTitle);
                } else {
                    if (configTitleMap.get(curUrl) != null) {
                        toolbarInterface.onSetTitle(configTitleMap.get(curUrl));
                    } else {
                        if (null == title) {
                            toolbarInterface.onSetTitle(null);
                        } else {
                            String titleLower = title.toLowerCase();
                            if (titleLower.startsWith("http://") || titleLower.startsWith("https://")) {
                                toolbarInterface.onSetTitle(null);
                            } else {
                                toolbarInterface.onSetTitle(title);
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

            //Andorid 4.1+
            @Override
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                super.openFileChooser(valueCallback, acceptType, capture);
                handleFileChooser(valueCallback, acceptType);
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
                    toolbarInterface.onSetTitle(fixedTitle);
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
                toolbarInterface.onPageFinished(url);

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
            loadUrl(url);

            Uri uri = Uri.parse(url.replace("#", "ANTI_FRAGMENT"));
            String toolbarTitle = uri.getQueryParameter("toolbarTitle");
            setFixedTitle(toolbarTitle);
            toolbarInterface.onSetTitle(toolbarTitle);

            if (url.contains("hideToolbar=true")) {
                showToolbar(false);
            } else if (url.contains("hideToolbar=false")) {
                showToolbar(true);
            }

            String toolbarConfigs = uri.getQueryParameter("configToolbar");
            if (!TextUtils.isEmpty(toolbarConfigs)) {
                configToolbar(Uri.parse("jsbridge://config_toolbar_btns?params=" + toolbarConfigs), url);
            }
            return true;
        }
        return false;
    }


    private boolean handleWebViewScheme(Uri uri) {
        if (WebViewScheme.SCHEME.equals(uri.getScheme())) {
            switch (uri.getHost()) {
                case WebViewScheme.ACTION_SET_TITLE://设置页面Title，如果这里设置了title，那么不使用DOM里面接收的TITLE
                    String title = uri.getQueryParameter("title");
                    toolbarInterface.onSetTitle(title);
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
                case WebViewScheme.ACTION_SET_COOKIE:
                    String domain = ParamsUtil.getBase64EncodedParameter(uri, "domain");
                    String key = ParamsUtil.getBase64EncodedParameter(uri, "key");
                    String value = ParamsUtil.getBase64EncodedParameter(uri, "value");
                    CookieUtil.setCookie(domain, key, value);
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
                    toolbarInterface.onConfigOptionMenu(uri);
                    return true;
                case WebViewScheme.ACTION_CONFIG_TOOLBAR_BTNS://配置Toolbar的按钮（文字按钮&图片按钮&图文按钮）
                case WebViewScheme.ACTION_CONFIG_TOOLBAR_BTNS_OLD://配置Toolbar的按钮（文字按钮&图片按钮&图文按钮）
                    toolbarInterface.onConfigToolbar(uri, webView.getUrl());
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


    /**
     * @return true 菜单显示状态被关闭 false 菜单原本就未显示
     */
    public boolean consumeBackEvent() {
        boolean consumedByToolbar = toolbarInterface.onBackBtnPress();
        if (consumedByToolbar) {
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


    public View getView() {
        return rootView;
    }

    public TrWebView getWebView() {
        return webView;
    }

    /**
     * @param uri jsbridge://config_toolbar_btns?params=BASE64_ENCODED_JSON_ARRAY
     * @param url 如果配置当前URL，则使用webView.getUrl()
     */
    public void configToolbar(Uri uri, String url) {
        toolbarInterface.onConfigToolbar(uri, url);
    }

    public void loadUrl() {
        if (null == toolbarInterface) {
            toolbarInterface = new DefaultToolbar();
            toolbarInterface.onAttach(toolbarContainer, this, activity);
        }
        handleUri(originUrl);
    }

    public abstract static class WebViewClientInterface {

        /*
         * 新的URL被拦截，自己处理返回true，交给WebViewHelper处理返回false
         */
        public boolean onLoadUrl(final String url) {
            return false;
        }

        public void onPageFinished(String url) {
        }

        public void onReceivedError(int errorCode, String description, String failingUrl) {
        }

        public void onProgressChanged(int newProgress) {

        }

        /**
         * @param url 该URL无法被APP处理，一般可以用系统Intent方式处理
         */
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
