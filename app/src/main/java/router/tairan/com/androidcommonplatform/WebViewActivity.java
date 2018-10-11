package router.tairan.com.androidcommonplatform;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.trc.android.common.h5.WebViewHelper;

public class WebViewActivity extends AppCompatActivity {
    WebViewHelper webViewHelper;
    WebViewHelper.WebViewClientInterface clientInterface = new WebViewHelper.WebViewClientInterface() {
        @Override
        public void openLinkInNewWindow(String url) {
            start(WebViewActivity.this, url);
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
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webViewHelper = WebViewHelper.create(this);
        String url = getIntent().getStringExtra("URL");
        webViewHelper.setClientInterface(clientInterface)
//                .showToolbar(false)
//                .setFixedTitle("固定对TITLE")
                .setDebug(true)
                .setOriginUrl(url);
        setContentView(webViewHelper.getView());
        webViewHelper.loadUrl();
    }

    @Override
    public void onBackPressed() {
        if (!webViewHelper.consumeBackEvent())
            super.onBackPressed();
    }
}
