package com.trc.android.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trc.android.common.exception.ExceptionManager;
import com.trc.common.R;

import java.io.File;

/**
 * @author HuangMing on 2016/8/9.
 * 调用图库并处理选择的图片
 */
public class PicturesSelectUtil extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CAMERA = 0x001;
    public static final int REQUEST_CROP = 0x002;
    public static final int REQUEST_ALBUM = 0x003;

    private static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG.toString();
    private static final String CROP_TYPE = "image/*";
    private static final int DEFAULT_ASPECT = 1;
    private static final int DEFAULT_OUTPUT = 640;
    public static final String KEY_IS_CROP = "isCrop";
    public static final String KEY_PIC_SIZE = "picSize";
    public static final String KEY_FILE = "File";

    private boolean mIsCrop;
    private int mPicSize = 0;
    public Uri cameraOutPutUri;
    public Uri cropOutPutUri;

    private TextView btnTakePhoto;
    private TextView btnPhotoAlbum;
    private TextView btnCancel;
    private boolean isPaused;

    public interface OnPicturesCallback {
        void onSelect(File file);

        void onCancel();
    }

    public static void select(FragmentActivity activity, boolean isCrop, OnPicturesCallback callback) {
        select(activity, isCrop, 0, callback);
    }

    public static void select(FragmentActivity activity, boolean isCrop, int picSize, OnPicturesCallback callback) {
        LifeCircleCallbackUtil.inject(activity, new LifeCircleCallbackUtil.Callback() {
            @Override
            void onCreate(Fragment fragment) {
                Intent intent = new Intent(activity, PicturesSelectUtil.class);
                intent.putExtra(KEY_IS_CROP, isCrop);
                intent.putExtra(KEY_PIC_SIZE, picSize);

                //应该由LifeCircleCallbackUtil拉起activity,绑定生命周期
                fragment.startActivityForResult(intent, 100);
                //activity.startActivity(intent);
                //activity.overridePendingTransition(0, 0);
            }

            @Override
            void onActivityResult(Fragment fragment, int resultCode, Intent data) {
                if (resultCode == RESULT_OK) {
                    callback.onSelect((File) data.getSerializableExtra(KEY_FILE));
                } else {
                    callback.onCancel();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_common_select_photo);
        mIsCrop = getIntent().getBooleanExtra(KEY_IS_CROP, false);
        mPicSize = getIntent().getIntExtra(KEY_PIC_SIZE, 0);
        View view = findViewById(R.id.content);
        view.getBackground().setAlpha(150);
        btnTakePhoto = findViewById(R.id.select_take_photo);
        btnPhotoAlbum = findViewById(R.id.select_photo_album);
        btnCancel = findViewById(R.id.select_choose_cancel);

        btnTakePhoto.setOnClickListener(this);
        btnPhotoAlbum.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        //拍照后保存图片的路径（注：6规范拍照存储路径）
        cameraOutPutUri = FileUtil.getShareFileUri(System.currentTimeMillis() + ".jpeg");
    }

    @Override
    public void onClick(final View v) {
        int i = v.getId();
        if (i == R.id.select_take_photo) {
            final Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutPutUri);
            photoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PermissionUtil.requestPermission(this, Manifest.permission.CAMERA,
                    "请在设置中授予权限，以正常使用相关功能", new PermissionUtil.OnPermissionCallback() {
                        @Override
                        public void onGranted() {
                            startActivityForResult(photoIntent, PicturesSelectUtil.REQUEST_CAMERA);
                            isPaused = false;
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isPaused) {
                                        toast("相机未打开,请检查APP打开相机权限是否被禁止");
                                    }
                                    isPaused = false;
                                }
                            }, 1000);
                        }

                        @Override
                        public void onDenied() {

                        }
                    });

        } else if (i == R.id.select_photo_album) {
            final Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
            albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            //判断是否有读写图像权限
            PermissionUtil.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    "请在设置中授予权限，以使用相关功能", new PermissionUtil.OnPermissionCallback() {
                        @Override
                        public void onGranted() {
                            startActivityForResult(albumIntent, PicturesSelectUtil.REQUEST_ALBUM);
                            isPaused = false;
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isPaused) {
                                        toast("相册未打开,请检查APP打开相册权限是否被禁止");
                                    }
                                    isPaused = false;
                                }
                            }, 1000);
                        }

                        @Override
                        public void onDenied() {

                        }
                    });

        } else if (i == R.id.select_choose_cancel) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                //拍照之后
                if (requestCode == REQUEST_CAMERA) {
                    onCameraResult();
                    //相册选择之后
                } else if (requestCode == REQUEST_ALBUM) {
                    openCamera(data.getData());
                } else if (requestCode == REQUEST_CROP) {
                    resolveResult(new File(cropOutPutUri.getPath()));
                } else {
                    finish();
                    overridePendingTransition(0, 0);
                }
            } else {
                finish();
                overridePendingTransition(0, 0);
            }
        } catch (Exception e) {
            ExceptionManager.handle(e);
        }
    }

    /**
     * 拍照之后返回结果处理
     */
    public void onCameraResult() {
        if (mIsCrop) {
            cropPhoto(cameraOutPutUri);
        } else {
            File cameraOutFile;
            if ("content".equals(cameraOutPutUri.getScheme())) {
                cameraOutFile = FileUtil.getRealFile(cameraOutPutUri);
            } else {
                cameraOutFile = new File(cameraOutPutUri.getPath());
            }
            resolveResult(cameraOutFile);
        }
    }

    /**
     * 相册选图之后返回结果处理
     */
    public void openCamera(Uri uri) {
        if (mIsCrop) {
            cropPhoto(uri);
        } else {
            //设置图片
            ContentResolver cr = this.getContentResolver();
            File cameraOutFile = new File(ImgUtil.getImagePath(cr, uri));
            resolveResult(cameraOutFile);
        }
    }

    /**
     * 剪裁之后的结果处理
     *
     * @param picFile
     */
    private void resolveResult(File picFile) {
        Intent intent = new Intent();
        intent.putExtra(KEY_FILE, picFile);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, 0);
    }

    /**
     * 剪裁图片
     *
     * @param uri
     */
    private void cropPhoto(Uri uri) {
        if (mPicSize == 0) {
            mPicSize = DEFAULT_OUTPUT;
        }
        try {
            cropOutPutUri = Uri.fromFile(new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpeg"));
            Intent intent = new Intent("com.android.camera.action.CROP", null)
                    .setDataAndType(uri, CROP_TYPE).putExtra("crop", true)
                    .putExtra("scale", true)
                    .putExtra("aspectX", DEFAULT_ASPECT)
                    .putExtra("aspectY", DEFAULT_ASPECT)
                    .putExtra("outputX", mPicSize)
                    .putExtra("outputY", mPicSize)
                    .putExtra("return-data", false)
                    .putExtra("outputFormat", OUTPUT_FORMAT)
                    .putExtra("noFaceDetection", true)
                    .putExtra("scaleUpIfNeeded", true)
                    .putExtra(MediaStore.EXTRA_OUTPUT, cropOutPutUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PicturesSelectUtil.REQUEST_CROP);
        } catch (Exception e) {
            String msg;
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                msg = "图片裁剪失败,SD卡不存在，请插入SD卡";
            } else {
                msg = "图片裁剪失败,请稍后再试";
            }
            toast(msg);
            LogUtil.e(e);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
        setResult(RESULT_CANCELED);
    }
}
