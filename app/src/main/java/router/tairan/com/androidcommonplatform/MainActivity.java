package router.tairan.com.androidcommonplatform;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.trc.android.common.util.Contexts;
import com.trc.android.common.util.ImgUtil;
import com.trc.android.common.util.PicturesSelectUtil;

import java.io.File;

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

    public void onCLickToPicture(View view) {
        Contexts.init(getApplication());
        PicturesSelectUtil.select(this, false, new PicturesSelectUtil.OnPicturesCallback() {
            @Override
            public void onSelect(File file) {
                Log.d("onSelect", "选择成功: " + file.getPath());
                File tmp = new File(getExternalCacheDir(), "tmp.jpg");
                ImgUtil.compress(file, tmp, 100, 640 * 384);
            }

            @Override
            public void onCancel() {

            }
        });
    }
}
