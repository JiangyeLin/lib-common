package router.tairan.com.androidcommonplatform;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.trc.android.common.CommonLib;
import com.trc.android.common.util.Contexts;
import com.trc.android.common.util.ImgUtil;
import com.trc.android.common.util.PermissionUtil;
import com.trc.android.common.util.PicturesSelectUtil;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonLib.init(getApplication());
        setContentView(R.layout.activity_main);
    }

    public void onClickToWebViewTestActivity(View view) {
        WebViewActivity.start(this, "https://www.baidu.com/");
    }

    public void onClickToWebViewScheme(View view) {
        WebViewActivity.start(this, "https://taiheweb.applinzi.com/index.html?toolbarTitle=" + URLEncoder.encode("我是自定义标题"));

    }

    public void onClickToCube(View view) {
        WebViewActivity.start(this, "https://mofang.tfabric.com/?hideToolbar=true");
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

    public void onCLickToCustomToolbarWebView(View view) {
        ToolbarWebViewActivity.start(this, "https://www.baidu.com/");
    }

    //测试支持全局字典配置的TrTextView
    public void onClickToTrTextView(View view) {
        TrTextViewTestActivity.start(this);
    }

    public void onClickToSinglePermission(View view) {
        PermissionUtil.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "单个位置权限申请测试", new PermissionUtil.OnPermissionCallback() {
            @Override
            public void onGranted() {
                Toast.makeText(MainActivity.this, "单个位置权限申请成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied() {
                Toast.makeText(MainActivity.this, "单个位置权限申请拒绝", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickToMultiPermission(View view) {
        PermissionUtil.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                "多个权限申请测试", new PermissionUtil.OnPermissionsCallback() {
                    @Override
                    public void onGranted() {
                        Toast.makeText(MainActivity.this, "多个权限申请成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(List<String> deniedPermissions) {
                        Toast.makeText(MainActivity.this, "多个权限申请失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
