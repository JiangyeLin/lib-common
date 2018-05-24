package com.trc.android.common.h5;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.trc.android.common.exception.ExceptionManager;
import com.trc.android.common.util.CookieUtil;
import com.trc.android.common.util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TrWebView extends WebView {
    private static HashSet<String> urlSet = new HashSet<>();

    public TrWebView(Context context) {
        super(context);
        initWebView();
    }

    public TrWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public TrWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebView();
    }

    private boolean needLoadOnResume;


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            if (needLoadOnResume) {
                loadUrl("javascript:onResume()");
            }
        } else if (visibility == View.GONE) {
            needLoadOnResume = true;
            loadUrl("javascript:onPause()");
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");
        removeJavascriptInterface("searchBoxJavaBridge_");
        WebSettings webSettings = getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//允许Https页面内打开Http链接
            webSettings.setMixedContentMode(WebSettings.LOAD_NORMAL);
        }
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    if (mimetype.equals("application/vnd.android.package-archive") || url.toLowerCase().contains(".apk")) {
                        final File file = FileUtil.getShareFile(url.hashCode() + "" + url.length() + ".apk");
                        if (file.exists()) {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setDataAndType(FileUtil.getShareFileUri(file), "application/vnd.android.package-archive");
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getContext().startActivity(it);
                        } else {
                            if (urlSet.add(url)) {
                                Toast.makeText(getContext(), "开始下载,下载完成后将提示您安装应用", Toast.LENGTH_LONG).show();
                                DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                final File tmpFile = new File(file.getPath() + "." + System.currentTimeMillis());
                                request.setDestinationUri(Uri.fromFile(tmpFile));
                                request.setTitle("下载中");
                                request.setDescription(contentDisposition);
                                final long downloadId = downloadManager.enqueue(request);
                                getContext().getApplicationContext().registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        long requestId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                                        if (requestId == downloadId) {
                                            urlSet.remove(url);
                                            if (tmpFile.renameTo(file)) {
                                                Intent it = new Intent(Intent.ACTION_VIEW);
                                                it.setDataAndType(FileUtil.getShareFileUri(file), "application/vnd.android.package-archive");
                                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                it.setAction(Intent.CATEGORY_DEFAULT);
                                                context.startActivity(it);
                                            }
                                        }
                                    }
                                }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                            } else {
                                Toast.makeText(getContext(), "下载中，可在通知栏查看下载任务。", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        getContext().startActivity(intent);
                    }
                } catch (Exception e) {
                    ExceptionManager.handle(e);
                }
            }
        });
    }

    @Override
    public void loadUrl(String url) {
        if (null == url) {
            return;
        }
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", url);
        setCookie(url);
        super.loadUrl(url, extraHeaders);
    }

    @Override
    public void reload() {
        String url = getUrl();
        setCookie(url);
        super.reload();
    }

    private void setCookie(String url) {
        if (null != url && url.startsWith("http")) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            for (CookieConfig.KeyValue keyValue : CookieConfig.getUniversalCookie()) {
                CookieUtil.setCookie(host, keyValue.key, keyValue.value);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupToolbarIfNecessary();
    }

    private void setupToolbarIfNecessary() {

    }

}

