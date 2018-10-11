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
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.trc.android.common.exception.ExceptionManager;
import com.trc.android.common.h5.devtool.FloatingButton;
import com.trc.android.common.h5.devtool.HtmlFormatterUtil;
import com.trc.android.common.h5.devtool.WebDevTool;
import com.trc.android.common.h5.devtool.WebviewRecorderModel;
import com.trc.android.common.util.ContactSelectUtil;
import com.trc.android.common.util.FileUtil;
import com.trc.android.common.util.LogUtil;
import com.trc.android.common.util.NullUtil;
import com.trc.android.common.util.PicturesSelectUtil;
import com.trc.common.R;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.trc.android.common.h5.ParamsUtil.getBase64EncodedParameter;

/**
 * @author HanTuo on 2017/3/22.
 */

public class WebViewHelper {
    private FragmentActivity activity;
    private WebViewClientInterface webViewClientInterface;

    private View rootView;
    private TrWebView webView;

    private boolean isDebug;
    private ProgressBar progressBar;
    private FrameLayout errorCoverViewContainer;
    private ToolbarInterface toolbarInterface;
    private String originUrl;
    private String fixedTitle;
    private HashMap<String, String> backActionMap = new HashMap<>(1);
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean needClearHistory;
    private HashMap<String, String> configTitleMap = new HashMap<>(1);
    private WebDevTool webDevTool;
    private View errorCoverView;
    private ViewGroup toolbarContainer;
    private View debugBtn;

    public WebViewHelper(FragmentActivity fragmentActivity) {
        activity = fragmentActivity;
    }

    private void registLifecycle() {
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

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            private void onResume() {
                if (isDebug) {
                    webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_RESUME, "OnResume"));
                }
                webView.onResume();
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            private void onPause() {
                if (isDebug) {
                    webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.kEY_PAUSE, "OnPause"));
                }
                webView.onPause();
            }
        });
    }


    public static WebViewHelper create(FragmentActivity activity) {
        WebViewHelper webViewHelper = new WebViewHelper(activity);
        webViewHelper.initViews();
        webViewHelper.setupChromeClient();
        webViewHelper.setupWebClient();
        webViewHelper.registLifecycle();
        return webViewHelper;
    }

    public WebViewHelper setDebug(boolean debug) {
        isDebug = debug;
        if (isDebug && null == webDevTool) {
            webDevTool = new WebDevTool(this);
            webDevTool.setUA(webView.getSettings().getUserAgentString());
            webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        }
        WebView.setWebContentsDebuggingEnabled(debug);
        return this;
    }

    public final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            new Thread(() -> {
                HtmlFormatterUtil.formatter(html, FileUtil.getShareFile("WebDebug.html"));
            }).start();
        }
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
                swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setEnabled(false), 2000);
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
     */
    public WebViewHelper setCustomToolbar(ToolbarInterface toolbar) {
        toolbarInterface = toolbar;
        toolbar.onAttach(toolbarContainer, this, activity);
        return this;
    }

    /**
     * 设置网络不同时显示的错误页面，
     * 参考{@link #setConnectErrorCover(int, int)}
     *
     * @param connectErrorCoverView 设置的View会被添加到特定FrameLayout中
     * @param reloadBtn             该Button会被设置点击事件，点击后reload当前页面
     * @return
     */
    public WebViewHelper setConnectErrorCover(View connectErrorCoverView, View reloadBtn) {
        connectErrorCoverView.setVisibility(View.GONE);
        errorCoverView = connectErrorCoverView;
        errorCoverViewContainer.addView(connectErrorCoverView);
        if (null != reloadBtn) {
            reloadBtn.setOnClickListener(v -> reload());
        }
        return this;
    }

    /**
     * 设置网络不同时显示的错误页面，
     * 参考{@link #setConnectErrorCover(View, View)}
     *
     * @param layoutId 通过inflate资源ID为layoutId出来的的View会被添加到特定FrameLayout中
     * @param btnId    该Button会被设置点击事件，点击后reload当前页面
     * @return
     */
    public WebViewHelper setConnectErrorCover(@LayoutRes int layoutId, @IdRes int btnId) {
        View failCover = LayoutInflater.from(activity).inflate(layoutId, errorCoverViewContainer, false);
        View reloadBtn = failCover.findViewById(btnId);
        return setConnectErrorCover(failCover, reloadBtn);
    }

    /**
     * 设置首页固定的TITLE
     *
     * @param title
     * @return
     */
    public WebViewHelper setFixedTitle(String title) {
        fixedTitle = title;
        return this;
    }

    /**
     * 是否显示Toolbar
     *
     * @param show
     * @return
     */
    public WebViewHelper showToolbar(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        toolbarContainer.setVisibility(visibility);
        return this;
    }

    public void reload() {
        if (null != errorCoverView) {
            errorCoverView.setVisibility(View.GONE);
        }
        if (isDebug)
            webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_RELOAD, originUrl));

        webView.reload();
    }

    public void loadUrl(String url) {
        if (null != errorCoverView) {
            errorCoverView.setVisibility(View.GONE);
        }
        webView.loadUrl(url);
        if (isDebug) {
            webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_LOADURL, url));
        }
    }


    public WebViewHelper setClientInterface(WebViewClientInterface clientInterface) {
        webViewClientInterface = clientInterface;
        return this;
    }


    private void setupChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (isDebug) {
                    String consoleLog = String.format("%1$s -- From line %2$s of %3$s", consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
                    webDevTool.putConsoleLog(new WebviewRecorderModel(WebDevTool.KEY_CONSOLE, consoleLog));
                }

                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                webViewClientInterface.onProgressChanged(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                String finalTitle = title;
                String curUrl = view.getUrl();
                if (NullUtil.equal(originUrl, curUrl) && NullUtil.notEmpty(fixedTitle)) {//首页配置了固定标题
                    finalTitle = fixedTitle;
                } else {
                    if (configTitleMap.get(curUrl) != null) {
                        finalTitle = configTitleMap.get(curUrl);
                    } else if (null != title) {
                        if (title.startsWith("http://") || title.startsWith("https://")) {
                            finalTitle = null;
                        }
                    }
                }
                toolbarInterface.onSetTitle(finalTitle);
                webViewClientInterface.onReceiveTitle(finalTitle);
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
                    PicturesSelectUtil.select(activity, "选择图片需要相机权限", false, new PicturesSelectUtil.OnPicturesCallback() {
                        @Override
                        public void onSelect(File file, int type) {
                            Uri uri = Uri.fromFile(file);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                //X5内核可能会要求传递单个URI参数
                                try {
                                    valueCallback.onReceiveValue(new Uri[]{uri});
                                } catch (ClassCastException e) {
                                    valueCallback.onReceiveValue(uri);
                                }
                            } else {
                                try {
                                    valueCallback.onReceiveValue(uri);
                                } catch (ClassCastException e) {
                                    valueCallback.onReceiveValue(new Uri[]{uri});
                                }
                            }
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

                if (isDebug) {
                    webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_PAGESTART, url));
                    if (webDevTool.getCustomCookies() != null) {
                        for (WebviewRecorderModel model : webDevTool.getCustomCookies()) {
                            Uri uri = Uri.parse(url);
                            CookieUtil.setCookie(uri.getHost(), model.key, model.desc);
                        }
                    }

                    //获取cookie
                    CookieManager cookieManager = CookieManager.getInstance();
                    webDevTool.setSimpleCookies(cookieManager.getCookie(url));
                }
            }

            /**
             * 建立链接从header里面取token
             */
            private void executeRequest(WebResourceRequest request) {
                String scheme = request.getUrl().getScheme().trim();
                if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                    new Thread(() -> {
                        try {
                            String url = request.getUrl().toString();
                            URLConnection connection = new URL(url).openConnection();
                            connection.addRequestProperty("Cookie", CookieManager.getInstance().getCookie(url));
                            for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                                connection.addRequestProperty(entry.getKey(), entry.getValue());
                            }
                            connection.connect();
                            String cookie = connection.getHeaderField("Set-Cookie");
                            if (cookie != null) {
                                webDevTool.addDetailCookie(cookie);
                            }
                            connection.getInputStream().close();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                //记录 所有加载的资源(网页，接口，资源文件，js，css等)
                if (webDevTool != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                    String host = webResourceRequest.getUrl().getHost();
                    webDevTool.loadSources(host, webResourceRequest.getUrl().toString());

                    if (webDevTool.isDetailCookie() && "get".equalsIgnoreCase(webResourceRequest.getMethod())) {

                        executeRequest(webResourceRequest);
                    }
                }
                return super.shouldInterceptRequest(webView, webResourceRequest);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                swipeRefreshLayout.setEnabled(!url.contains("disableRefresh=false"));
                if (null == originUrl) {
                    originUrl = webView.getUrl();
                }
                boolean result = handleUri(url);
                if (isDebug) {
                    webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_OVERRIDE_URL_LOADING, url));
                }
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
                if (isDebug) {
                    webDevTool.putErrorLog(new WebviewRecorderModel(
                            WebDevTool.KEY_RECEIVE_ERROR, description + " " + failingUrl));
                }
            }

            //404等错误会回调这里
            @Override
            public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
                super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);

                if (isDebug) {
                    webDevTool.putErrorLog(new WebviewRecorderModel(String.valueOf(webResourceResponse.getStatusCode()), webResourceRequest.getUrl().toString()));
                }
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
                WebResourceResponse shouldInterceptRequest = webViewClientInterface.shouldInterceptRequest(url);
                if (shouldInterceptRequest != null) {
                    return shouldInterceptRequest;
                } else {
                    return super.shouldInterceptRequest(webView, url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webViewClientInterface.onPageFinished(url);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(pageFailToOpen);
                if (needClearHistory) {
                    needClearHistory = false;
                    webView.clearHistory();
                }
                toolbarInterface.onPageFinished(url);
                progressBar.setVisibility(View.GONE);
                if (isDebug) {
                    webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_PAGRFINISH, url));
                    // 注入js 获取html源文件
                    webView.loadUrl("javascript:window.java_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
                }
            }
        });
    }

    //return false 表示由当前WebView处理
    public boolean handleUri(String url) {
        try {
            url = webViewClientInterface.transformUrl(url);
            Uri uri = Uri.parse(url);
            //先交给WebViewHelper的webViewClientInterface处理
            if (webViewClientInterface.onLoadUrl(url)) {
                return true;
            } else if (handleWebViewScheme(uri)) {
                //交给内置的处理jsbridge://的代码处理
                return true;
            } else if (handleHttpLink(url)) {
                //处理Http、Https链接
                return true;
            } else if (url.startsWith("javascript:")) {
                webView.loadUrl(url);
                return false;
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
            String lastUrl = webView.getUrl();
            if (url.equals(lastUrl)) {
                reload();
            } else {
                loadUrl(url);
                handlePossibleBugIfLinkHasHash(lastUrl, url);
            }
            handleExtraParamsInHttpUrl(url);
            return true;
        }
        return false;
    }

    /**
     * 当URL#之前的部分一致时，会出现一下几种情况
     * X5
     * <br>没有#的，自己跳自己，没有堆栈，但可拦截，页面不刷新
     * <br>有#的，自己跳自己，有堆栈，没拦截
     * <br>有#的，跳转有#的，有堆栈，没拦截
     * <br>有#的，调没#的，有堆栈，有拦截
     * <br>没#的，跳有#的，有堆栈，有拦截的
     *
     * @param lastUrl
     * @param url
     */
    private void handlePossibleBugIfLinkHasHash(String lastUrl, String url) {
        try {
            int endIndex = lastUrl.indexOf('#');
            if (endIndex > 0) {
                String schemeAuthority = lastUrl.substring(0, endIndex);
                if (url.startsWith(schemeAuthority)) {
                    webView.reload();
                }
            }
        } catch (Exception e) {
            LogUtil.e(e);
        }
    }

    /**
     * 处理HTTP LINK中约定的参数
     * <br>1:处理固定标题设置
     * <br>2:Toolbar配置
     *
     * @param url
     */
    private void handleExtraParamsInHttpUrl(String url) {
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
                            handleUri(link);
                            needClearHistory = true;
                        }
                    }
                    return true;
                case WebViewScheme.ACTION_GO_BACK_TO_H5_HOME://回到H5应用的首页
                case WebViewScheme.ACTION_GO_BACK_TO_H5_HOME_OLD://回到H5应用的首页
                    handleUri(originUrl);
                    needClearHistory = true;
                    return true;
                case WebViewScheme.ACTION_GO_BACK_OLD://返回上一个加载的页面
                case WebViewScheme.ACTION_GO_BACK://返回上一个加载的页面
                    if (webView.canGoBack()) {
                        goBack();
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
                    goBack();
                    return true;
                } else {
                    return false;
                }
            } else {
                handleUri(backAction);
                return true;
            }
        }
    }

    private void goBack() {
        webView.goBack();
        if (isDebug) {
            webDevTool.putRecorder(new WebviewRecorderModel(WebDevTool.KEY_GOBACK, "回退"));
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

    protected void showDebugBtn() {
        if (debugBtn == null)
            debugBtn = FloatingButton.create(activity.findViewById(Window.ID_ANDROID_CONTENT)
                    , R.drawable.lib_common_debug
                    , 60, 60
                    , (v) -> webDevTool.showDebugPage());
    }

    public void loadUrl() {
        if (null == toolbarInterface) {
            DefaultToolbar defaultToolbar = new DefaultToolbar();
            this.toolbarInterface = defaultToolbar;
            this.toolbarInterface.onAttach(toolbarContainer, this, activity);
            if (isDebug) {
                showDebugBtn();
            }
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

        public void onReceiveTitle(String title) {

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

        /**
         * 一般情况finish Activity
         */
        public abstract void closeWindow();

        /**
         * SwipeRefreshLayout下拉刷新被触发，return true自己处理，记得恢复SwipeRefreshLayout刷新状态
         *
         * @return
         */
        public boolean onRefresh() {
            return false;
        }

        public WebResourceResponse shouldInterceptRequest(String url) {
            return null;
        }
    }

}
