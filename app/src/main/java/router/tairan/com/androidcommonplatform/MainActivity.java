package router.tairan.com.androidcommonplatform;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickToWebViewTestActivity(View view) {
        WebViewActivity.start(this, "https://www.baidu.com/");
    }

    public void onClickToWebViewScheme(View view) {
        WebViewActivity.start(this, "http://taiheweb.applinzi.com/scheme.html");

    }

    public void onClickToCube(View view) {
        WebViewActivity.start(this, "https://mofang.tfabric.com/");
    }
}
