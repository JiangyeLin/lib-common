package com.trc.android.common.h5.devtool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trc.android.common.h5.CookieUtil;
import com.trc.android.common.h5.WebViewHelper;
import com.trc.android.common.util.Contexts;
import com.trc.android.common.util.FileUtil;
import com.trc.common.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * JiangyeLin on 2018/7/13
 * webview调试工具界面
 */
public class WebDebugFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private TextView tvLog;
    private TextView tvInfo;
    private FrameLayout frameLayout;
    private TextView tvSources;
    private TextView tvShortCuts;
    private TextView tvHTML;

    private WebDevTool recorder;
    private Button btnClearData;//清除历史数据
    WebViewHelper webViewHelper;

    public static WebDebugFragment newInstance(WebDevTool webDevToolRecorder, WebViewHelper webViewHelper) {
        WebDebugFragment fragment = new WebDebugFragment();
        fragment.webViewHelper = webViewHelper;
        fragment.recorder = webDevToolRecorder;
        return fragment;
    }


    //显示 调试工具界面
    public void showDevTools() {
        FragmentActivity activity = (FragmentActivity) webViewHelper.getView().getContext();
        setDevToolCallBack(new WebDebugFragment.DevToolCallBack() {

            @Override
            public void clearCacheOnClick() {
                webViewHelper.getWebView().clearCache(true);
            }

            @Override
            public void reloadOnClick() {
                webViewHelper.reload();
            }

            @Override
            public void loadUrlOnClick(@Nullable String url) {
                if (TextUtils.isEmpty(url)) {
                    webViewHelper.loadUrl();
                } else {
                    webViewHelper.loadUrl(url);
                }
                hide();
            }

            @Override
            public void setCookies(String domain, String... cookies) {
                for (String cookie : cookies) {
                    //cookie:   key=123
                    String[] keyValue = cookie.split("=");
                    if (keyValue.length == 2) {
                        if (!TextUtils.isEmpty(domain)) {
                            //指定域名的直接设置cookie
                            CookieUtil.setCookie(domain, keyValue[0], keyValue[1]);
                        } else {
                            //未指定域名的暂存
                            recorder.addCustomCookie(keyValue[0], keyValue[1]);
                        }

                        Toast.makeText(activity, "Cookie设置成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void clearCookies() {
                CookieUtil.clearCookie();
                if (recorder.getCustomCookies() != null) {
                    recorder.getCustomCookies().clear();
                }
            }

            @Override
            public void debugX5() {
                webViewHelper.loadUrl("http://debugtbs.qq.com");
                hide();
            }
        });
        show(activity);
    }

    private void show(FragmentActivity activity) {

        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        boolean isAdded = supportFragmentManager.getFragments().contains(this);
        if (isAdded) {
            supportFragmentManager.beginTransaction().show(this).commit();
        } else {
            supportFragmentManager.beginTransaction()
                    .add(Window.ID_ANDROID_CONTENT, this)
                    .addToBackStack(getClass().getSimpleName())
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = View.inflate(getContext(), R.layout.lib_common_webdevtool_layout, null);

        //兼容全屏状态
        try {
            if ((getActivity().getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) {
                int resourceId = getActivity().getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    int height = getActivity().getResources().getDimensionPixelSize(resourceId);
                    rootView.setPadding(0, height, 0, 0);
                }
            }
        } catch (Exception ignored) {
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            return;
        }
        if (tvLog.isSelected()) {
            //fragment重新可见,刷新一下log列表
            initLogLayout();
        } else if (tvInfo.isSelected()) {
            initInfoLayout();
        } else if (tvSources.isSelected()) {
            initSourcesLayout();
        }
    }

    private void initViews() {
        frameLayout = rootView.findViewById(R.id.framelayout);

        tvLog = rootView.findViewById(R.id.tvLog);
        tvInfo = rootView.findViewById(R.id.tvInfo);
        tvSources = rootView.findViewById(R.id.tvSources);
        tvShortCuts = rootView.findViewById(R.id.tvShortcuts);
        tvHTML = rootView.findViewById(R.id.tvHtml);

        tvLog.setOnClickListener(this);
        tvInfo.setOnClickListener(this);
        tvSources.setOnClickListener(this);
        tvShortCuts.setOnClickListener(this);
        tvHTML.setOnClickListener(this);
        tvLog.setSelected(true);

        initLogLayout();

        Button btnExit = rootView.findViewById(R.id.btnExit);
        btnClearData = rootView.findViewById(R.id.btnClearData);

        btnExit.setOnClickListener(v -> hide());

        btnClearData.setOnClickListener(v -> {
            if (tvLog.isSelected()) {
                if (recorder.getConsoleLogList() != null) {
                    recorder.getConsoleLogList().clear();
                }
                if (recorder.getRecorderModelList() != null) {
                    recorder.getRecorderModelList().clear();
                }
                if (recorder.getErrorList() != null) {
                    recorder.getErrorList().clear();
                }
                initLogLayout();
            } else if (tvSources.isSelected()) {
                if (recorder.getSourcesMap() != null) {
                    recorder.getSourcesMap().clear();
                }
                initSourcesLayout();
            }
        });
    }

    private void hide() {
        getActivity().getSupportFragmentManager().beginTransaction().hide(this).commit();
    }

    @Override
    public void onClick(View v) {
        if (R.id.tvLog == v.getId()) {
            if (tvLog.isSelected()) {
                return;
            }
            clearBtnStatus();
            tvLog.setSelected(true);
            initLogLayout();
            btnClearData.setVisibility(View.VISIBLE);
        } else if (R.id.tvSources == v.getId()) {
            if (tvSources.isSelected()) {
                return;
            }
            clearBtnStatus();
            tvSources.setSelected(true);
            initSourcesLayout();
            btnClearData.setVisibility(View.VISIBLE);
        } else if (R.id.tvInfo == v.getId()) {
            if (tvInfo.isSelected()) {
                return;
            }
            clearBtnStatus();
            tvInfo.setSelected(true);
            initInfoLayout();
            btnClearData.setVisibility(View.GONE);
        } else if (R.id.tvShortcuts == v.getId()) {
            if (tvShortCuts.isSelected()) {
                return;
            }
            clearBtnStatus();
            tvShortCuts.setSelected(true);
            initShortCutsLayout();
            btnClearData.setVisibility(View.GONE);
        } else if (R.id.tvHtml == v.getId()) {
            if (tvHTML.isSelected()) {
                return;
            }
            clearBtnStatus();
            tvHTML.setSelected(true);
            initShowHtmlLayout();
            btnClearData.setVisibility(View.GONE);
        }
    }

    private void clearBtnStatus() {
        tvLog.setSelected(false);
        tvInfo.setSelected(false);
        tvSources.setSelected(false);
        tvShortCuts.setSelected(false);
        tvHTML.setSelected(false);
    }

    /**
     * 当前信息 视图
     */
    private void initInfoLayout() {
        frameLayout.removeAllViews();
        View view = View.inflate(getContext(), R.layout.lib_common_webdevtool_info, null);
        frameLayout.addView(view);

        String system = "Android " + Build.VERSION.RELEASE;
        TextView tvSystem = view.findViewById(R.id.tvSystem);
        TextView tvUA = view.findViewById(R.id.tvUa);

        tvSystem.setText("System: " + system);
        tvUA.setText("UA: " + recorder.getUA());

        TextView tvX5Status = view.findViewById(R.id.tvX5Status);
        if (null == webViewHelper.getWebView().getX5WebViewExtension()) {
            tvX5Status.setText("x5内核状态：未生效");
        } else {
            tvX5Status.setText("x5内核状态：已生效");
        }

        //当前url地址
        TextView tvCurUrl = view.findViewById(R.id.tvCurUrl);
        tvCurUrl.setText(webViewHelper.getWebView().getUrl());

        view.findViewById(R.id.btnJumpBrowser).setOnClickListener(v -> {
            Uri uri = Uri.parse(webViewHelper.getWebView().getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        Switch swDetail = view.findViewById(R.id.sw_isDetailCookie);
        swDetail.setChecked(recorder.isDetailCookie());
        swDetail.setOnCheckedChangeListener((buttonView, isChecked) -> recorder.setDetailCookie(isChecked));

        LinearLayout llCookies = view.findViewById(R.id.llCookies);
        llCookies.removeAllViews();

        if (!recorder.isDetailCookie()) {
            //简单
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (TextUtils.isEmpty(recorder.getX5Cookies())) {
                return;
            }

            String[] x5Cookies = recorder.getX5Cookies().split(";");

            TextView tvX5Cookie = new TextView(getContext());
            tvX5Cookie.setPadding(0, 8, 0, 8);
            tvX5Cookie.setText("以下是x5内核的cookie");
            tvX5Cookie.setTextColor(ContextCompat.getColor(getContext(), R.color.lib_common_devtool_indigo));
            llCookies.addView(tvX5Cookie);

            for (String cookie : x5Cookies) {
                String[] keyValue = cookie.split("=");

                if (keyValue.length == 2) {
                    LinearLayout linearLayout = new LinearLayout(getContext());

                    TextView etKey = new TextView(getContext());
                    etKey.setGravity(Gravity.LEFT);
                    TextView etValue = new TextView(getContext());

                    etKey.setText(keyValue[0]);
                    etValue.setText(keyValue[1]);

                    etKey.setTextIsSelectable(true);
                    etValue.setTextIsSelectable(true);

                    etValue.setPadding(60, 0, 0, 0);

                    etKey.setLayoutParams(params);
                    linearLayout.addView(etKey);
                    linearLayout.addView(etValue);

                    llCookies.addView(linearLayout);
                }
            }

            //添加systemCookies
            if (!TextUtils.isEmpty(recorder.getSystemCookies())) {
                String[] systemCookies = recorder.getSystemCookies().split(";");
                TextView tvSystemCookie = new TextView(getContext());
                tvSystemCookie.setPadding(0, 8, 0, 8);
                tvSystemCookie.setText("以下是系统内核的cookie");
                tvSystemCookie.setTextColor(ContextCompat.getColor(getContext(), R.color.lib_common_devtool_indigo));
                llCookies.addView(tvSystemCookie);

                for (String cookie : systemCookies) {
                    String[] keyValue = cookie.split("=");
                    if (keyValue.length == 2) {
                        LinearLayout linearLayout = new LinearLayout(getContext());

                        TextView etKey = new TextView(getContext());
                        etKey.setGravity(Gravity.LEFT);
                        TextView etValue = new TextView(getContext());

                        etKey.setText(keyValue[0]);
                        etValue.setText(keyValue[1]);

                        etKey.setTextIsSelectable(true);
                        etValue.setTextIsSelectable(true);

                        etValue.setPadding(60, 0, 0, 0);

                        etKey.setLayoutParams(params);
                        linearLayout.addView(etKey);
                        linearLayout.addView(etValue);

                        llCookies.addView(linearLayout);
                    }
                }
            }

        } else if (recorder.getCookieSet() != null) {
            //详细
            for (String str : recorder.getCookieSet()) {
                TextView etKey = new TextView(getContext());
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
                if (str.contains("Path")) {
                    int pos = str.indexOf("Path");
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), pos, pos + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (str.contains("expires")) {
                    int pos = str.indexOf("expires");
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), pos, pos + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (str.contains("domain")) {
                    int pos = str.indexOf("domain");
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), pos, pos + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                etKey.setText(spannableStringBuilder);
                llCookies.addView(etKey);
            }
        }
    }

    /**
     * 资源加载记录视图
     */
    private void initSourcesLayout() {
        frameLayout.removeAllViews();
        View view = View.inflate(getContext(), R.layout.lib_common_webdevtool_source, null);
        frameLayout.addView(view);

        RecyclerView rvLeft = view.findViewById(R.id.rvLeft);
        RecyclerView rvRight = view.findViewById(R.id.rvRight);

        if (recorder.getSourcesMap() == null || recorder.getSourcesMap().size() == 0) {
            return;
        }
        List<String> hosts = new ArrayList<>();
        for (ArrayMap.Entry entry : recorder.getSourcesMap().entrySet()) {
            hosts.add(entry.getKey().toString());
        }

        SparseBooleanArray checkedArray = new SparseBooleanArray();
        checkedArray.put(0, true);
        //域名列表
        rvLeft.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeft.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(View.inflate(getContext(), R.layout.lib_common_webdevtool_source_item, null)) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = holder.itemView.findViewById(R.id.tvContent);
                textView.setSelected(checkedArray.get(position));//是否选中

                textView.setText(hosts.get(position));
                textView.setOnTouchListener((v, event) -> {
                    //处理与长按复制的冲突
                    textView.setFocusable(false);
                    return false;
                });
                textView.setOnClickListener(v -> {
                    checkedArray.clear();
                    checkedArray.put(position, true);
                    notifyDataSetChanged();

                    rvRight.setAdapter(new SourcesRightAdapter(recorder.getSourcesMap().get(hosts.get(position))));
                });
            }

            @Override
            public int getItemCount() {
                return hosts.size();
            }
        });

        //域名加载的资源列表
        rvRight.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRight.setAdapter(new SourcesRightAdapter(recorder.getSourcesMap().get(hosts.get(0))));
    }

    //source栏右侧 适配器
    private static class SourcesRightAdapter extends RecyclerView.Adapter {

        private List<String> list;

        SourcesRightAdapter(List<String> list) {
            super();
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lib_common_webdevtool_source_item, parent, false);
            return new RecyclerView.ViewHolder(view) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = holder.itemView.findViewById(R.id.tvContent);
            textView.setText(list.get(position));
            textView.setSelected(true);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }
    }


    /**
     * log筛选
     */
    private LinkedHashMap<String, String> filterMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Boolean> checkedItems = new LinkedHashMap<>();

    private List<WebDevTool.RecorderModel> handlerLogFilter() {
        List<WebDevTool.RecorderModel> list = new ArrayList<>(recorder.getRecorderModelList());

        Iterator<WebDevTool.RecorderModel> iterator = list.iterator();
        while (iterator.hasNext()) {
            WebDevTool.RecorderModel model = iterator.next();
            if (model.key.equals(WebDevTool.KEY_OVERRIDE_URL_LOADING) && !checkedItems.get("ShouldOverrideUrlLoading")) {
                iterator.remove();
            } else if (model.key.equals(WebDevTool.KEY_LOADURL) && !checkedItems.get("LoadUrl")) {
                iterator.remove();
            } else if (model.key.equals(WebDevTool.KEY_RELOAD) && !checkedItems.get("Reload")) {
                iterator.remove();
            } else if (model.key.equals(WebDevTool.KEY_PAGESTART) && !checkedItems.get("onPageStarted")) {
                iterator.remove();
            } else if (model.key.equals(WebDevTool.KEY_PAGRFINISH) && !checkedItems.get("onPageFinished")) {
                iterator.remove();
            } else if (!checkedItems.get("生命周期事件")) {
                if (model.key.equals(WebDevTool.kEY_PAUSE) || model.key.equals(WebDevTool.KEY_RESUME) || model.key.equals(WebDevTool.KEY_GOBACK)) {
                    iterator.remove();
                }
            }
        }
        return list;
    }

    /**
     * 日志视图
     */
    private void initLogLayout() {
        filterMap.put("ShouldOverrideUrlLoading", WebDevTool.KEY_OVERRIDE_URL_LOADING);
        filterMap.put("LoadUrl", WebDevTool.KEY_LOADURL);
        filterMap.put("Reload", WebDevTool.KEY_RELOAD);
        filterMap.put("onPageStarted", WebDevTool.KEY_PAGESTART);
        filterMap.put("onPageFinished", WebDevTool.KEY_PAGRFINISH);
        filterMap.put("生命周期事件", WebDevTool.KEY_GOBACK);

        checkedItems.put("ShouldOverrideUrlLoading", true);
        checkedItems.put("LoadUrl", true);
        checkedItems.put("Reload", true);
        checkedItems.put("onPageStarted", true);
        checkedItems.put("onPageFinished", true);
        checkedItems.put("生命周期事件", true);

        frameLayout.removeAllViews();
        View view = View.inflate(getContext(), R.layout.lib_common_webdevtool_log, null);
        frameLayout.addView(view);

        TextView tvJump = view.findViewById(R.id.tvJump);
        TextView tvConsole = view.findViewById(R.id.tvConsole);
        TextView tvError = view.findViewById(R.id.tvError);

        //筛选日志
        View imgFilter = view.findViewById(R.id.imgFilter);

        tvJump.setSelected(true);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(new WebDevToolAdapter(recorder.getRecorderModelList()));

        tvJump.setOnClickListener(v -> {
            imgFilter.setVisibility(View.VISIBLE);
            tvJump.setSelected(true);

            tvConsole.setSelected(false);
            tvError.setSelected(false);

            recyclerView.setAdapter(new WebDevToolAdapter(recorder.getRecorderModelList()));
        });
        tvConsole.setOnClickListener(v -> {
            tvJump.setSelected(false);
            tvConsole.setSelected(true);
            tvError.setSelected(false);
            recyclerView.setAdapter(new WebDevToolAdapter(recorder.getConsoleLogList()));
            imgFilter.setVisibility(View.GONE);
        });
        tvError.setOnClickListener(v -> {
            tvJump.setSelected(false);
            tvConsole.setSelected(false);
            tvError.setSelected(true);

            recyclerView.setAdapter(new WebDevToolAdapter(recorder.getErrorList()));
            imgFilter.setVisibility(View.GONE);
        });
        imgFilter.setOnClickListener(v -> {
            //生成dialog展示数据
            List<String> itemList = new ArrayList<>();
            List<Boolean> checkList = new ArrayList<>();

            //dialog要求的数据格式
            boolean[] booleans = new boolean[filterMap.size()];
            String[] items = new String[filterMap.size()];

            itemList.clear();
            checkList.clear();

            for (String item : filterMap.keySet()) {
                itemList.add(item);
                checkList.add(checkedItems.get(item));
            }

            for (int i = 0; i < checkedItems.size(); i++) {
                items[i] = itemList.get(i);
                booleans[i] = checkList.get(i);
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("筛选-勾选您要展示的条目")
                    .setMultiChoiceItems(items, booleans, (dialog, which, isChecked) -> {
                        checkedItems.put(items[which], isChecked);
                        recyclerView.setAdapter(new WebDevToolAdapter(handlerLogFilter()));
                    })
                    .create()
                    .show();
        });
    }

    /**
     * 快捷操作视图
     */
    private void initShortCutsLayout() {
        View view = View.inflate(getContext(), R.layout.lib_common_webdevtool_shortcut, null);
        frameLayout.removeAllViews();
        frameLayout.addView(view);

        view.findViewById(R.id.btnReload).setOnClickListener(v -> {
            if (devToolCallBack != null) {
                if (recorder.getCookieSet() != null) {
                    recorder.getCookieSet().clear();
                }
                devToolCallBack.reloadOnClick();
            }
        });

        view.findViewById(R.id.btnClearCache).setOnClickListener(v -> {
            if (devToolCallBack != null) {
                devToolCallBack.clearCacheOnClick();
            }
        });

        view.findViewById(R.id.btnDebugx5).setOnClickListener(v -> {
            if (devToolCallBack != null) {
                devToolCallBack.debugX5();
            }
        });

        //loadurl 快捷
        AppCompatAutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autotvUrl);

        ArrayList<String> datas = readUrlInputHistory();
        if (!datas.contains("http://")) {
            datas.add("http://");
        }
        if (!datas.contains("https://")) {
            datas.add("https://");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datas);
        autoCompleteTextView.setAdapter(adapter);

        view.findViewById(R.id.btnLoadurl).setOnClickListener(v -> {
            if (devToolCallBack == null) {
                return;
            }
            String url = autoCompleteTextView.getText().toString();

            if (!TextUtils.isEmpty(url)) {
                //暂存到本地
                adapter.add(url);
                writeUrlInputHistory(datas, url);
            }
            devToolCallBack.loadUrlOnClick(url);
        });

        EditText etDomain = view.findViewById(R.id.etDomain);
        EditText etKey = view.findViewById(R.id.etKey);
        EditText etValue = view.findViewById(R.id.etValue);

        view.findViewById(R.id.btnSetCookie).setOnClickListener(v -> {
            String key = etKey.getText().toString();
            String value = etValue.getText().toString();

            if (TextUtils.isEmpty(key)) {
                Toast.makeText(getContext(), "请输入您想要设置的key", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(value)) {
                Toast.makeText(getContext(), "请输入您想要设置的cookie", Toast.LENGTH_SHORT).show();
                return;
            }
            String cookie = key + "=" + value;
            if (devToolCallBack != null) {
                devToolCallBack.setCookies(etDomain.getText().toString(), cookie);
            }
        });

        view.findViewById(R.id.btnClearCookie).setOnClickListener(v -> {
            if (devToolCallBack != null) {
                devToolCallBack.clearCookies();
            }
        });
    }

    /**
     * 显示html源码视图
     */
    private void initShowHtmlLayout() {
        frameLayout.removeAllViews();
        View view = View.inflate(getContext(), R.layout.lib_common_webdevtool_html, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        List<String> list = loadHtmlList();
        view.findViewById(R.id.btnShareHtml).setOnClickListener(v -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : list) {
                stringBuilder.append(string);
            }
            try {
                //分享文件
                Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                sendIntent.setType("*/*");
                sendIntent.putExtra(Intent.EXTRA_STREAM, FileUtil.getShareFileUri("WebDebug.html"));
                startActivity(Intent.createChooser(sendIntent, "发送HTML"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "抱歉，分享失败，已将文件保存在应用ExternalCache目录下,请您自行拷贝", Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(View.inflate(getContext(), R.layout.lib_common_webdevtool_htmlcode_item, null)) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = holder.itemView.findViewById(R.id.tvContent);
                textView.setText(list.get(position));
            }

            @Override
            public int getItemCount() {
                return list == null ? 0 : list.size();
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            list.clear();
            list.addAll(loadHtmlList());
            recyclerView.getAdapter().notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });
        frameLayout.addView(view);
    }

    //加载html源码文件到list中
    private List<String> loadHtmlList() {
        ArrayList<String> list = new ArrayList<>();
        BufferedReader bufferedReader = null;
        File file = FileUtil.getShareFile("WebDebug.html");

        if (file.exists()) {
            try {
                FileInputStream is = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(is));
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    list.add(string);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private DevToolCallBack devToolCallBack;

    public interface DevToolCallBack {
        void clearCacheOnClick();

        void reloadOnClick();

        void loadUrlOnClick(@Nullable String url);

        void setCookies(String domain, String... cookie);

        void clearCookies();

        void debugX5();
    }

    public void setDevToolCallBack(DevToolCallBack devToolCallBack) {
        this.devToolCallBack = devToolCallBack;
    }

    /**
     * 缓存 输入的loadurl历史记录
     */

    private ArrayList<String> readUrlInputHistory() {
        SharedPreferences sharedPreferences = Contexts.getInstance().getSharedPreferences("WebDevTool.xml", Context.MODE_PRIVATE);
        Set<String> stringSet = sharedPreferences.getStringSet("url", new HashSet<>());
        return new ArrayList<>(stringSet);
    }

    private void writeUrlInputHistory(ArrayList<String> list, String url) {
        list.add(url);
        if (list.size() > 5) {
            list.remove(0);
        }
        SharedPreferences sp = Contexts.getInstance().getSharedPreferences("WebDevTool.xml", Context.MODE_PRIVATE);
        Set set = new HashSet(list);
        sp.edit().putStringSet("url", set).apply();
    }
}
