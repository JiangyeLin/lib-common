package com.trc.android.common.h5.devtool;

import androidx.collection.ArrayMap;

import com.trc.android.common.h5.WebViewHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JiangyeLin on 2018/7/13
 * 为webview调试工具记录数据
 */
public class WebDevTool {
    public static String KEY_PAGESTART = "开始加载";
    public static String KEY_PAGRFINISH = "加载完成";
    public static String KEY_RECEIVE_ERROR = "加载错误";
    public static String KEY_OVERRIDE_URL_LOADING = "ShouldOverrideUrlLoading";
    public static String KEY_GOBACK = "Webview";
    public static String KEY_RELOAD = "Reload";
    public static String KEY_LOADURL = "LoadUrl";
    public static String kEY_PAUSE = "Webview";
    public static String KEY_RESUME = "Webview";
    public static String KEY_CONSOLE = "Console";

    private List<RecorderModel> recorderModelList;
    private List<RecorderModel> consoleLogList;
    private List<RecorderModel> errorList;

    private String UA;

    /**
     * cookie manager中只能取到cookie的key-value，如果要取cookie的path、expires等字段，需要手动创建url connect从header里面取，
     * 可能会导致某些问题，所以默认只取cookie manager中的参数
     */
    private boolean isDetailCookie = false;

    private String systemCookies; //系统cookie
    private String x5Cookies;   //x5cookie
    private Set<String> detailCookiesSet;   //通过header取的服务端set-cookie字段里的cookie

    /**
     * 自定义的cookie
     */
    private List<RecorderModel> customCookies;

    /**
     * 记录加载的资源
     * key=host
     * value=所有该host下加载的资源
     */
    private static ArrayMap<String, List<String>> sourcesMap;

    public List<RecorderModel> getRecorderModelList() {
        return recorderModelList;
    }

    public void putRecorder(RecorderModel webviewRecorderModel) {
        if (recorderModelList == null) {
            recorderModelList = new ArrayList<>();
        }
        recorderModelList.add(webviewRecorderModel);
    }

    public List<RecorderModel> getConsoleLogList() {
        return consoleLogList;
    }

    public void putConsoleLog(RecorderModel webviewRecorderModel) {
        if (consoleLogList == null) {
            consoleLogList = new ArrayList<>();
        }
        consoleLogList.add(webviewRecorderModel);
    }

    public void putErrorLog(RecorderModel webviewRecorderModel) {
        if (errorList == null) {
            errorList = new ArrayList<>();
        }
        errorList.add(webviewRecorderModel);
    }

    public List<RecorderModel> getErrorList() {
        return errorList;
    }

    public String getUA() {
        return UA;
    }

    public void setUA(String UA) {
        this.UA = UA;
    }

    public void loadSources(String host, String source) {
        if (sourcesMap == null) {
            sourcesMap = new ArrayMap<>();
        }
        List<String> list = sourcesMap.get(host);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(source);
        sourcesMap.put(host, list);
    }

    public ArrayMap<String, List<String>> getSourcesMap() {
        return sourcesMap;
    }

    public void addCustomCookie(String key, String cookie) {
        if (customCookies == null) {
            customCookies = new ArrayList<>();
        }
        customCookies.add(new RecorderModel(key, cookie));
    }

    public List<RecorderModel> getCustomCookies() {
        return customCookies;
    }

    public boolean isDetailCookie() {
        return isDetailCookie;
    }

    public void setDetailCookie(boolean detailCookie) {
        isDetailCookie = detailCookie;
    }

    public String getX5Cookies() {
        return x5Cookies;
    }

    public void setX5Cookies(String x5Cookies) {
        this.x5Cookies = x5Cookies;
    }

    public String getSystemCookies() {
        return systemCookies;
    }

    public void setSystemCookies(String systemCookies) {
        this.systemCookies = systemCookies;
    }

    public Set<String> getCookieSet() {
        return detailCookiesSet;
    }

    public void addDetailCookie(String cookie) {
        if (detailCookiesSet == null) {
            detailCookiesSet = new HashSet<>();
        }
        detailCookiesSet.add(cookie);
    }

    public void showDebugPage() {
        if (null == webDevToolDialogFragment) {
            webDevToolDialogFragment = WebDebugFragment.newInstance(this, webViewHelper);
        }
        webDevToolDialogFragment.showDevTools();
    }


    public WebDevTool(WebViewHelper webViewHelper) {
        this.webViewHelper = webViewHelper;
    }

    WebDebugFragment webDevToolDialogFragment;
    WebViewHelper webViewHelper;


    /**
     * 调试工具model
     */
    public static class RecorderModel {
        public String key;
        public String desc;

        public RecorderModel(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }
    }
}
