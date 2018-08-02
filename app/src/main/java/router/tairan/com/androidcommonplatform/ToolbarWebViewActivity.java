package router.tairan.com.androidcommonplatform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trc.android.common.h5.DefaultToolbar;
import com.trc.android.common.h5.WebViewHelper;

public class ToolbarWebViewActivity extends AppCompatActivity {

    WebViewHelper webViewHelper;
    WebViewHelper.WebViewClientInterface clientInterface = new WebViewHelper.WebViewClientInterface() {
        @Override
        public void openLinkInNewWindow(String url) {
            start(ToolbarWebViewActivity.this, url);
        }

        @Override
        public String transformUrl(String url) {//如果有路由转换规则，在此处理
            return super.transformUrl(url);
        }

        @Override
        public boolean onLoadUrl(String url) {//对特定URL感兴趣，可以在此优先处理
            return super.onLoadUrl(url);
        }

        public void closeWindow() {
            finish();
        }
    };


    public static void start(Context context, String url) {
        Intent intent = new Intent(context, ToolbarWebViewActivity.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webViewHelper = WebViewHelper.create(this);
        webViewHelper.setClientInterface(clientInterface)
//                .showToolbar(false)
//                .setFixedTitle("固定对TITLE")
                .setCustomToolbar(new DefaultToolbar() {
                    @Override
                    public void onAttach(ViewGroup container, WebViewHelper helper, Activity host) {
                        activity = host;
                        webViewHelper = helper;
                        webView = helper.getWebView();
                        LayoutInflater.from(container.getContext()).inflate(R.layout.my_h5_toolbar, container, true);
                        tvTitle = container.findViewById(R.id.tvTitle);
                        llToolbarBtnContainer = container.findViewById(R.id.toolbarBtnContainer);
                        View btnClose = container.findViewById(R.id.btnClose);
                        btnClose.setOnClickListener(v -> webViewHelper.closeWindow());
                        View btnBack = container.findViewById(R.id.btnReturn);
                        btnBack.setOnClickListener(v -> activity.onBackPressed());
                    }
                })
                .setOriginUrl(getIntent().getStringExtra("URL"));
        setContentView(webViewHelper.getView());
        webViewHelper.loadUrl();
    }

    @Override
    public void onBackPressed() {
        if (!webViewHelper.consumeBackEvent())
            super.onBackPressed();
    }
}
