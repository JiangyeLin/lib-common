package com.trc.android.common.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HanTuo on 16/8/5.
 * 检查权限，必要的化申请权限
 * @author linjiangye  权限申请流程优化
 * 调整前：弹窗提醒，只要有一个权限曾经拒绝过，则跳转到系统的应用setting界面，由用户手动授予权限
 * 调整后：
 * 1.直接通过弹窗申请，需要什么申请什么,无需跳转到setting页面手动授予
 * 2.若用户拒绝，重复步骤1
 * 3.若用户拒绝且勾选了不再提示，则弹窗跳转到设置页面，由用户手动授予
 */
public class PermissionUtil extends AppCompatActivity {
    public static final String INTENT_KEY_PERMISSIONS = "INTENT_KEY_PERMISSIONS";
    public static final String INTENT_KEY_PERMISSION_TIPS = "INTENT_KEY_PERMISSION_TIPS";
    public static final String ACTION_REQUEST_PERMISSION = "TRMALL:ACTION_REQUEST_MERMISSION";//TRC
    public static final String KEY_IS_GRANTED = "KEY_IS_GRANTED";
    public static final String KEY_DENIED_LIST = "KEY_DENIED_LIST";
    private ArrayList<String> permissionList;
    private String tips;
    private AlertDialog alertDialog;

    private boolean isNeverAskAgain = false;//用户是否勾选了不再提示

    public interface OnPermissionCallback {
        void onGranted();

        void onDenied();
    }

    public interface OnPermissionsCallback {
        void onGranted();

        void onDenied(List<String> deniedPermissions);
    }

    private static boolean isPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }


    public static void requestPermissions(Context context, String[] permissions, @NonNull String tips, @NonNull final OnPermissionsCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionsGranted(context, permissions)) {
                callback.onGranted();
            } else {
                BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        context.unregisterReceiver(this);
                        boolean isGranted = intent.getBooleanExtra(KEY_IS_GRANTED, false);
                        if (isGranted) {
                            callback.onGranted();
                        } else {
                            callback.onDenied(Collections.EMPTY_LIST);
                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter(ACTION_REQUEST_PERMISSION);
                context.registerReceiver(broadcastReceiver, intentFilter);
                boolean isFromActivity = context instanceof Activity;
                Intent intent = new Intent(context, PermissionUtil.class);
                ArrayList<String> permissionList = new ArrayList<>(permissions.length);
                permissionList.addAll(Arrays.asList(permissions));
                intent.putStringArrayListExtra(INTENT_KEY_PERMISSIONS, permissionList);
                intent.putExtra(INTENT_KEY_PERMISSION_TIPS, tips);
                if (!isFromActivity) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                if (isFromActivity) {
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            }
        } else {
            callback.onGranted();
        }

    }


    public static void requestPermission(Context context, String permission, @NonNull String tips, @NonNull final OnPermissionCallback callback) {
        requestPermissions(context, new String[]{permission}, tips, new OnPermissionsCallback() {
            @Override
            public void onGranted() {
                callback.onGranted();
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                callback.onDenied();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionList = getIntent().getStringArrayListExtra(INTENT_KEY_PERMISSIONS);
        tips = getIntent().getStringExtra(INTENT_KEY_PERMISSION_TIPS);
    }

    //申请权限
    private void doResuestPermissions() {
        String[] permissions = new String[permissionList.size()];
        permissionList.toArray(permissions);
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isAllPermissionGranted = true;
        ArrayList<String> deniedList = new ArrayList<>(permissions.length);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isAllPermissionGranted = false;
                deniedList.add(permissions[i]);
            }
        }
        if (isAllPermissionGranted) {
            Intent intent = new Intent(ACTION_REQUEST_PERMISSION);
            intent.putExtra(KEY_IS_GRANTED, true);
            intent.putStringArrayListExtra(KEY_DENIED_LIST, deniedList);
            sendBroadcast(intent);
            finish();
        } else {
            if (shouldShowExplainationDialog()) {
                //用户只是拒绝了权限 弹窗提醒授予权限
                isNeverAskAgain = false;
            } else {
                //用户拒绝了权限，并且勾选了不再提示，弹窗提醒用户前往设置中进行授权
                isNeverAskAgain = true;
            }
            showExplainationDialog();
        }
    }

    /**
     * 显示解释弹窗
     */
    private void showExplainationDialog() {
        final ArrayList deniedList = new ArrayList();
        for (String permission : permissionList) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                deniedList.add(permission);
            }
        }
        String positiveText = isNeverAskAgain ? "前往设置" : "同意授权";

        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("权限申请")
                    .setMessage(tips)
                    .setPositiveButton(positiveText, (dialog, which) -> {
                        if (isNeverAskAgain) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                            }
                        } else {
                            doResuestPermissions();
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        Intent intent = new Intent(ACTION_REQUEST_PERMISSION);
                        intent.putExtra(KEY_IS_GRANTED, false);
                        intent.putExtra(KEY_DENIED_LIST, deniedList);
                        sendBroadcast(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .create();
        }
        if (!alertDialog.isShowing())
            alertDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private boolean shouldShowExplainationDialog() {
        for (String permission : permissionList) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == alertDialog || !alertDialog.isShowing()) {
            if (shouldShowExplainationDialog()) {
                showExplainationDialog();
            } else {
                doResuestPermissions();
            }
        }
    }
}
